/*
 * JFractalizer, a Java Fractal Program. Copyright (C) 2012 Lucas Werkmeister
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;
import de.lucaswerkmeister.jfractalizer.framework.ZoomableFractal;

public abstract class CifFractal implements ZoomableFractal {
	CifCanvas<?>			canvas;
	CifMenuListener			menuListener;
	MenuItem				undoMenuItem, redoMenuItem;
	public static final int	LOG_CLASS_PREFIX		= DefaultPlugin.LOG_PLUGIN_PREFIX + (((0 << 5) + (0 << 0)) << 8);
	public static final int	LOG_CHANGED_IMAGE_TYPE	= LOG_CLASS_PREFIX + 0;
	public static final int	LOG_SAVING				= LOG_CLASS_PREFIX + 1;
	public static final int	LOG_SET_PALETTE			= LOG_CLASS_PREFIX + 2;
	public static final int	LOG_START_CALCULATION	= LOG_CLASS_PREFIX + 3;
	public static final int	LOG_STOP_CALCULATION	= LOG_CLASS_PREFIX + 4;
	public static final int	LOG_INIT_MENU			= LOG_CLASS_PREFIX + 5;
	public static final int	LOG_ZOOM				= LOG_CLASS_PREFIX + 6;
	public static final int	LOG_ZOOM_TO_START		= LOG_CLASS_PREFIX + 7;
	public static final int	LOG_SHUTDOWN			= LOG_CLASS_PREFIX + 8;

	@Override
	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public BufferedImage getImage() {
		return canvas.getImage();
	}

	@Override
	public void suggestImageType(int imageType) {
		log(LOG_CHANGED_IMAGE_TYPE, this, imageType);
		canvas.setImageType(imageType);
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException {
		log(LOG_SAVING, this);

		final Attributes noAtts = new AttributesImpl();

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
	}

	public void setCanvas(final CifCanvas<?> newCanvas) {
		canvas = newCanvas;
	}

	@Override
	public void stopCalculation() {
		log(LOG_STOP_CALCULATION, this);
		canvas.stopCalculation();
	}

	@Override
	public void setColorPalette(final ColorPalette newPalette) {
		log(LOG_SET_PALETTE, this, canvas.palette, newPalette);
		canvas.palette = newPalette;
	}

	@Override
	public void startCalculation() {
		log(LOG_START_CALCULATION, this);
		canvas.start();
	}

	@Override
	public void initMenu(final Menu fractalMenu) {
		log(LOG_INIT_MENU, this, fractalMenu);

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
		// do nothing
	}

	@Override
	public void zoom(final int x, final int y, final double factor) {
		log(LOG_ZOOM, this, x, y, factor);

		final double currentWidth = (canvas.getMaxReal() - canvas.getMinReal());
		final double currentHeight = (canvas.getMaxImag() - canvas.getMinImag());
		final double centerR = canvas.getMinReal() + currentWidth * ((double) x / canvas.getImageSize().width);
		final double centerI = canvas.getMinImag() + currentHeight * (1 - ((double) y / canvas.getImageSize().height));
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
	public void zoomToStart(int x, int y, double factor) {
		log(LOG_ZOOM_TO_START, this, x, y, factor);

		zoom(x, y, factor);
		Rectangle2D.Double start = getStartArea();
		double realSize = canvas.getMaxReal() - canvas.getMinReal();
		double imagSize = canvas.getMaxImag() - canvas.getMinImag();
		final boolean CENTER_WHEN_BIGGER = false; // Disabled
		if (realSize < start.width) {
			// move horizontally
			if (canvas.getMaxReal() > start.getMaxX()) {
				// clamp to east edge
				canvas.setMaxReal(start.getMaxX());
				canvas.setMinReal(start.getMaxX() - realSize);
			}
			else if (canvas.getMinReal() < start.getMinX()) {
				// clamp to west edge
				canvas.setMinReal(start.getMinX());
				canvas.setMaxReal(start.getMinX() + realSize);
			}
		}
		else if (CENTER_WHEN_BIGGER) {
			// center horizontally
			canvas.setMinReal(start.getCenterX() - realSize / 2);
			canvas.setMaxReal(start.getCenterX() + realSize / 2);
		}
		if (imagSize < start.height) {
			// move vertically
			if (canvas.getMaxImag() > start.getMaxY()) {
				// clamp to north edge
				canvas.setMaxImag(start.getMaxY());
				canvas.setMinImag(start.getMaxY() - imagSize);
			}
			else if (canvas.getMinImag() < start.getMinY()) {
				// clamp to south edge
				canvas.setMinImag(start.getMinY());
				canvas.setMaxImag(start.getMinY() + imagSize);
			}
		}
		else if (CENTER_WHEN_BIGGER) {
			// center vertically
			canvas.setMinImag(start.getCenterY() - imagSize / 2);
			canvas.setMaxImag(start.getCenterY() + imagSize / 2);
		}
	}

	@Override
	public double getZoomFactor() {
		Rectangle2D.Double start = getStartArea();
		double xZoomFactor = start.width / (canvas.getMaxReal() - canvas.getMinReal());
		double yZoomFactor = start.height / (canvas.getMaxImag() - canvas.getMinImag());
		return Math.max(xZoomFactor, yZoomFactor);
	}

	@Override
	public void awaitCalculation() {
		canvas.awaitCalculation();
	}

	@Override
	public void addCalculationFinishedListener(final ActionListener listener) {
		new Thread() {
			public void run() {
				awaitCalculation();
				listener.actionPerformed(null);
			}
		}.start();
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		CifParams params = canvas.getParams();
		switch (optionName) {
			case "width":
				canvas.setImageSize(new Dimension(Integer.parseInt(optionContent), canvas.getImageSize().height));
				return;
			case "height":
				canvas.setImageSize(new Dimension(canvas.getImageSize().width, Integer.parseInt(optionContent)));
				return;
			case "minReal":
				params = params.copyChangeMinReal(Double.parseDouble(optionContent));
				break;
			case "maxReal":
				params = params.copyChangeMaxReal(Double.parseDouble(optionContent));
				break;
			case "minImag":
				params = params.copyChangeMinImag(Double.parseDouble(optionContent));
				break;
			case "maxImag":
				params = params.copyChangeMaxImag(Double.parseDouble(optionContent));
				break;
			case "maxPasses":
				params = params.copyChangeMaxPasses(Integer.parseInt(optionContent));
				break;
			case "superSamplingFactor":
				params = params.copyChangeSuperSamplingFactor(Byte.parseByte(optionContent));
				break;
			default:
				throw new IllegalCommandLineException(
						"Unknown option \""
								+ optionName
								+ "\" for fractal "
								+ getClass().getSimpleName()
								+ "! Known options: width, height, minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor");
		}
		canvas.setParams(params, false);
	}

	/**
	 * Determines the area that should be included in the start area.
	 * <p>
	 * Due to different aspect ratios, the actual starting area will most likely be a superarea (never a subarea, or a
	 * partially disjunct area) of this area.
	 * 
	 * @return A double-precision rectangle specifying the start area; x axis is real, and y axis is imaginary.
	 */
	protected abstract java.awt.geom.Rectangle2D.Double getStartArea();

	@Override
	public void shutdown() {
		log(LOG_SHUTDOWN, this);
		canvas.shutdown();
	}
}
