package at.feichtinger.sensorlogger.activities;

import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import at.feichtinger.sensorlogger.R;

public class HomeActivity extends ListActivity {

	/** The list of help items. */
	private List<String> items;
	/** The list of help messages. */
	private List<String> messages;

	/**
	 * Listener for clicking on a help item. Displays a dialog showing the help
	 * message.
	 */
	final OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long id) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
			builder.setTitle(items.get(position)).setMessage(messages.get(position)).setCancelable(false)
					.setPositiveButton("Ok, got it.", new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int id) {
							dialog.cancel();
						}
					});
			builder.create().show();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

		items = Arrays.asList(getResources().getStringArray(R.array.help_items));
		messages = Arrays.asList(getResources().getStringArray(R.array.help_messages));
		setListAdapter(new HelpListAdapter(this, R.layout.help_list_item, items));

		getListView().setOnItemClickListener(onItemClickListener);

		final Button startButton = (Button) findViewById(R.id.startButton);
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				startActivity(new Intent(getBaseContext(), LoggerActivity.class));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.homeactivity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			final Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class HelpListAdapter extends ArrayAdapter<String> {
		private final List<String> items;

		public HelpListAdapter(final Context context, final int textViewResourceId, final List<String> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.help_list_item, null);
			}

			final String item = items.get(position);
			if (item != null) {
				final TextView textView = (TextView) convertView.findViewById(R.id.helplistitem_text);
				if (textView != null) {
					textView.setText(item);
				}
			}
			return convertView;
		}

	}
}