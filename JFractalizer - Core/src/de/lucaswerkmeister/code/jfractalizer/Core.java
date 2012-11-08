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
import java.io.File;
import java.io.IOException;

import javax.swing.JColorChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Acts as a wrapper for all functions that plugins may wish to invoke on the core.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public final class Core
{
	/**
	 * <code>private</code> constructor so the class can't be instantiated.
	 */
	private Core()
	{
	}

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
	public static void setStatus(final String status)
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
	public static void setStatus(final String status, final Color color)
	{
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().setStatus(status, color);
	}

	/**
	 * Changes the current provider to the specified type.
	 * 
	 * @param fractalProviderClass
	 *            The class of the new provider type.
	 * @param params
	 *            Any parameters that will be passed to the new provider.
	 * @throws ReflectiveOperationException
	 *             If anything goes wrong instantiating the new provider.
	 */
	public static void changeProvider(Class<? extends FractalProvider> fractalProviderClass, Object... params) throws ReflectiveOperationException,
			IllegalArgumentException
	{
		stopCalculation();
		MainFrame.getInstance().setCurrentProvider(fractalProviderClass.newInstance());
		MainFrame.getInstance().getCurrentProvider().onProviderChange(params);
		startCalculation();
	}

	/**
	 * Loads the specified file.
	 * 
	 * @param saveFile
	 *            The file that should be loaded.
	 * @throws SAXException
	 *             If anything goes wrong while loading the file.
	 * @throws IOException
	 *             If anything goes wrong while loading the file.
	 * @throws ParserConfigurationException
	 *             If anything goes wrong while loading the file.
	 */
	public static void loadFile(File saveFile) throws SAXException, IOException, ParserConfigurationException
	{
		MainFrame.getInstance().loadFile(saveFile);
	}
}
