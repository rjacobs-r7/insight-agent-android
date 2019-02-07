package be.robinj.rapid7.insight.agent.streamreader;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import io.reactivex.Observable;

public class ErrorStreamReader implements StreamReader {
	private final InputStream stream;

	public ErrorStreamReader(final InputStream stream) {
		this.stream = stream;
	}

	@Override
	public void run() {
		Observable.fromIterable(
				new BufferedReader(new InputStreamReader(this.stream)).lines()::iterator)
				.doOnNext(error -> Log.e("Rapid7Insight", error));
	}
}
