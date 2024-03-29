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
package de.lucaswerkmeister.jfractalizer.core;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlLoader;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlPaletteLoader;
import de.lucaswerkmeister.jfractalizer.framework.Fractal;

public class FractXmlReader extends DefaultHandler {
	private DefaultHandler			innerLoader	= null;
	private FractXmlLoader			fractal		= null;
	private FractXmlPaletteLoader	palette		= null;

	public Fractal getFractal() {
		return fractal.getFractal();
	}

	public ColorPalette getPalette() {
		return palette.getPalette();
	}

	@Override
	public void notationDecl(final String name, final String publicId, final String systemId) throws SAXException {
		if (innerLoader != null)
			innerLoader.notationDecl(name, publicId, systemId);
	}

	@Override
	public void unparsedEntityDecl(final String name, final String publicId, final String systemId,
			final String notationName) throws SAXException {
		if (innerLoader != null)
			innerLoader.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void setDocumentLocator(final Locator locator) {
		if (innerLoader != null)
			innerLoader.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		if (innerLoader != null)
			innerLoader.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		if (innerLoader != null)
			innerLoader.endDocument();
	}

	@Override
	public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
		if (innerLoader != null)
			innerLoader.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(final String prefix) throws SAXException {
		if (innerLoader != null)
			innerLoader.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		if (innerLoader != null)
			innerLoader.startElement(uri, localName, qName, attributes);
		else if (qName.equals("fractal"))
			try {
				innerLoader = ((Fractal) Class.forName(attributes.getValue("canonicalName")).getDeclaredConstructor().newInstance())
						.getFractXmlLoader();
				innerLoader.startDocument();
				innerLoader.startElement(uri, localName, qName, attributes);
				fractal = (FractXmlLoader) innerLoader;
			}
			catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
				e.printStackTrace();
			}
		else if (qName.equals("palette"))
			try {
				innerLoader = ((ColorPalette) Class.forName(attributes.getValue("canonicalName")).getDeclaredConstructor().newInstance())
						.getFractXmlLoader();
				innerLoader.startDocument();
				innerLoader.startElement(uri, localName, qName, attributes);
				palette = (FractXmlPaletteLoader) innerLoader;
			}
			catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (innerLoader != null)
			innerLoader.endElement(uri, localName, qName);
		if (qName.equals("palette") || qName.equals("fractal")) {
			innerLoader.endDocument();
			innerLoader = null;
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (innerLoader != null)
			innerLoader.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		if (innerLoader != null)
			innerLoader.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(final String target, final String data) throws SAXException {
		if (innerLoader != null)
			innerLoader.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(final String name) throws SAXException {
		if (innerLoader != null)
			innerLoader.skippedEntity(name);
	}

	@Override
	public void warning(final SAXParseException e) throws SAXException {
		if (innerLoader != null)
			innerLoader.warning(e);
	}

	@Override
	public void error(final SAXParseException e) throws SAXException {
		if (innerLoader != null)
			innerLoader.error(e);
	}

	@Override
	public void fatalError(final SAXParseException e) throws SAXException {
		if (innerLoader != null)
			innerLoader.fatalError(e);
	}
}
