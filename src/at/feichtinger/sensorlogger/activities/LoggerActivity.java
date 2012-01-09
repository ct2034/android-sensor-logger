package at.feichtinger.sensorlogger.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import at.feichtinger.sensorlogger.R;
import at.feichtinger.sensorlogger.model.Activity;
import at.feichtinger.sensorlogger.services.SensorLoggerService;
import at.feichtinger.sensorlogger.services.SensorLoggerService.ServiceState;

public class LoggerActivity extends android.app.Activity {
	/** A string used to represent this class in log messages. */
	private final static String TAG = "at.feichtinger.sensorlogger.activities.LoggingActivity";

	/* *********************************************************************************
	 * Fields
	 */

	/** A messenger for receiving messages from the service. */
	private final Messenger callbackMessenger = new Messenger(new CallbackHandler());
	/** A messenger for sending messages to the service. */
	private Messenger sensorLoggerService;
	/** Indicates whether the service is bound to this activity. */
	private boolean mIsBound;

	/* *********************************************************************************
	 * UI stuff
	 */

	/** The grid which displays the activities. */
	private GridView activitiesGrid;
	/** The button for stopping the logging process. */
	private Button stopButton;
	/** A TextView for displaying status messages. */
	private TextView statusTextView;

	/**
	 * Adapter for activity grid.
	 */
	private class ActivityAdapter extends BaseAdapter {
		private final List<Activity> activities;

		public ActivityAdapter(final List<Activity> activities) {
			this.activities = activities;
		}

		@Override
		public int getCount() {
			return activities.size();
		}

		@Override
		public Object getItem(final int position) {
			return activities.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_item, null);
			}

			final Activity item = activities.get(position);
			;
			if (item != null) {
				final TextView textView = (TextView) convertView.findViewById(R.id.activityitem_text);
				if (textView != null) {
					textView.setText(getResources().getString(item.getStringResourceKey()));
				}

				final ImageView imageView = (ImageView) convertView.findViewById(R.id.activityitem_image);
				if (imageView != null) {
					imageView.setBackgroundDrawable(getResources().getDrawable(item.getDrawableResourceKey()));
				}
			}
			return convertView;
		}
	}

	/**
	 * Call-back handler class for communication with the SensorLoggerService
	 */
	private class CallbackHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			if (msg.what == SensorLoggerService.MSG_STATUS) {
				showStatus(msg.getData());
			}

		}
	}

	private final ServiceConnection sensorLoggerServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			sensorLoggerService = new Messenger(service);

			try {
				final Message message = Message.obtain(null, SensorLoggerService.MSG_REGISTER_CLIENT);
				message.replyTo = callbackMessenger;
				sensorLoggerService.send(message);
			} catch (final RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName name) {
			sensorLoggerService = null;
		}
	};

	private final OnItemClickListener onGridItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			try {
				final Message message = Message.obtain(null, SensorLoggerService.MSG_SET_ACTIVITY_AND_START_LOGGING);

				// put the selected activity into the message (a string
				// representation is sufficient)
				int stringResourceKey = ((Activity) activitiesGrid.getItemAtPosition(position)).getStringResourceKey();
				final Bundle bundle = new Bundle();
				bundle.putString(SensorLoggerService.DATA_ACTIVITY, getResources().getString(stringResourceKey));
				message.setData(bundle);

				sensorLoggerService.send(message);
			} catch (final RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	private final OnClickListener onStopButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			try {
				final Message message = Message.obtain(null, SensorLoggerService.MSG_STOP_LOGGING);
				sensorLoggerService.send(message);
			} catch (final RemoteException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
	};

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(sensorLoggerServiceConnection);
			mIsBound = false;
		}
	}

	private void doBindService() {
		bindService(new Intent(LoggerActivity.this, SensorLoggerService.class), sensorLoggerServiceConnection,
				Context.BIND_AUTO_CREATE | Service.START_STICKY);
		mIsBound = true;
	}

	private void showStatus(final Bundle data) {
		final ServiceState status = SensorLoggerService.ServiceState.valueOf(data
				.getString(SensorLoggerService.DATA_STATUS));
		if (status == ServiceState.LOGGING) {
			statusTextView.setText("Logging for activity: " + data.getString(SensorLoggerService.DATA_ACTIVITY));
		} else {
			statusTextView.setText("Pick an activity to start logging.");
		}
	}

	/* ***************************************************************
	 * Activity life-cycle methods
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logger);

		// initialize UI elements
		activitiesGrid = (GridView) findViewById(R.id.gridview);
		stopButton = (Button) findViewById(R.id.stopButton);
		statusTextView = (TextView) findViewById(R.id.statusTextView);

		activitiesGrid.setOnItemClickListener(onGridItemClickListener);
		stopButton.setOnClickListener(onStopButtonClickListener);

		final BaseAdapter adapter = new ActivityAdapter(Arrays.asList(Activity.values()));
		activitiesGrid.setAdapter(adapter);

		doBindService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
}