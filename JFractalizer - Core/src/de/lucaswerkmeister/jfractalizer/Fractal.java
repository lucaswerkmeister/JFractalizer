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
package de.lucaswerkmeister.jfractalizer;

import java.awt.Canvas;
import java.awt.Menu;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

/**
 * A {@link Fractal} provides a fractal for the JFractalizer.
 * 
 * @author Lucas Werkmeister
 */
public interface Fractal extends SelectableService {
	/**
	 * Returns a {@link Canvas} that displays the fractal.
	 * <p>
	 * The JFractalizer will add a context menu; to amend to that, use
	 * {@link #initContextMenu(PopupMenu)}. Any other user interaction (select
	 * to zoom etc.) should be managed by the plugin.
	 * 
	 * @return A Canvas displaying the fractal.
	 */
	public Canvas getCanvas();

	/**
	 * Returns the current image.
	 * 
	 * @return The image.
	 */
	public BufferedImage getImage();

	public void saveFractXml(TransformerHandler handler) throws SAXException;

	public FractXmlLoader getFractXmlLoader();

	/**
	 * Stops the calculation of the fractal.
	 */
	public void stopCalculation();

	/**
	 * Sets the {@link ColorPalette} for the fractal.
	 * <p>
	 * This method shall never be called while the calculation is running.
	 * 
	 * @param newPalette
	 *            The new color palette.
	 */
	public void setColorPalette(ColorPalette newPalette);

	/**
	 * Starts the calculation.
	 * <p>
	 * This method shall never be called while the calculation is running.
	 */
	public void startCalculation();

	/**
	 * Adds entries to the Fractal menu on the menu bar.
	 * 
	 * @param fractalMenu
	 *            The menu.
	 */
	public void initMenu(Menu fractalMenu);

	/**
	 * Adds entries to the context menu.
	 * 
	 * @param contextMenu
	 *            The context menu.
	 */
	public void initContextMenu(PopupMenu contextMenu);

	/**
	 * By a call of this method, the JFractalizer informs the fractal provider
	 * that it was switched to on request from another Fractal, and
	 * passes any received arguments on to it. The Fractal may ignore
	 * this completely, if wanted.
	 * 
	 * @param params
	 *            The parameters that the other Fractal wished to pass
	 *            on to this Fractal.
	 */
	public void onProviderChange(Object... params);

	/**
	 * If the JFractalizer was started with command line arguments, some of them
	 * are passed to the fractal provider (one by one) via this method.
	 * 
	 * @param args
	 *            A string that contains a single option.
	 * @param optionName
	 *            The name of the option. Given purely for convenience.
	 * @param optionContent
	 *            The content of the option. Given purely for convenience.
	 */
	public void handleCommandLineOption(String option, String optionName,
			String optionContent);

	/**
	 * Blocks until calculation is either stopped or finished.
	 */
	public void awaitCalculation();

	/**
	 * Adds an {@link ActionListener} that will be notified when the calculation
	 * is finished.
	 * 
	 * @param listener
	 *            The action listener.
	 */
	public void addCalculationFinishedListener(ActionListener listener);
}
