package de.lucaswerkmeister.jfractalizer.core;

import java.util.Locale;

/**
 * Encapsulates a nanosecond-precision time span.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class TimeSpan {
	/**
	 * Creates a new {@link TimeSpan} with the specified duration.
	 * 
	 * @param nanos
	 *            The duration in nanoseconds.
	 */
	public TimeSpan(long nanos) {

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
}
