package be.robinj.rapid7.insight.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.robinj.rapid7.insight.agent.logentries.LogentriesConnection;

public class MetricsIngestor implements Runnable {
	private static final Pattern PATTERN_CPU_STAT = Pattern.compile("^(cpu\\d*)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)$");

	@Override
	public void run() {
		while (true) {
			try {
				LogentriesConnection.getInstance().ingest(MainActivity.LOG_TOKEN_SYSTEM_INFO, collectMetrics().toString());

				Thread.sleep(1_000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Metric collectMetrics() throws IOException {
		final Metric metric = new Metric();

		// CPU
		final BufferedReader reader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("cat /proc/stat").getInputStream()));
		String line = null;

		while ((line = reader.readLine()) != null) {
			final Matcher procStatMatcher = PATTERN_CPU_STAT.matcher(line);
			if (procStatMatcher.find()) {
				final CPUMetric cpuMetric = new CPUMetric();

				cpuMetric.cpu = procStatMatcher.group(1);
				cpuMetric.cpuUser = Integer.parseInt(procStatMatcher.group(2));
				cpuMetric.cpuNice = Integer.parseInt(procStatMatcher.group(3));
				cpuMetric.cpuSystem = Integer.parseInt(procStatMatcher.group(4));
				cpuMetric.cpuIdle = Integer.parseInt(procStatMatcher.group(5));
				cpuMetric.cpuIOWait = Integer.parseInt(procStatMatcher.group(6));
				cpuMetric.cpuIRQ = Integer.parseInt(procStatMatcher.group(7));
				cpuMetric.cpuSoftIRQ = Integer.parseInt(procStatMatcher.group(8));

				metric.cpu.add(cpuMetric);
			} else if (line.startsWith("ctxt")) {
				metric.ctxt = Integer.parseInt(line.substring("ctxt".length() + 1));
			} else if (line.startsWith("btime")) {
				metric.btime = Integer.parseInt(line.substring("btime".length() + 1));
			} else if (line.startsWith("processes")) {
				metric.processes = Integer.parseInt(line.substring("processes".length() + 1));
			} else if (line.startsWith("procs_running")) {
				metric.procsRunning = Integer.parseInt(line.substring("procs_running".length() + 1));
			} else if (line.startsWith("procs_blocked")) {
				metric.procsBlocked = Integer.parseInt(line.substring("procs_blocked".length() + 1));
			}
		}

		return metric;
	}

	static class Metric {
		public List<CPUMetric> cpu = new ArrayList<>();

		public int ctxt = -1; // context switches

		public int btime = -1; // uptime

		public int processes = -1;
		public int procsRunning = -1;
		public int procsBlocked = -1;

		@Override
		public String toString() {
			final StringBuilder strBuilder = new StringBuilder()
				.append("context_switches=").append(this.ctxt)
				.append(" uptime=").append(this.btime)
				.append(" processes=").append(this.processes)
				.append(" processes_running=").append(this.procsRunning)
				.append(" processes_blocked=").append(this.procsBlocked);

			for (CPUMetric cpuMetric : this.cpu) {
				strBuilder.append(" ").append(cpuMetric.toString());
			}

			return strBuilder.toString();
		}
	}

	static class CPUMetric {
		public String cpu = null;
		public int cpuUser = -1;
		public int cpuNice = -1;
		public int cpuSystem = -1;
		public int cpuIdle = -1;
		public int cpuIOWait = -1;
		public int cpuIRQ = -1;
		public int cpuSoftIRQ = -1;

		@Override
		public String toString() {
			return new StringBuilder()
				.append(this.cpu).append("_user=").append(this.cpuUser)
				.append(" ").append(this.cpu).append("_nice=").append(this.cpuNice)
				.append(" ").append(this.cpu).append("_system=").append(this.cpuSystem)
				.append(" ").append(this.cpu).append("_idle=").append(this.cpuIdle)
				.append(" ").append(this.cpu).append("_iowait=").append(this.cpuIOWait)
				.append(" ").append(this.cpu).append("_irq=").append(this.cpuIRQ)
				.append(" ").append(this.cpu).append("_soft_irq=").append(this.cpuSoftIRQ)
				.toString();
		}
	}
}
