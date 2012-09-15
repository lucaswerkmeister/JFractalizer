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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.mandelbrot;

import java.awt.Canvas;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.RenderedImage;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.Core;
import de.lucaswerkmeister.code.jfractalizer.FractXmlLoader;
import de.lucaswerkmeister.code.jfractalizer.FractalProvider;

public class MandelbrotProvider implements FractalProvider
{
	private MandelbrotCanvas		canvas;
	private MandelbrotMenuListener	menuListener;

	public MandelbrotProvider()
	{
		canvas = new MandelbrotCanvas();
		menuListener = new MandelbrotMenuListener(this);
	}

	@Override
	public Canvas getCanvas()
	{
		return canvas;
	}

	@Override
	public RenderedImage getImage()
	{
		return canvas.getImage();
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException
	{
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

	@Override
	public FractXmlLoader getFractXmlLoader()
	{
		return new MandelbrotFractXmlLoader();
	}

	public void setCanvas(final MandelbrotCanvas newCanvas)
	{
		canvas = newCanvas;
	}

	@Override
	public String getName()
	{
		return "Mandelbrot Set";
	}

	@Override
	public void stopCalculation()
	{
		canvas.stopCalculation();
	}

	@Override
	public void setColorPalette(final ColorPalette newPalette)
	{
		canvas.palette = newPalette;
	}

	@Override
	public void startCalculation()
	{
		canvas.start();
	}

	@Override
	public void initMenu(Menu fractalMenu)
	{
		MenuItem editBoundaries = new MenuItem("Edit boundaries...", new MenuShortcut(KeyEvent.VK_B));
		editBoundaries.addActionListener(menuListener);
		fractalMenu.add(editBoundaries);
		MenuItem additionalParams = new MenuItem("Edit additional parameters...", new MenuShortcut(KeyEvent.VK_A));
		additionalParams.addActionListener(menuListener);
		fractalMenu.add(additionalParams);
		MenuItem recalculate = new MenuItem("Recalculate", new MenuShortcut(KeyEvent.VK_R));
		recalculate.addActionListener(menuListener);
		fractalMenu.add(recalculate);
	}

	@Override
	public void initContextMenu(PopupMenu contextMenu)
	{
		MenuItem goToStart = new MenuItem("Show start image");
		final MandelbrotCanvas c = canvas;
		goToStart.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Core.stopCalculation();
				c.initDefaultValues();
				Core.startCalculation();
			}
		});
		contextMenu.add(goToStart);
	}

	@Override
	public void zoom(int x, int y, double factor)
	{
		double currentWidth = (canvas.getMaxReal() - canvas.getMinReal());
		double currentHeight = (canvas.getMaxImag() - canvas.getMinImag());
		double centerR = canvas.getMinReal() + currentWidth * ((double) x / canvas.getWidth());
		double centerI = canvas.getMinImag() + currentHeight * (1 - ((double) y / canvas.getHeight()));
		double halfSizeR = currentWidth * factor / 2;
		double halfSizeI = currentHeight * factor / 2;
		canvas.setMinReal(centerR - halfSizeR);
		canvas.setMaxReal(centerR + halfSizeR);
		canvas.setMinImag(centerI - halfSizeI);
		canvas.setMaxImag(centerI + halfSizeI);
		double maxPassesF = 1 / factor;
		maxPassesF = ((maxPassesF - 1) / MandelbrotCanvas.maxPassesFactor) + 1;
		canvas.setMaxPasses((int) Math.round(canvas.getMaxPasses() * maxPassesF));
	}
}
