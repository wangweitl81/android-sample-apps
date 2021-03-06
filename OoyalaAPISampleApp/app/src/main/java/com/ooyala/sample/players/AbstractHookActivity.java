package com.ooyala.sample.players;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.ooyala.android.OoyalaNotification;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.android.util.SDCardLogcatOoyalaEventsLogger;

import java.util.Observable;
import java.util.Observer;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public abstract class AbstractHookActivity extends Activity implements Observer {
	private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
	final String TAG = this.getClass().toString();

	private final SDCardLogcatOoyalaEventsLogger log = new SDCardLogcatOoyalaEventsLogger();

	protected String embedCode;
	protected String pcode;
	protected String domain;
	protected OoyalaPlayerLayoutController playerLayoutController;
	protected OoyalaPlayerLayout playerLayout;

	OoyalaPlayer player;

	private boolean writePermission = false;
	boolean asked = false;

	// complete player setup after we asked for permission to write into external storage
	abstract void completePlayerSetup(final boolean asked);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
		} else {
			writePermission = true;
			asked = true;
		}

		embedCode = getIntent().getExtras().getString("embed_code");
		pcode = getIntent().getExtras().getString("pcode");
		domain = getIntent().getExtras().getString("domain");
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
			asked = true;
			if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
				writePermission = true;
			}
			completePlayerSetup(asked);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (null != player) {
			player.resume();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (player != null) {
			player.suspend();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		destroyPlayer();
	}

	private void destroyPlayer() {
		if (player != null) {
			player.destroy();
			player = null;
		}
		if (playerLayout != null) {
			playerLayout.release();
		}
		if (playerLayoutController != null) {
			playerLayoutController.destroy();
			playerLayoutController = null;
		}
	}


	@Override
	public void update(Observable arg0, Object argN) {
		if (arg0 != player) {
			return;
		}

		final String arg1 = OoyalaNotification.getNameOrUnknown(argN);
		if (arg1 == OoyalaPlayer.TIME_CHANGED_NOTIFICATION_NAME) {
			return;
		}

		if (arg1 == OoyalaPlayer.ERROR_NOTIFICATION_NAME) {
			final String msg = "Error Event Received";
			if (player != null && player.getError() != null) {
				Log.e(TAG, msg, player.getError());
			} else {
				Log.e(TAG, msg);
			}
			return;
		}

		// Automation Hook: to write Notifications to a temporary file on the device/emulator
		String text = "Notification Received: " + arg1 + " - state: " + player.getState();
		// Automation Hook: Write the event text along with event count to log file in sdcard if the log file exists
		log.writeToSdcardLog(text);

		Log.d(TAG, text);
	}
}
