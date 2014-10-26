package co.edu.udea.comunicacionesapp.activities.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import co.edu.udea.comunicacionesapp.R;
import co.edu.udea.comunicacionesapp.services.ChatManager;
import co.edu.udea.comunicacionesapp.services.adapter.ChatMessageAdapter;

public class WifiChatFragment extends Fragment {

	private View view;
	private ChatManager chatManager;
	private TextView chatLine;
	private ListView listView;
	ChatMessageAdapter adapter = null;
	private List<String> items = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_chat, container, false);
		chatLine = (TextView) view.findViewById(R.id.txtChatLine);
		listView = (ListView) view.findViewById(android.R.id.list);
		adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
				items);

		listView.setAdapter(adapter);
		view.findViewById(R.id.button1).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if (chatManager != null) {
							chatManager.write(chatLine.getText().toString()
									.getBytes());
							pushMessage("Me: " + chatLine.getText().toString());
							chatLine.setText("");
							chatLine.clearFocus();
						}
					}
				});

		return view;
	}

	public interface MessageTarget {
		public Handler getHandler();
	}

	public void setChatManager(ChatManager obj) {
		chatManager = obj;
	}

	public void pushMessage(String readMessage) {
		adapter.add(readMessage);
		adapter.notifyDataSetChanged();
	}
}
