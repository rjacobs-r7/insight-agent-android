package be.robinj.rapid7.insight.agent.logentries;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.UUID;

public class LogentriesConnection {
	private Socket socket;

	private static LogentriesConnection instance;

	private LinkedList<String> retry = new LinkedList<>();

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
		final String line = logToken.toString() + " " + str + "\n";

		this.write(line);
	}

	public synchronized void write(final String str) throws IOException {
		try {
			this.openConnection();

			final byte[] bytes = this.stringToBytes(str);

			final OutputStream stream = this.socket.getOutputStream();
			stream.write(bytes);
			stream.flush();
		} catch (final SocketException ex) {
			this.socket = null;
			ex.printStackTrace();

			this.retry.add(str);
		}
	}

	public synchronized int retryFailed() throws IOException {
		int retried = 0;

		while (! this.retry.isEmpty()) {
			this.write(this.retry.remove());
			retried++;
		}

		return retried;
	}

	private byte[] stringToBytes(final String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}
}
