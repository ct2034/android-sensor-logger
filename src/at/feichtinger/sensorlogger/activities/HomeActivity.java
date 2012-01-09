package at.feichtinger.sensorlogger.activities;

import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import at.feichtinger.sensorlogger.R;

public class HomeActivity extends ListActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);

		final List<String> items = Arrays.asList(getResources().getStringArray(R.array.help_items));
		final ListAdapter adapter = new HelpListAdapter(this, R.layout.help_list_item, items);
		setListAdapter(adapter);

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