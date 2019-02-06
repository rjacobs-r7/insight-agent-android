package be.robinj.rapid7.insight.agent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import be.robinj.rapid7.insight.agent.listener.Listener;

public class StreamReader implements Runnable {
	private final InputStream stream;
	private final Listener listener;

	public StreamReader(final InputStream stream, final Listener listener) {
		this.stream = stream;
		this.listener = listener;
	}

	@Override
	public void run() {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(this.stream));
		String line = null;

		try {
			while ((line = reader.readLine()) != null) {
				this.listener.onEvent(line);
			}
		} catch (final Exception ex) {
			MainActivity.setText(ex.toString());
			ex.printStackTrace();
		}
	}
}
