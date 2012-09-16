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

import java.awt.Color;
import java.awt.image.RenderedImage;

import javax.swing.JColorChooser;

/**
 * Acts as a wrapper for all functions that plugins may wish to invoke on the core.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public abstract class Core
{
	/**
	 * Gets the image currently displayed by the main window, whether it is completely calculated or not.
	 * 
	 * @return The image currently displayed by the main window.
	 */
	public static RenderedImage getImage()
	{
		if (MainFrame.getInstance() == null)
			return null;
		return MainFrame.getInstance().getCurrentProvider().getImage();
	}

	/**
	 * Gets the global <code>JColorChooser</code> of the JFractalizer.
	 * 
	 * @return The global <code>JColorChooser</code> of the JFractalizer.
	 */
	public static JColorChooser getGlobalColorChooser()
	{
		if (MainFrame.getInstance() == null)
			return null;
		return MainFrame.getInstance().colorChooser;
	}

	/**
	 * Stops the currently running calculation.
	 */
	public static void stopCalculation()
	{
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().getCurrentProvider().stopCalculation();
	}

	/**
	 * Starts calculation of the current fractal image.
	 */
	public static void startCalculation()
	{
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().getCurrentProvider().startCalculation();
	}

	/**
	 * Sets the text displayed by the status bar of the JFractalizer.
	 * 
	 * @param status
	 *            The new status.
	 */
	public static void setStatus(String status)
	{
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().setStatus(status, Color.black);
	}

	/**
	 * Sets the text displayed by the status bar of the JFractalizer and its color.
	 * 
	 * @param status
	 *            The new status.
	 * @param color
	 *            The color of the new status.
	 */
	public static void setStatus(String status, Color color)
	{
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().setStatus(status, color);
	}
}
