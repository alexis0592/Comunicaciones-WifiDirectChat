package co.edu.udea.comunicacionesapp.broadcast;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;
import co.edu.udea.comunicacionesapp.activities.WifiDirectActivity;

/**
 * Un BroadcastReceiver que escucha por eventos Wifi-Direct relacionados y pasa estos
 * a WifiDirectActivity y sus Fragments para la acción necesaria 
 * @author Yefry Alexis Calderón Yepes
 *
 */
public class WifiBroadcastReceiver extends BroadcastReceiver {

	private WifiP2pManager mManager;
	private Channel mChannel;
	private Activity mActivity;

	public WifiBroadcastReceiver(WifiP2pManager manager, Channel channel,
			WifiDirectActivity activity) {
		this.mManager = manager;
		this.mChannel = channel;
		this.mActivity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity

		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			if (mManager == null) {
				return;
			}

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {
				// Estamos conectados con el otro dispositivo, conección
				// requerida
				// información para encontrar grupos de propios IP
				Log.d(WifiDirectActivity.TAG,
						"Connected to p2p network. Requesting network details");
				mManager.requestConnectionInfo(mChannel,
						(ConnectionInfoListener) mActivity);
			} else {
				// Esta desconectado
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
			WifiP2pDevice device = (WifiP2pDevice) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			// TODO: POner lo del LOG por si quiero
		}
	}

}
