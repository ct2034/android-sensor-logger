package at.feichtinger.sensorlogger.activities;

import android.os.Bundle;
import at.feichtinger.sensorlogger.R;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
