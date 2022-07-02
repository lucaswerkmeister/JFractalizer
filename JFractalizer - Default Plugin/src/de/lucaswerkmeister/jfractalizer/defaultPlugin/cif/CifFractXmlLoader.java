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

import java.awt.Dimension;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlLoader;
import de.lucaswerkmeister.jfractalizer.framework.Fractal;

public class CifFractXmlLoader extends FractXmlLoader {
	CifFractal		fractal;

	String			currentQName		= null;
	Attributes		currentAttributes	= null;
	ColorPalette	palette				= null;
	CifCanvas<?>	newCanvas			= null;

	public CifFractXmlLoader(Class<? extends CifFractal> fractalClass) {
		try {
			fractal = fractalClass.newInstance();
			newCanvas = (CifCanvas<?>) fractal.getCanvas();
		}
		catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		currentQName = qName;
		currentAttributes = attributes;
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		currentQName = null;
		currentAttributes = null;
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (currentQName == null)
			return;
		final String asString = new String(ch).substring(start, start + length);
		if (currentQName.equals("width")) {
			Dimension d = new Dimension(Integer.parseInt(asString), fractal.getCanvas().getHeight());
			newCanvas.setPreferredSize(d);
			newCanvas.setSize(d);
			fractal.setImageSize(d);
		}
		else if (currentQName.equals("height"))
			fractal.setImageSize(new Dimension(fractal.getCanvas().getWidth(), Integer.parseInt(asString)));
		else if (currentQName.equals("minReal"))
			fractal.setMinReal(Double.parseDouble(asString));
		else if (currentQName.equals("maxReal"))
			fractal.setMaxReal(Double.parseDouble(asString));
		else if (currentQName.equals("minImag"))
			fractal.setMinImag(Double.parseDouble(asString));
		else if (currentQName.equals("maxImag"))
			fractal.setMaxImag(Double.parseDouble(asString));
		else if (currentQName.equals("maxPasses"))
			fractal.setMaxPasses(Integer.parseInt(asString));
		else if (currentQName.equals("superSamplingFactor"))
			fractal.setSuperSamplingFactor(Byte.parseByte(asString));
	}

	@Override
	public void endDocument() throws SAXException {
		fractal.setCanvas(newCanvas);
	}

	@Override
	public Fractal getFractal() {
		return fractal;
	}
}
