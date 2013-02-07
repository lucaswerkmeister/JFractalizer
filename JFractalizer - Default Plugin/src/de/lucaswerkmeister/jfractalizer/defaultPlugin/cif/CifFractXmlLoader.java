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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.Dimension;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lucaswerkmeister.jfractalizer.ColorPalette;
import de.lucaswerkmeister.jfractalizer.FractXmlLoader;
import de.lucaswerkmeister.jfractalizer.FractalProvider;

public class CifFractXmlLoader extends FractXmlLoader {
	CifProvider provider;

	String currentQName = null;
	Attributes currentAttributes = null;
	ColorPalette palette = null;
	CifCanvas<?> newCanvas = null;

	public CifFractXmlLoader(Class<? extends CifProvider> providerClass) {
		try {
			provider = providerClass.newInstance();
			newCanvas = (CifCanvas<?>) provider.getCanvas();
		} catch (InstantiationException | IllegalAccessException e) {
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
		final String asString = new String(ch).substring(start, start + length);
		if (asString.equals("\n"))
			return;
		if (currentQName.equals("width")) {
			Dimension d = new Dimension(Integer.parseInt(asString), provider.getCanvas().getHeight());
			newCanvas.setPreferredSize(d);
			newCanvas.setSize(d);
		} else if (currentQName.equals("height")) {
			Dimension d = new Dimension(provider.getCanvas().getWidth(), Integer.parseInt(asString));
			newCanvas.setPreferredSize(d);
			newCanvas.setSize(d);
		} else if (currentQName.equals("minReal"))
			newCanvas.setMinReal(Double.parseDouble(asString));
		else if (currentQName.equals("maxReal"))
			newCanvas.setMaxReal(Double.parseDouble(asString));
		else if (currentQName.equals("minImag"))
			newCanvas.setMinImag(Double.parseDouble(asString));
		else if (currentQName.equals("maxImag"))
			newCanvas.setMaxImag(Double.parseDouble(asString));
		else if (currentQName.equals("maxPasses"))
			newCanvas.setMaxPasses(Integer.parseInt(asString));
		else if (currentQName.equals("superSamplingFactor"))
			newCanvas.setSuperSamplingFactor(Byte.parseByte(asString));
	}

	@Override
	public void endDocument() throws SAXException {
		provider.setCanvas(newCanvas);
	}

	@Override
	public FractalProvider getProvider() {
		return provider;
	}
}
