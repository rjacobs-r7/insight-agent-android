package be.robinj.rapid7.insight.agent.streamreader;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import be.robinj.rapid7.insight.agent.LogLine;
import be.robinj.rapid7.insight.agent.logentries.LogentriesConnection;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class EventStreamReader implements StreamReader {
	private final InputStream stream;
	private final UUID logToken;

	public EventStreamReader(final InputStream stream, final UUID logToken) {
		this.stream = stream;
		this.logToken = logToken;
	}

	@Override
	public void run() {
		final LogentriesConnection connection = LogentriesConnection.getInstance();
		final ExecutorService threadPool = Executors.newFixedThreadPool(2);

		Observable.fromIterable(
				new BufferedReader(new InputStreamReader(this.stream)).lines()::iterator)
				.subscribeOn(Schedulers.from(threadPool))
				.map(LogLine::tryParse)
				.map(line -> this.logToken.toString() + " " + line + "\n")
				.buffer(1, TimeUnit.SECONDS)
				.map(batch -> batch.stream().collect(Collectors.joining()))
				.doOnNext(connection::write)
				.doOnNext(batch -> Log.v("Rapid7Insight", "Sent " + batch.getBytes(StandardCharsets.UTF_8).length + " bytes"))
				.doOnError(ex -> {
					Log.e("Rapid7Insight", ex.toString());
					ex.printStackTrace();
				})
				.doOnComplete(() -> Log.i("Rapid7Insight", "Stream closed"))
				.subscribe();

		Observable.interval(5, TimeUnit.SECONDS)
				.subscribeOn(Schedulers.from(threadPool))
				.map(x -> connection.retryFailed())
				.doOnNext(n -> Log.i("Rapid7Insight", "Re-sent " + n + " batches"))
				.doOnError(ex -> {
					Log.e("Rapid7Insight", ex.toString());
					ex.printStackTrace();
				})
				.subscribe();
	}
}
