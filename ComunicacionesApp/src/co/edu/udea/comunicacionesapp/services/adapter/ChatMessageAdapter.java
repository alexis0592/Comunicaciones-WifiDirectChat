package co.edu.udea.comunicacionesapp.services.adapter;

import java.util.List;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<String> {

	private List<String> messages = null;
	Context context;
	List<String> items = null;

	public ChatMessageAdapter(Context context, int resource, List<String> items) {

		super(context, resource, items);
		this.context = context;
		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);

			v = vi.inflate(android.R.layout.simple_list_item_1, null);
		}
		String message = items.get(position);

		if (message != null && !message.isEmpty()) {
			TextView nameText = (TextView) v.findViewById(android.R.id.text1);

			if (nameText != null) {
				nameText.setText(message);
				if (message.startsWith("Me: ")) {
					nameText.setTextAppearance(context.getApplicationContext(),
							co.edu.udea.comunicacionesapp.R.style.normalText);
				} else {
					nameText.setTextAppearance(context.getApplicationContext(),
							co.edu.udea.comunicacionesapp.R.style.boldText);
				}
			}
		}

		return super.getView(position, convertView, parent);

	}

}
