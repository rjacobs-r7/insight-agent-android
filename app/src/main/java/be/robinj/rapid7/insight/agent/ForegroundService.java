package be.robinj.rapid7.insight.agent;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import be.robinj.rapid7.insight.agent.listener.ErrorListener;
import be.robinj.rapid7.insight.agent.listener.EventListener;

public class ForegroundService extends Service {
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("Rapid7Insight", "Insight agent started");
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent == null) {
			return super.onStartCommand(intent, flags, startId);
		} else if (intent.getAction().equals("ACTION_START_FOREGROUND_SERVICE")) {
			this.startForegroundService();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	private void startForegroundService() {
		final String channelId = "be.robinj.rapid7.insight.agent.main";
		final String channelName = "Main";

		final Intent intent = new Intent();
		final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		final Bitmap largeIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.r7);
		builder.setWhen(System.currentTimeMillis());
		builder.setLargeIcon(largeIconBitmap);
		builder.setSmallIcon(R.drawable.r7);
		builder.setPriority(Notification.PRIORITY_MAX);
		builder.setContentTitle("Rapid7 Insight");
		builder.setContentText("Insight Agent is monitoring this asset.");
		builder.setFullScreenIntent(pendingIntent, true);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			final NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
			chan.setLightColor(Color.BLUE);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
			final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			manager.createNotificationChannel(chan);

			builder.setChannelId(channelId);
		}

		final Notification notification = builder.build();

		this.startForeground(1, notification);

		try {
			this.startReadingLogs();
			this.startIngestingMetrics();
		} catch (final Exception ex) {
			MainActivity.setText(ex.toString());
		}
	}

	private void startReadingLogs() throws IOException {
		final Process p = this.exec("logcat -v time");
		final InputStream stdout = p.getInputStream();
		final InputStream stderr = p.getErrorStream();

		final StreamReader outReader = new StreamReader(stdout, new EventListener(MainActivity.LOG_TOKEN_LOGCAT));
		final StreamReader errReader = new StreamReader(stderr, new ErrorListener());

		final Thread thout = new Thread(outReader);
		final Thread therr = new Thread(errReader);

		thout.start();
		therr.start();
	}

	private void startIngestingMetrics() {
		new Thread(new MetricsIngestor()).start();
	}

	private Process exec(final String cmd) throws IOException {
		return Runtime.getRuntime().exec(cmd);
	}
}
