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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Menu;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;

public interface ColorPalette extends SelectableService {
	/**
	 * Gets the color that the palette assigns to the specified number of passes.
	 * 
	 * Implementations should note that this method can be called by multiple threads at the same time; <i>the caller
	 * does not take care of synchronization</i>. The implementation has to take care of potential synchronization
	 * issues itself. (Most implementations should only have to access the fast storage, which is (in most cases)
	 * thread-safe, so this shouldn't be too much of a problem.)
	 * 
	 * @param passes
	 * @return
	 */
	public Color getColor(int passes);

	public void saveFractXml(TransformerHandler handler) throws SAXException;

	public FractXmlPaletteLoader getFractXmlLoader();

	public void makeFastStorage();

	public void initMenu(Menu colorPaletteMenu, FractalProvider provider, Frame owner);

	/**
	 * If the JFractalizer was started with command line arguments, some of them are passed to the color palette (one by
	 * one) via this method.
	 * 
	 * @param args
	 *            A String that contains a single option.
	 */
	public void handleCommandLineOption(String option);
}
