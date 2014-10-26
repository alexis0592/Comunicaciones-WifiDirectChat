package co.edu.udea.comunicacionesapp.services.adapter;

import java.util.List;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class WifiDeviceAdapter extends ArrayAdapter<WiFiP2pService> {

	private List<WiFiP2pService> items;
	Context context;

	public WifiDeviceAdapter(Context context, int resource,
			int textViewResourceId, List<WiFiP2pService> items) {
		super(context, resource, textViewResourceId, items);
		this.items = items;
		this.context = context;
	}

	public View geView(int position, View convertView, ViewGroup parents) {
		View view = convertView;

		if (view == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(
					android.R.layout.simple_expandable_list_item_2, null);
		}
		WiFiP2pService service = items.get(position);
		if (service != null) {
			TextView nameText = (TextView) view
					.findViewById(android.R.id.text1);
			if(nameText != null){
				nameText.setText(service.device.deviceName + "-" + service.instanceName);
			}
			TextView statusText = (TextView)view.findViewById(android.R.id.text2);
			statusText.setText(getDeviceStatus(service.device.status));

		}
		return view;
	}

	public static String getDeviceStatus(int statusCode) {
		switch (statusCode) {
		case WifiP2pDevice.CONNECTED:
			return "Connected";
		case WifiP2pDevice.INVITED:
			return "Invited";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";

		}
	}

}
