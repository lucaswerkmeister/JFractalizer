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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PaletteClassReader extends FractXmlPaletteLoader
{
	private FractXmlPaletteLoader	innerLoader	= null;	;

	@Override
	public ColorPalette getPalette()
	{
		return innerLoader.getPalette();
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.notationDecl(name, publicId, systemId);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void setDocumentLocator(Locator locator)
	{
		if (innerLoader != null)
			innerLoader.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException
	{
		if (innerLoader != null)
			innerLoader.startDocument();
	}

	@Override
	public void endDocument() throws SAXException
	{
		if (innerLoader != null)
			innerLoader.endDocument();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.startPrefixMapping(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.endPrefixMapping(prefix);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.startElement(uri, localName, qName, attributes);
		else if (qName.equals("palette"))
			try
			{
				innerLoader = ((ColorPalette) Class.forName(attributes.getValue("canonicalName")).newInstance()).getFractXmlLoader();
				innerLoader.startElement(uri, localName, qName, attributes);
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				e.printStackTrace();
			}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(String name) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.skippedEntity(name);
	}

	@Override
	public void warning(SAXParseException e) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.warning(e);
	}

	@Override
	public void error(SAXParseException e) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.error(e);
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException
	{
		if (innerLoader != null)
			innerLoader.fatalError(e);
	}
}
