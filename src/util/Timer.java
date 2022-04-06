package util;


import java.util.concurrent.TimeUnit;

public class Timer {
	long lastStartTime;
	long totalSpentTime;
	boolean isPaused;

	public Timer() {
		reset();
	}

	public long getTime() {
		// ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		// return bean.getCurrentThreadCpuTime();
		return System.nanoTime();
	}

	public void start() {
		restart();
	}
	
	public void restart() {
		lastStartTime = getTime();
		isPaused = false;
	}

	public void pause() {
		totalSpentTime += (getTime() - lastStartTime);
		isPaused = true;
	}

	public void reset() {
		isPaused = true;
		totalSpentTime = 0;
	}

	public long getSpentTimeInNanoseconds() {
		if (isPaused) {
			return totalSpentTime;
		} else {
			return totalSpentTime + getTime() - lastStartTime;
		}
	}

	public long getSpentTimeInSeconds() {
		return TimeUnit.SECONDS.convert(getSpentTimeInNanoseconds(),
				TimeUnit.NANOSECONDS);
	}
	
	private static String format2digits(int i) {
		if (i <= 9) {
			return "0" + i;
		} else {
			return i + "";
		}
	}

	public static String formatTime(long spentTime2) {
		int hours = 0, minutes = 0;
		int seconds = (int) spentTime2;

		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds = seconds % 60;

			if (minutes >= 60) {
				hours = minutes / 60;
				minutes = minutes % 60;
				return hours + "h" + format2digits(minutes) + "m"
						+ format2digits(seconds) + "s";
			} else {
				return minutes + "m" + format2digits(seconds) + "s";
			}
		} else {
			return seconds + "s";
		}
	}
}
