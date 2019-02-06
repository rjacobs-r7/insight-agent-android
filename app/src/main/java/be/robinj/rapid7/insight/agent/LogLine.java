package be.robinj.rapid7.insight.agent;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogLine {
	// ^(\d{2})\-(\d{2})\s([\d\:\.]+)\s+([VDIWEFS])\/([^\s]*)\s*\(\s*(\d+)\s*\)\s*\:\s*(.*)$
	/**
	 * Capture groups:
	 * month, day, time, level, component, PID, message
	 */
	private static final String LOG_REGEX = "^(\\d{2})\\-(\\d{2})\\s([\\d\\:\\.]+)\\s+([VDIWEFS])\\/([^\\s]*)\\s*\\(\\s*(\\d+)\\s*\\)\\s*\\:\\s*(.*)$";
	private static final Pattern LOG_PATTERN = Pattern.compile(LOG_REGEX);

	// pid, activity, uid, embryo
	private static final Pattern START_PROC_PATTERN = Pattern.compile("^Start proc\\s*(\\d+)\\:([^\\/]+)\\/[a-z0-9]+ for embryo ([^\\ ]+)$");
	// activity, etc, reason
	private static final Pattern FORCE_STOP_PATTERN = Pattern.compile("^Force stopping ([^\\s]+) (.+)\\:\\s*([^\\s]+)$");
	// pid, activity, uid, etc
	private static final Pattern KILL_PATTERN = Pattern.compile("^Killing (\\d+)\\:([^\\s\\/]+)\\/([a-z0-9]+)\\s(.*).+$");

	private final String original;
	private boolean parsed;

	private String timestamp;
	private char level;
	private String component;
	private short pid;
	private String message;

	public LogLine(final String str) {
		this.original = str;

		final Matcher matcher = LOG_PATTERN.matcher(str);

		try {
			this.parsed = matcher.find();
			if (! this.parsed) {
				throw new Exception("Failed to parse log line");
			}

			this.timestamp = this.parseTimestamp(
					matcher.group(1),
					matcher.group(2),
					matcher.group(3));
			this.level = matcher.group(4).charAt(0);
			this.component = matcher.group(5);
			this.pid = Short.parseShort(matcher.group(6));
			this.message = matcher.group(7);
		} catch (final Exception ex) {
			this.parsed = false;

			ex.printStackTrace();
		}
	}

	// ISO8601 2019-02-06T13:15:37Z
	private String parseTimestamp(final String month, final String day, final String time) {
		return (1900 + new Date().getYear()) + "-" + month + "-" + day + "T" + time.split("\\.", 1)[0] + "Z";
	}

	public String getOriginal() {
		return this.original;
	}

	public boolean isParsed() {
		return this.parsed;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public char getLevel() {
		return this.level;
	}

	/*
	 V: Verbose (lowest priority)
	 D: Debug
	 I: Info
	 W: Warning
	 E: Error
	 F: Fatal
	 S: Silent (highest priority, on which nothing is ever printed)
	 */
	public String getLevelName() {
		if (this.level == 'V') {
			return "VERBOSE";
		} else if (this.level == 'D') {
			return "DEBUG";
		} else if (this.level == 'I') {
			return "INFO";
		} else if (this.level == 'W') {
			return "WARNING";
		} else if (this.level == 'E') {
			return "ERROR";
		} else if (this.level == 'F') {
			return "FATAL";
		} else if (this.level == 'S') {
			return "SILENT";
		}

		return "";
	}

	public String getComponent() {
		return this.component;
	}

	public short getPid() {
		return this.pid;
	}

	public String getMessage() {
		try {
			final Matcher startProcMatcher = START_PROC_PATTERN.matcher(this.message);
			if (startProcMatcher.find()) {
				// pid, activity, uid, embryo
				return "action=start_proc pid=" + startProcMatcher.group(1) + " activity=" + startProcMatcher.group(2) + " uid=" + startProcMatcher.group(3) + " embryo=" + startProcMatcher.group(4);
			}

			final Matcher forceStopMatcher = FORCE_STOP_PATTERN.matcher(this.message);
			if (forceStopMatcher.find()) {
				// activity, etc, reason
				return "action=force_stop activity=" + forceStopMatcher.group(1) + " reason=" + forceStopMatcher.group(3) + " " + forceStopMatcher.group(2).trim();
			}

			final Matcher killMatcher = KILL_PATTERN.matcher(this.message);
			if (killMatcher.find()) {
				// pid, activity, uid
				return "action=kill pid=" + killMatcher.group(1) + " activity=" + killMatcher.group(2) + " uid=" + killMatcher.group(3) + " " + killMatcher.group(4);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return this.message;
	}

	@Override
	public String toString() {
		if (!this.parsed) {
			return this.getOriginal();
		}

		return this.getTimestamp() + " level=" + this.getLevelName() + " tag=" + this.getComponent() + " pid=" + this.getPid() + " " + this.getMessage();
	}
}
