package be.robinj.rapid7.insight.agent.listener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import be.robinj.rapid7.insight.agent.LogLine;
import be.robinj.rapid7.insight.agent.MainActivity;

public class EventListener implements Listener {
	private Socket socket;
	private UUID logToken;

	public EventListener(final UUID logToken) {
		this.logToken = logToken;
	}

	@Override
	public void onEvent(String str) {
		try {
			this.openConnection();

			final LogLine logLine = new LogLine(str);

			final String line = this.logToken.toString() + " " + logLine.toString() + "\n";
			final byte[] bytes = this.stringToBytes(line);

			synchronized (this) {
				final OutputStream stream = this.socket.getOutputStream();
				stream.write(bytes);
				stream.flush();
			}

		} catch (final Exception ex) {
			MainActivity.setText(ex.toString());
			ex.printStackTrace();
		}
	}

	private byte[] stringToBytes(final String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

	private void openConnection() throws IOException {
		if (this.socket == null) {
			this.socket = new Socket("data.logentries.com", 10000);
		}
	}
}
