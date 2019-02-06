package be.robinj.rapid7.insight.agent;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.UUID;

import be.robinj.rapid7.insight.agent.exception.LogReadPermissionNotGranted;
import be.robinj.rapid7.insight.agent.listener.ErrorListener;
import be.robinj.rapid7.insight.agent.listener.EventListener;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_READ_LOGS = "android.permission.READ_LOGS";

    private static final UUID logToken = UUID.fromString("a25faf2d-c591-4258-a151-b31b6858378e");

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

			if (!this.hasReadLogsPermission()) {
				throw new LogReadPermissionNotGranted();
			}

        	this.startReadingLogs();
		} catch (final Exception ex) {
			setText(ex.toString());
			ex.printStackTrace();
		}
    }

    private boolean hasReadLogsPermission() {
        return this.getPackageManager().checkPermission(PERMISSION_READ_LOGS, this.getPackageName())
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startReadingLogs() throws IOException {
		final Process p = this.exec("logcat -v time");
		final InputStream stdout = p.getInputStream();
		final InputStream stderr = p.getErrorStream();

		final StreamReader outReader = new StreamReader(stdout, new EventListener(logToken));
		final StreamReader errReader = new StreamReader(stderr, new ErrorListener());

		final Thread thout = new Thread(outReader);
		final Thread therr = new Thread(errReader);

		thout.start();
		therr.start();
	}

    private Process exec(final String cmd) throws IOException {
    	return Runtime.getRuntime().exec(cmd);
	}

	public static void setText(final String str) {
    	new Handler(looper).post(new Runnable() {
			@Override
			public void run() {
				tvMain.setText(str);
			}
		});
	}
}