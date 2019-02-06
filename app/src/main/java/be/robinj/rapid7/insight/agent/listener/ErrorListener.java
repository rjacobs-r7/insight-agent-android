package be.robinj.rapid7.insight.agent.listener;

import be.robinj.rapid7.insight.agent.MainActivity;

public class ErrorListener implements Listener {
	@Override
	public void onEvent(String str) {
		MainActivity.setText(str);
	}
}
