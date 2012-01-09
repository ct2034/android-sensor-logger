package at.feichtinger.sensorlogger.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import at.feichtinger.sensorlogger.R;
import at.feichtinger.sensorlogger.services.FTPUploadService;

public class FilesActivity extends ListActivity {
	private static final String TAG = "at.feichtinger.sensorlogger.activities.FilesActivity";

	/* ********************************************************************
	 * Fields
	 */
	private File[] allFiles;

	/* ********************************************************************
	 * UI stuff
	 */

	/** The button which will start the send process. */
	private Button sendButton;

	/** The button to delete all selected files. */
	private Button deleteButton;

	/** The list view of this activity. */
	private ListView listView;

	/**
	 * Listener for send files button. Gets a list of selected files and passes
	 * them to the upload service.
	 */
	private final OnClickListener sendButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			final List<String> paths = new ArrayList<String>();

			final int len = listView.getCount();
			final SparseBooleanArray checked = listView.getCheckedItemPositions();
			for (int i = 0; i < len; i++) {
				if (checked.get(i)) {
					paths.add(allFiles[i].getAbsolutePath());
				}
			}

			// a handler for receiving call-back messages from the FTP upload
			// service
			final Handler callbackHandler = new Handler() {
				@Override
				public void handleMessage(final Message msg) {
					// get status from intent
					final String status = msg.getData().getString(FTPUploadService.MESSAGE_STATUS);
					Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();

					updateFilesList();
				}
			};

			final Intent intent = new Intent(getApplicationContext(), FTPUploadService.class);
			intent.putExtra(FTPUploadService.FILE_PATHS, paths.toArray(new String[] {}));
			intent.putExtra(FTPUploadService.MESSENGER, new Messenger(callbackHandler));
			startService(intent);
		}
	};

	/**
	 * The listener for the delete files button. Displays an alert box to make
	 * sure the user really wants to delete the files. If he does, then all
	 * selected files will be deleted.
	 */
	private final OnClickListener deleteButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(final View v) {
			// show an alert box if the user really wants to delete the files
			// //TODO use string resources...
			new AlertDialog.Builder(FilesActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Sure?")
					.setMessage("Are you sure you want to delete the selected files?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(final DialogInterface dialog, final int which) {
							deleteSelectedFiles();
						}
					}).setNegativeButton("No", null).show();
		}

	};

	/** The list adapter. */
	private class ActivitiesAdapter extends ArrayAdapter<File> {

		public ActivitiesAdapter(final Context context, final File[] objects) {
			super(context, R.layout.file_list_item, objects);
		}

		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			if (convertView == null) {
				final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.file_list_item, null);
			}

			final File f = getItem(position);
			if (f != null) {
				// show the files name
				final TextView nameView = (TextView) convertView.findViewById(R.id.filelistitem_file_name);
				if (nameView != null) {
					nameView.setText(f.getName());
				}

				// show file size
				final TextView sizeView = (TextView) convertView.findViewById(R.id.filelistitem_file_size);
				if (sizeView != null) {
					sizeView.setText(f.length() / 1024 + "kB");
				}

				// show a the creation date
				final TextView dateView = (TextView) convertView.findViewById(R.id.filelistitem_file_date);
				if (dateView != null) {
					final DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
					final String date = dateFormat.format(new Date(f.lastModified()));
					dateView.setText(date);
				}
			}
			return convertView;
		}
	}

	/* *****************************************************************************
	 * ConextMenu methods
	 */

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.filesactivity_listview_contextmenu, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.contexmenu_open:
			openFile(allFiles[(int) info.id]);
			break;
		case R.id.contextmenu_delete:
			deleteFile(allFiles[(int) info.id]);
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	/* *******************************************************************
	 * Activity life-cycle methods
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filesactivity);

		// init ui elements
		sendButton = (Button) findViewById(R.id.files_sendSelectedFilesButton);
		sendButton.setOnClickListener(sendButtonClickListener);

		deleteButton = (Button) findViewById(R.id.files_deleteSelectedFilesButton);
		deleteButton.setOnClickListener(deleteButtonClickListener);

		listView = getListView();
		listView.setItemsCanFocus(false);

		// activate the context menu for this list
		registerForContextMenu(listView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateFilesList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/* *****************************************************************************
	 * Private helper methods
	 */

	private void openFile(final File file) {
		final Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		final Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "text/plain");
		startActivity(intent);
	}

	private void deleteFile(final File file) {
		file.delete();
		updateFilesList();
	}

	private void deleteSelectedFiles() {
		final int len = listView.getCount();
		for (int i = 0; i < len; i++) {
			final SparseBooleanArray checked = listView.getCheckedItemPositions();
			if (checked.get(i)) {
				allFiles[i].delete();
			}
		}
		updateFilesList();
	}

	private void updateFilesList() {
		allFiles = getExternalFilesDir(null).listFiles();
		setListAdapter(new ActivitiesAdapter(this, allFiles));
	}
}