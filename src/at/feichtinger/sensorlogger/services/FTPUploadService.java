package at.feichtinger.sensorlogger.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTPClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import at.feichtinger.sensorlogger.R;

/**
 * A service implementation for uploading files to a FTP server.
 */
public class FTPUploadService extends IntentService {

	public FTPUploadService() {
		super("FTPUploadService");
	}

	/** The tag used for logging */
	private static final String TAG = "at.feichtinger.sensorlogger.services.FTPUploadService";

	/** The key for storing and retrieving URIs in the intent. */
	public final static String FILE_PATHS = "FTPUploadService.data";
	/** The key for storing and retrieving a Messenger instance from the intent */
	public static final String MESSENGER = "FTPUploadService.messenger";
	/** The key for storing and retrieving the status message */
	public static final String MESSAGE_STATUS = "message.string";

	/** The IP address of the FTP server to connect to. */
	private String server;
	/** The user name to use for uploading files. */
	private String username;
	/** The password of the FTP user. */
	private String password;

	@Override
	public void onCreate() {
		super.onCreate();

		// get FTP settings from settings file
		final SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		this.server = sharedPreferences.getString(getString(R.string.settings_key_ftp_server), "");
		this.username = sharedPreferences.getString(getString(R.string.settings_key_ftp_user), "");
		this.password = sharedPreferences.getString(getString(R.string.settings_key_ftp_password), "");
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
		final Bundle bundle = intent.getExtras();
		if (bundle != null) {
			// get the messenger object
			final Messenger messenger = (Messenger) bundle.get(MESSENGER);

			final String[] paths = intent.getStringArrayExtra(FILE_PATHS);
			final File[] files = new File[paths.length];
			for (int i = 0; i < paths.length; i++) {
				files[i] = new File(paths[i]);
			}

			if (files.length > 0) {
				uploadFiles(messenger, files);
			} else {
				// there is nothing to upload
				sendStatusToClient(messenger, getResources().getString(R.string.ftpuploadservice_no_files_selected));
			}
		}
	}

	/**
	 * Uploads all given files to the FTP server. Once a file has been uploaded
	 * it will be deleted from local storage.
	 * 
	 * @param messenger
	 *            a call-back messenger to inform the calling entity of the
	 *            current status.
	 * @param files
	 *            an array of files to be uploaded
	 */
	public void uploadFiles(final Messenger messenger, final File[] files) {
		Log.i(TAG, ".uploadFiles");

		// show a toast telling the user how many files are going to be uploaded
		sendStatusToClient(messenger, getQuantityString(R.plurals.ftpuploadservice_upload_selected_files, files.length));

		BufferedInputStream buffIn = null;
		int nrFilesSent = 0;
		try {
			final FTPClient ftpClient = connectToServer();

			for (final File file : files) {
				buffIn = new BufferedInputStream(new FileInputStream(file));
				final boolean result = ftpClient.storeFile(file.getName(), buffIn);
				buffIn.close();

				if (result) {
					nrFilesSent++;
					file.delete();

					// status: file was uploaded successfully
					final String format = getResources().getString(R.string.ftpuploadservice_file_upload_success);
					sendStatusToClient(messenger, String.format(format, file.getName()));
				} else {
					final String format = getResources().getString(R.string.ftpuploadservice_file_upload_error);
					sendStatusToClient(messenger, String.format(format, file.getName()));
				}
			}
			// close connection
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (final SocketException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (final UnknownHostException e) {
			Log.e(TAG, e.getMessage(), e);
		} catch (final IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		sendStatusToClient(messenger,
				getQuantityString(R.plurals.ftpuploadservice_upload_selected_files_finished, nrFilesSent));
	}

	private FTPClient connectToServer() throws SocketException, IOException, UnknownHostException {
		final FTPClient ftpClient = new FTPClient();
		ftpClient.connect(InetAddress.getByName(server));
		ftpClient.login(username, password);
		ftpClient.changeWorkingDirectory("./androiddata");
		return ftpClient;
	}

	private void sendStatusToClient(final Messenger messenger, final String status) {
		try {
			final Message message = Message.obtain();
			final Bundle data = new Bundle();
			data.putString(MESSAGE_STATUS, status);
			message.setData(data);
			messenger.send(message);
		} catch (final RemoteException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private String getQuantityString(final int resourceId, final int quantity) {
		final String formatString = getResources().getQuantityString(resourceId, quantity);
		final String text = String.format(formatString, quantity);
		return text;
	}
}
