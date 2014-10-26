package co.edu.udea.comunicacionesapp.services;

import java.util.ArrayList;

import co.edu.udea.comunicacionesapp.R;
import co.edu.udea.comunicacionesapp.services.adapter.WiFiP2pService;
import co.edu.udea.comunicacionesapp.services.adapter.WifiDeviceAdapter;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Un simple LIstFragment que muestra los servicios disponibles como publicados
 * por los dispositivos
 * 
 * @author Yefry Alexis Calderon Yepes
 * 
 */
public class WifiDirectServicesList extends ListFragment {

	WifiDeviceAdapter wifiDeviceAdapter = null;

	public interface DeviceClickListener {
		public void connectP2P(WiFiP2pService wiFiP2pService);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.device_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		wifiDeviceAdapter = new WifiDeviceAdapter(this.getActivity(),
				android.R.layout.simple_expandable_list_item_2,
				android.R.id.text1, new ArrayList<WiFiP2pService>());
		setListAdapter(wifiDeviceAdapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		((DeviceClickListener) getActivity()).connectP2P((WiFiP2pService) l
				.getItemAtPosition(position));
		((TextView)v.findViewById(android.R.id.text2)).setText("Conectando");
	}
}
