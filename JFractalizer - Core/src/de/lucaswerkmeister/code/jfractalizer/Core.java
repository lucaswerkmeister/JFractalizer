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
	 * @return
	 */
	public static RenderedImage getImage()
	{
		return MainFrame.getInstance().getCurrentProvider().getImage();
	}

	/**
	 * Gets the global <code>JColorChooser</code> of the JFractalizer.
	 * 
	 * @return The global <code>JColorChooser</code> of the JFractalizer.
	 */
	public static JColorChooser getGlobalColorChooser()
	{
		return MainFrame.getInstance().colorChooser;
	}
}
