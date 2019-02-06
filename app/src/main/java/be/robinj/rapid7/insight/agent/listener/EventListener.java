package be.robinj.rapid7.insight.agent.listener;

import java.util.UUID;

import be.robinj.rapid7.insight.agent.LogLine;
import be.robinj.rapid7.insight.agent.LogentriesConnection;
import be.robinj.rapid7.insight.agent.MainActivity;

public class EventListener implements Listener {

	private final UUID logToken;
	private final LogentriesConnection connection;

	public EventListener(final UUID logToken) {
		this.logToken = logToken;
		this.connection = LogentriesConnection.getInstance();
	}

	@Override
	public void onEvent(String str) {
		try {
			this.connection.ingest(this.logToken, new LogLine(str).toString());
		} catch (final Exception ex) {
			MainActivity.setText(ex.toString());
			ex.printStackTrace();
		}
	}
}
