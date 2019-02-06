package be.robinj.rapid7.insight.agent;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class LogentriesConnection {
	private Socket socket;

	private static LogentriesConnection instance;

	private LogentriesConnection() {}

	public static synchronized LogentriesConnection getInstance() {
		if (instance == null) {
			instance = new LogentriesConnection();
		}

		return instance;
	}

	private synchronized void openConnection() throws IOException {
		if (this.socket == null || !this.socket.isConnected() || this.socket.isClosed()) {
			Log.i("Rapid7Insight", "Opening connection to InsightOps");
			this.socket = new Socket("data.logentries.com", 10000);
		}
	}

	public void ingest(final UUID logToken, final String str) throws IOException {
		this.openConnection();

		final String line = logToken.toString() + " " + str + "\n";
		final byte[] bytes = this.stringToBytes(line);

		synchronized (this) {
			final OutputStream stream = this.socket.getOutputStream();
			stream.write(bytes);
			stream.flush();
		}
	}

	private byte[] stringToBytes(final String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}
}
