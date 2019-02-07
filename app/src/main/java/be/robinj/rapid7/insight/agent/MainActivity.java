package be.robinj.rapid7.insight.agent;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import be.robinj.rapid7.insight.agent.exception.LogReadPermissionNotGranted;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_READ_LOGS = "android.permission.READ_LOGS";

	static final UUID LOG_TOKEN_LOGCAT = UUID.fromString("a25faf2d-c591-4258-a151-b31b6858378e");
	static final UUID LOG_TOKEN_SYSTEM_INFO = UUID.fromString("46d7b77b-a965-4937-89ab-2745d927cbbf");

    private static Looper looper;
    private static TextView tvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Custom timestamp structure ID: fa6a4440-4579-4a03-be08-c259a84db062

		try {
			looper = this.getMainLooper();
			tvMain = this.findViewById(R.id.tvMain);

			if (! Permission.hasBasicPermissions(this)) {
				Permission.requestBasicPermissions(this);
				return;
			}

			this.start();
		} catch (final Exception ex) {
			setText(ex.toString());
			ex.printStackTrace();
		}
    }

	private boolean hasReadLogsPermission() {
		return this.getPackageManager().checkPermission(PERMISSION_READ_LOGS, this.getPackageName())
				== PackageManager.PERMISSION_GRANTED;
	}

	public static void setText(final String str) {
    	new Handler(looper).post(() -> tvMain.setText(str));
	}

	private void start() throws LogReadPermissionNotGranted {
		if (!this.hasReadLogsPermission()) {
			throw new LogReadPermissionNotGranted("adb shell\npm grant be.robinj.rapid7.insight.agent android.permission.READ_LOGS");
		}

		final Intent intent = new Intent(MainActivity.this, ForegroundService.class);
		intent.setAction("ACTION_START_FOREGROUND_SERVICE");
		this.startService(intent);

		this.moveTaskToBack(true);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		try {
			this.start();
		} catch (final LogReadPermissionNotGranted ex) {
			ex.printStackTrace();
			tvMain.setText(ex.toString());
		}
	}

	@Override
	protected void onResume() {
    	super.onResume();

		Toast.makeText(this, "Insight Agent is running", Toast.LENGTH_SHORT).show();
	}
}
