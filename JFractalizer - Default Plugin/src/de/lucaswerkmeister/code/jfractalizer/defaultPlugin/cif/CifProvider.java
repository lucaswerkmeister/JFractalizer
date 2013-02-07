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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.cif;

import java.awt.Canvas;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.Core;
import de.lucaswerkmeister.code.jfractalizer.FractalProvider;

public abstract class CifProvider implements FractalProvider {
	CifCanvas<?> canvas;
	CifMenuListener menuListener;
	MenuItem undoMenuItem, redoMenuItem;

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public BufferedImage getImage() {
		return canvas.getImage();
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException {
		final Attributes noAtts = new AttributesImpl();
		final AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "canonicalName", "CDATA", getClass().getCanonicalName());
		handler.startElement("", "", "provider", atts);

		handler.startElement("", "", "width", noAtts);
		final char[] width = Integer.toString(canvas.getWidth()).toCharArray();
		handler.characters(width, 0, width.length);
		handler.endElement("", "", "width");

		handler.startElement("", "", "height", noAtts);
		final char[] height = Integer.toString(canvas.getHeight()).toCharArray();
		handler.characters(height, 0, height.length);
		handler.endElement("", "", "height");

		handler.startElement("", "", "minReal", noAtts);
		final char[] minReal = Double.toString(canvas.getMinReal()).toCharArray();
		handler.characters(minReal, 0, minReal.length);
		handler.endElement("", "", "minReal");

		handler.startElement("", "", "maxReal", noAtts);
		final char[] maxReal = Double.toString(canvas.getMaxReal()).toCharArray();
		handler.characters(maxReal, 0, maxReal.length);
		handler.endElement("", "", "maxReal");

		handler.startElement("", "", "minImag", noAtts);
		final char[] minImag = Double.toString(canvas.getMinImag()).toCharArray();
		handler.characters(minImag, 0, minImag.length);
		handler.endElement("", "", "minImag");

		handler.startElement("", "", "maxImag", noAtts);
		final char[] maxImag = Double.toString(canvas.getMaxImag()).toCharArray();
		handler.characters(maxImag, 0, maxImag.length);
		handler.endElement("", "", "maxImag");

		handler.startElement("", "", "maxPasses", noAtts);
		final char[] maxPasses = Integer.toString(canvas.getMaxPasses()).toCharArray();
		handler.characters(maxPasses, 0, maxPasses.length);
		handler.endElement("", "", "maxPasses");

		handler.startElement("", "", "superSamplingFactor", noAtts);
		final char[] superSamplingFactor = Byte.toString(canvas.getSuperSamplingFactor()).toCharArray();
		handler.characters(superSamplingFactor, 0, superSamplingFactor.length);
		handler.endElement("", "", "superSamplingFactor");

		canvas.getPalette().saveFractXml(handler);

		handler.endElement("", "", "provider");
	}

	public void setCanvas(final CifCanvas<?> newCanvas) {
		canvas = newCanvas;
	}

	@Override
	public void stopCalculation() {
		canvas.stopCalculation();
	}

	@Override
	public void setColorPalette(final ColorPalette newPalette) {
		canvas.palette = newPalette;
	}

	@Override
	public void startCalculation() {
		canvas.start();
	}

	@Override
	public void initMenu(final Menu fractalMenu) {
		final MenuItem recalculate = new MenuItem("Recalculate", new MenuShortcut(KeyEvent.VK_R));
		recalculate.addActionListener(menuListener);
		fractalMenu.add(recalculate);
		fractalMenu.addSeparator();
		final MenuItem editBoundaries = new MenuItem("Edit boundaries...", new MenuShortcut(KeyEvent.VK_E));
		editBoundaries.addActionListener(menuListener);
		fractalMenu.add(editBoundaries);
		final MenuItem additionalParams = new MenuItem("Edit additional parameters...", new MenuShortcut(KeyEvent.VK_A));
		additionalParams.addActionListener(menuListener);
		fractalMenu.add(additionalParams);
		fractalMenu.addSeparator();
		undoMenuItem = new MenuItem("Undo", new MenuShortcut(KeyEvent.VK_Z));
		undoMenuItem.addActionListener(menuListener);
		undoMenuItem.setEnabled(canvas.history.canUndo());
		fractalMenu.add(undoMenuItem);
		redoMenuItem = new MenuItem("Redo", new MenuShortcut(KeyEvent.VK_Y));
		redoMenuItem.addActionListener(menuListener);
		redoMenuItem.setEnabled(canvas.history.canRedo());
		fractalMenu.add(redoMenuItem);
	}

	@Override
	public void initContextMenu(final PopupMenu contextMenu) {
		final MenuItem goToStart = new MenuItem("Show start image");
		final CifCanvas<?> c = canvas;
		goToStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Core.stopCalculation();
				c.initDefaultValues();
				Core.startCalculation();
			}
		});
		contextMenu.add(goToStart);
	}

	@Override
	public void zoom(final int x, final int y, final double factor) {
		final double currentWidth = (canvas.getMaxReal() - canvas.getMinReal());
		final double currentHeight = (canvas.getMaxImag() - canvas.getMinImag());
		final double centerR = canvas.getMinReal() + currentWidth * ((double) x / canvas.getWidth());
		final double centerI = canvas.getMinImag() + currentHeight * (1 - ((double) y / canvas.getHeight()));
		final double halfSizeR = currentWidth * factor / 2;
		final double halfSizeI = currentHeight * factor / 2;
		canvas.setMinReal(centerR - halfSizeR);
		canvas.setMaxReal(centerR + halfSizeR);
		canvas.setMinImag(centerI - halfSizeI);
		canvas.setMaxImag(centerI + halfSizeI);
		double maxPassesF = 1 / factor;
		maxPassesF = ((maxPassesF - 1) / CifCanvas.maxPassesFactor) + 1;
		canvas.setMaxPasses((int) Math.round(canvas.getMaxPasses() * maxPassesF));
	}

	@Override
	public void awaitCalculation() {
		canvas.awaitCalculation();
	}
}
