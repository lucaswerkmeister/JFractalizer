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

import java.lang.reflect.InvocationTargetException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.FractXmlLoader;
import de.lucaswerkmeister.code.jfractalizer.FractXmlPaletteLoader;
import de.lucaswerkmeister.code.jfractalizer.FractalProvider;

public class CifFractXmlLoader extends FractXmlLoader
{
	CifProvider				provider;

	String					currentQName		= null;
	Attributes				currentAttributes	= null;
	ColorPalette			palette				= null;
	FractXmlPaletteLoader	innerHandler		= null;
	String					innerHandlerQName	= null;
	CifCanvas				newCanvas			= null;

	public CifFractXmlLoader(Class<? extends CifProvider> providerClass)
	{
		try
		{
			provider = providerClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
	{
		if (innerHandler == null)
		{
			if (qName.equals("palette"))
				try
				{
					final Class<?> paletteHandlerClass = Class.forName(attributes.getValue("canonicalName"));
					innerHandler = (FractXmlPaletteLoader) paletteHandlerClass.getMethod("getFractXmlLoader").invoke(
							paletteHandlerClass.getConstructor().newInstance());
					innerHandlerQName = qName;
					innerHandler.startElement(uri, localName, qName, attributes);
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException
						| ClassNotFoundException | InstantiationException e)
				{
					e.printStackTrace();
				}
			else
			{
				currentQName = qName;
				currentAttributes = attributes;
			}
		}
		else
			innerHandler.startElement(uri, localName, qName, attributes);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException
	{
		if (innerHandler == null)
		{
			currentQName = null;
			currentAttributes = null;
		}
		else if (qName.equals(innerHandlerQName))
		{
			if (innerHandler instanceof FractXmlPaletteLoader)
			{
				palette = innerHandler.getPalette();
				palette.makeFastStorage();
				newCanvas.setPalette(palette);
			}
			innerHandler = null;
			innerHandlerQName = null;
		}
		else
			innerHandler.endElement(uri, localName, qName);
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException
	{
		if (innerHandler == null)
		{
			final String asString = new String(ch).substring(start, start + length);
			if (asString.equals("\n"))
				return;
			if (currentQName.equals("width"))
				newCanvas.setSize(Integer.parseInt(asString), provider.getCanvas().getHeight());
			else if (currentQName.equals("height"))
				newCanvas.setSize(provider.getCanvas().getWidth(), Integer.parseInt(asString));
			else if (currentQName.equals("minReal"))
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
			else if (currentQName.equals("palette"))
				innerHandler.characters(ch, start, length);
		}
		else
			innerHandler.characters(ch, start, length);
	}

	@Override
	public void endDocument() throws SAXException
	{
		provider.setCanvas(newCanvas);
	}

	@Override
	public FractalProvider getProvider()
	{
		return provider;
	}

}
