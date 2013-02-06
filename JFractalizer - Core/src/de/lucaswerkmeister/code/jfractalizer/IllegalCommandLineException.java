/*
 * JFractalizer, a Java Fractal Program. Copyright (C) 2012 Lucas Werkmeister
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lucaswerkmeister.code.jfractalizer;

/**
 * Indicates that a command line was invalid.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class IllegalCommandLineException extends IllegalArgumentException {
	private static final long serialVersionUID = -21180174238251964L;

	/**
	 * Creates a new {@link IllegalCommandLineException} with the specified message.
	 * 
	 * @param message
	 *            The message.
	 */
	public IllegalCommandLineException(String message) {
		super(message);
	}

	/**
	 * Creates a new {@link IllegalCommandLineException} with the specified message and cause.
	 * 
	 * @param message
	 *            The message.
	 * @param cause
	 *            The cause.
	 */
	public IllegalCommandLineException(String message, Throwable cause) {
		super(message, cause);
	}
}