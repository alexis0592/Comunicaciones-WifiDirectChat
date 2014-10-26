package co.edu.udea.comunicacionesapp.activities;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import co.edu.udea.comunicacionesapp.R;
import co.edu.udea.comunicacionesapp.activities.fragments.WifiChatFragment;
import co.edu.udea.comunicacionesapp.activities.fragments.WifiChatFragment.MessageTarget;
import co.edu.udea.comunicacionesapp.broadcast.WifiBroadcastReceiver;
import co.edu.udea.comunicacionesapp.services.ChatManager;
import co.edu.udea.comunicacionesapp.services.ClientSocketHandler;
import co.edu.udea.comunicacionesapp.services.GroupOwnerSocketHandler;
import co.edu.udea.comunicacionesapp.services.WifiDirectServicesList;
import co.edu.udea.comunicacionesapp.services.WifiDirectServicesList.DeviceClickListener;
import co.edu.udea.comunicacionesapp.services.adapter.WiFiP2pService;
import co.edu.udea.comunicacionesapp.services.adapter.WifiDeviceAdapter;

public class WifiDirectActivity extends Activity implements
		DeviceClickListener, Handler.Callback, MessageTarget,
		ConnectionInfoListener {

	public static final String TAG = "wifidirectdemo";

	public static final int MESSAGE_READ = 0x400 + 1;
	public static final int MY_HANDLE = 0x400 + 2;

	// TXT RECORD properties
	public static final String TXTRECORD_PROP_AVAILABLE = "available";
	public static final String SERVICE_INSTANCE = "_wifidemotest";
	public static final String SERVICE_REG_TYPE = "_presence._tcp";
	private WifiP2pManager manager;

	public static final int SERVER_PORT = 4545;

	private final IntentFilter intentFilter = new IntentFilter();
	private Channel mChannel;
	private BroadcastReceiver receiver = null;
	private WifiP2pDnsSdServiceRequest serviceRequest;

	private Handler handler = new Handler(this);
	private WifiChatFragment chatFragment;
	private WifiDirectServicesList servicesList;

	private TextView statusTxtView;

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		statusTxtView = (TextView) findViewById(R.id.status_text);

		// Indicates a change in the Wi-Fi P2P status.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

		// Indicates a change in the list of available peers.
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

		// Indicates the state of Wi-Fi P2P connectivity has changed.
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

		// Indicates this device's details have changed.
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		this.mChannel = manager.initialize(this, getMainLooper(), null);
		startRegistrationAndDiscovery();

		servicesList = new WifiDirectServicesList();
		getFragmentManager().beginTransaction()
				.add(R.id.container_root, servicesList, "services").commit();

	}

	@Override
	protected void onRestart() {
		Fragment frag = getFragmentManager().findFragmentByTag("services");
		if (frag != null) {
			getFragmentManager().beginTransaction().remove(frag).commit();
		}
		super.onRestart();
	}

	@Override
	protected void onStop() {
		if (manager != null && mChannel != null) {
			manager.removeGroup(mChannel, new ActionListener() {

				@Override
				public void onFailure(int reasonCode) {
					Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
				}

				@Override
				public void onSuccess() {
				}

			});
		}
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	private void startRegistrationAndDiscovery() {
		Map<String, String> record = new HashMap<String, String>();
		record.put(TXTRECORD_PROP_AVAILABLE, "visible");

		WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
				SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
		try {
			manager.addLocalService(mChannel, service, new ActionListener() {

				@Override
				public void onSuccess() {
					appendStatus("Added Local Service");
				}

				@Override
				public void onFailure(int error) {
					appendStatus("Failed to add a service");
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		discoverService();

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
		Thread handler = null;
		/*
		 * The group owner accepts connections using a server socket and then
		 * spawns a client socket for every client. This is handled by {@code
		 * GroupOwnerSocketHandler}
		 */

		if (p2pInfo.isGroupOwner) {
			Log.d(TAG, "Connected as group owner");
			try {
				handler = new GroupOwnerSocketHandler(
						((MessageTarget) this).getHandler());
				handler.start();
			} catch (IOException e) {
				Log.d(TAG,
						"Failed to create a server thread - " + e.getMessage());
				return;
			}
		} else {
			Log.d(TAG, "Connected as peer");
			handler = new ClientSocketHandler(
					((MessageTarget) this).getHandler(),
					p2pInfo.groupOwnerAddress);
			handler.start();
		}
		chatFragment = new WifiChatFragment();
		getFragmentManager().beginTransaction()
				.replace(R.id.container_root, chatFragment).commit();
		statusTxtView.setVisibility(View.GONE);
	}

	public void appendStatus(String status) {
		String current = statusTxtView.getText().toString();
		statusTxtView.setText(current + "\n" + status);
	}

	@SuppressLint("NewApi")
	private void discoverService() {

		/*
		 * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */

		manager.setDnsSdResponseListeners(mChannel,
				new DnsSdServiceResponseListener() {

					@Override
					public void onDnsSdServiceAvailable(String instanceName,
							String registrationType, WifiP2pDevice srcDevice) {

						// A service has been discovered. Is this our app?

						if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

							// update the UI and add the item the discovered
							// device.
							WifiDirectServicesList fragment = (WifiDirectServicesList) getFragmentManager()
									.findFragmentByTag("services");
							if (fragment != null) {
								WifiDeviceAdapter adapter = ((WifiDeviceAdapter) fragment
										.getListAdapter());
								WiFiP2pService service = new WiFiP2pService();
								service.device = srcDevice;
								service.instanceName = instanceName;
								service.serviceRegistration = registrationType;
								adapter.add(service);
								adapter.notifyDataSetChanged();
								Log.d(TAG, "onBonjourServiceAvailable "
										+ instanceName);
							}
						}

					}
				}, new DnsSdTxtRecordListener() {

					/**
					 * A new TXT record is available. Pick up the advertised
					 * buddy name.
					 */
					@Override
					public void onDnsSdTxtRecordAvailable(
							String fullDomainName, Map<String, String> record,
							WifiP2pDevice device) {
						Log.d(TAG,
								device.deviceName + " is "
										+ record.get(TXTRECORD_PROP_AVAILABLE));
					}
				});

		// After attaching listeners, create a service request and initiate
		// discovery.
		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		manager.addServiceRequest(mChannel, serviceRequest,
				new ActionListener() {

					@Override
					public void onSuccess() {
						appendStatus("Added service discovery request");
					}

					@Override
					public void onFailure(int arg0) {
						appendStatus("Failed adding service discovery request");
					}
				});
		manager.discoverServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				appendStatus("Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				appendStatus("Service discovery failed");

			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MESSAGE_READ:
			byte[] readBuf = (byte[]) msg.obj;
			// construct a string from the valid bytes in the buffer
			String readMessage = new String(readBuf, 0, msg.arg1);
			Log.d(TAG, readMessage);
			(chatFragment).pushMessage("Buddy: " + readMessage);
			break;

		case MY_HANDLE:
			Object obj = msg.obj;
			(chatFragment).setChatManager((ChatManager) obj);

		}
		return true;
	}

	@SuppressLint("NewApi") @Override
	public void connectP2P(WiFiP2pService wiFiP2pService) {

		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = wiFiP2pService.device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		if (serviceRequest != null)
			manager.removeServiceRequest(mChannel, serviceRequest,
					new ActionListener() {

						@Override
						public void onSuccess() {
						}

						@Override
						public void onFailure(int arg0) {
						}
					});

		manager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				appendStatus("Connecting to service");
			}

			@Override
			public void onFailure(int errorCode) {
				appendStatus("Failed connecting to service");
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		receiver = new WifiBroadcastReceiver(manager, mChannel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}
}
