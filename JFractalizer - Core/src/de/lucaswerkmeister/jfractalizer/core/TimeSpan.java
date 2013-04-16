package de.lucaswerkmeister.jfractalizer.core;

import java.util.Locale;

/**
 * Encapsulates a nanosecond-precision time span.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class TimeSpan {
	private final long	nanos;

	/**
	 * Creates a new {@link TimeSpan} with the specified duration.
	 * 
	 * @param nanos
	 *            The duration in nanoseconds.
	 */
	public TimeSpan(long nanos) {
		this.nanos = nanos;
	}

	/**
	 * Creates a new {@link TimeSpan} with the specified duration.
	 * 
	 * @param nanos
	 *            The duration in nanoseconds.
	 * @param locale
	 *            The locale that this {@link TimeSpan} should use for output. (Please note that the locale isn't
	 *            actually used, or even saved.)
	 */
	public TimeSpan(long nanos, Locale locale) {
		this(nanos);
	}

	public String toString() {
		long interval = (nanos + 500_000) / 1_000_000;
		final short milliseconds = (short) (interval % 1000);
		interval -= milliseconds;
		interval /= 1000;
		final byte seconds = (byte) (interval % 60);
		interval -= seconds;
		interval /= 60;
		final byte minutes = (byte) (interval % 60);
		interval -= minutes;
		interval /= 60;
		final byte hours = (byte) (interval % 24);
		interval -= hours;
		interval /= 24;
		final long days = interval;
		final StringBuilder ret = new StringBuilder();
		if (days != 0) {
			ret.append(days);
			ret.append(" day");
			if (days != 1)
				ret.append('s');
			ret.append(' ');
		}
		if (hours != 0) {
			ret.append(hours);
			ret.append(" hour");
			if (hours != 1)
				ret.append('s');
			ret.append(' ');
		}
		if (minutes != 0) {
			ret.append(minutes);
			ret.append(" minute");
			if (minutes != 1)
				ret.append('s');
			ret.append(' ');
		}
		if (seconds != 0) {
			ret.append(seconds);
			ret.append(" second");
			if (seconds != 1)
				ret.append('s');
			ret.append(' ');
		}
		if (milliseconds != 0) {
			ret.append(milliseconds);
			ret.append(" millisecond");
			if (milliseconds != 1)
				ret.append('s');
			ret.append(' ');
		}
		boolean braces = ret.length() > 0;
		if (braces)
			ret.append('(');
		ret.append(nanos);
		ret.append(" ns");
		if (braces)
			ret.append(")");
		return ret.toString();
	}
}
