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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.palettes;

import java.awt.Color;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.FractXmlPaletteLoader;

public class FractXmlSimplePaletteLoader extends FractXmlPaletteLoader
{
	LinkedList<String>	elementQNames	= new LinkedList<>();
	Color				currentStartColor	= Color.black, currentEndColor = Color.black, currentCoreColor = Color.black;
	int					currentLength		= 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
	{
		elementQNames.addLast(qName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException
	{
		elementQNames.removeLast();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException
	{
		final String asString = new String(ch).substring(start, start + length);
		if (elementQNames.size() >= 2 && elementQNames.get(elementQNames.size() - 2).equals("startColor"))
			switch (elementQNames.getLast())
			{
				case "red":
					currentStartColor = new Color(Integer.parseInt(asString), currentStartColor.getGreen(), currentStartColor.getBlue(),
							currentStartColor.getAlpha());
				case "green":
					currentStartColor = new Color(currentStartColor.getRed(), Integer.parseInt(asString), currentStartColor.getBlue(),
							currentStartColor.getAlpha());
				case "blue":
					currentStartColor = new Color(currentStartColor.getRed(), currentStartColor.getGreen(), Integer.parseInt(asString),
							currentStartColor.getAlpha());
				case "alpha":
					currentStartColor = new Color(currentStartColor.getRed(), currentStartColor.getGreen(), currentStartColor.getBlue(),
							Integer.parseInt(asString));
			}
		else if (elementQNames.size() >= 2 && elementQNames.get(elementQNames.size() - 2).equals("endColor"))
			switch (elementQNames.getLast())
			{
				case "red":
					currentEndColor = new Color(Integer.parseInt(asString), currentEndColor.getGreen(), currentEndColor.getBlue(),
							currentEndColor.getAlpha());
				case "green":
					currentEndColor = new Color(currentEndColor.getRed(), Integer.parseInt(asString), currentEndColor.getBlue(),
							currentEndColor.getAlpha());
				case "blue":
					currentEndColor = new Color(currentEndColor.getRed(), currentEndColor.getGreen(), Integer.parseInt(asString),
							currentEndColor.getAlpha());
				case "alpha":
					currentEndColor = new Color(currentEndColor.getRed(), currentEndColor.getGreen(), currentEndColor.getBlue(),
							Integer.parseInt(asString));
			}
		else if (elementQNames.size() >= 2 && elementQNames.get(elementQNames.size() - 2).equals("coreColor"))
			switch (elementQNames.getLast())
			{
				case "red":
					currentCoreColor = new Color(Integer.parseInt(asString), currentCoreColor.getGreen(), currentCoreColor.getBlue(),
							currentCoreColor.getAlpha());
				case "green":
					currentCoreColor = new Color(currentCoreColor.getRed(), Integer.parseInt(asString), currentCoreColor.getBlue(),
							currentCoreColor.getAlpha());
				case "blue":
					currentCoreColor = new Color(currentCoreColor.getRed(), currentCoreColor.getGreen(), Integer.parseInt(asString),
							currentCoreColor.getAlpha());
				case "alpha":
					currentCoreColor = new Color(currentCoreColor.getRed(), currentCoreColor.getGreen(), currentCoreColor.getBlue(),
							Integer.parseInt(asString));
			}
		else if (elementQNames.getLast().equals("colorSteps"))
			currentLength = Integer.parseInt(asString);
	}

	@Override
	public ColorPalette getPalette()
	{
		return new SimplePalette(currentCoreColor, currentStartColor, currentEndColor, currentLength);
	}
}
