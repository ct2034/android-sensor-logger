package at.feichtinger.sensorlogger.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import at.feichtinger.sensorlogger.R;

public class TabHostActivity extends TabActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final TabHost tabHost = getTabHost();
		Intent intent;
		TabSpec spec;

		intent = new Intent().setClass(this, HomeActivity.class);
		spec = tabHost.newTabSpec("logger").setIndicator(getResources().getString(R.string.logger_activity_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, FilesActivity.class);
		spec = tabHost.newTabSpec("files").setIndicator(getResources().getString(R.string.files_activity_tab))
				.setContent(intent);
		tabHost.addTab(spec);
	}
}