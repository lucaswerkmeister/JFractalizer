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

import de.lucaswerkmeister.jfractalizer.ColorPalette;
import de.lucaswerkmeister.jfractalizer.FractXmlLoader;
import de.lucaswerkmeister.jfractalizer.SelectableService;

public interface FractalProvider extends SelectableService {
	public Canvas getCanvas();

	public BufferedImage getImage();

	public void saveFractXml(TransformerHandler handler) throws SAXException;

	public FractXmlLoader getFractXmlLoader();

	public void stopCalculation();

	public void setColorPalette(ColorPalette newPalette);

	public void startCalculation();

	public void initMenu(Menu fractalMenu);

	public void initContextMenu(PopupMenu contextMenu);

	/**
	 * Zooms the fractal to the specified center with the specified zoom factor.
	 * 
	 * @param x
	 *            The x coordinate of the point that is to become the new center, in pixels.
	 * @param y
	 *            The y coordinate of the point that is to become the new center, in pixels.
	 * @param factor
	 *            The zoom factor as (width of new area) / (width of old area). <code>1</code> means no zoom (the image
	 *            is centered on the new center), a greater factor means zoom out, a smaller factor means zoom in.
	 */
	public void zoom(int x, int y, double factor);

	/**
	 * By a call of this method, the JFractalizer informs the fractal provider that it was switched to on request from
	 * another FractalProvider, and passes any received arguments on to it. The FractalProvider may ignore this
	 * completely, if wanted.
	 * 
	 * @param params
	 *            The parameters that the other FractalProvider wished to pass on to this FractalProvider.
	 */
	public void onProviderChange(Object... params);

	/**
	 * If the JFractalizer was started with command line arguments, some of them are passed to the fractal provider (one
	 * by one) via this method.
	 * 
	 * @param args
	 *            A string that contains a single option.
	 */
	public void handleCommandLineOption(String option);

	/**
	 * Blocks until calculation is either stopped or finished.
	 */
	public void awaitCalculation();

	/**
	 * Adds an {@link ActionListener} that will be notified when the calculation is finished.
	 * 
	 * @param listener
	 *            The action listener.
	 */
	public void addCalculationFinishedListener(ActionListener listener);
}
