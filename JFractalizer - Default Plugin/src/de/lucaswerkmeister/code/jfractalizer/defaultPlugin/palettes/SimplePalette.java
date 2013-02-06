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
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.FractXmlPaletteLoader;
import de.lucaswerkmeister.code.jfractalizer.FractalProvider;

public class SimplePalette implements ColorPalette {
	Color coreColor;
	Color startColor;
	Color endColor;
	int colorSteps;
	private final List<Color> fastColorStorage;

	public SimplePalette() {
		this(Color.black, Color.red, Color.yellow, 16);
	}

	public SimplePalette(final Color coreColor, final Color startColor, final Color endColor, final int colorSteps) {
		this.coreColor = coreColor;
		this.startColor = startColor;
		this.endColor = endColor;
		this.colorSteps = colorSteps;
		fastColorStorage = new ArrayList<>(colorSteps);
		makeFastStorage();
	}

	@Override
	public Color getColor(final int passes) {
		if (passes == -1)
			return coreColor;
		try {
			return fastColorStorage.get(passes % colorSteps);
		} catch (final Exception e) {
			makeFastStorage();
			return fastColorStorage.get(passes % colorSteps);
		}
	}

	@Override
	public void makeFastStorage() {
		final double lessColorSteps = colorSteps - 1;
		for (short s = 0; s < colorSteps; s++)
			fastColorStorage.add(new Color((int) Math.round(((colorSteps - 1 - s) / lessColorSteps)
					* startColor.getRed() + (s / lessColorSteps) * endColor.getRed()), (int) Math
					.round(((colorSteps - 1 - s) / lessColorSteps) * startColor.getGreen() + (s / lessColorSteps)
							* endColor.getGreen()), (int) Math.round(((colorSteps - 1 - s) / lessColorSteps)
					* startColor.getBlue() + (s / lessColorSteps) * endColor.getBlue())));
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException {
		final Attributes noAtts = new AttributesImpl();
		final AttributesImpl atts = new AttributesImpl();
		atts.addAttribute("", "", "canonicalName", "CDATA", getClass().getCanonicalName());
		handler.startElement("", "", "palette", atts);

		handler.startElement("", "", "startColor", noAtts);
		saveColor(handler, startColor);
		handler.endElement("", "", "startColor");

		handler.startElement("", "", "endColor", noAtts);
		saveColor(handler, endColor);
		handler.endElement("", "", "endColor");

		handler.startElement("", "", "coreColor", noAtts);
		saveColor(handler, coreColor);
		handler.endElement("", "", "coreColor");

		handler.startElement("", "", "colorSteps", noAtts);
		final char[] colSteps = Integer.toString(colorSteps).toCharArray();
		handler.characters(colSteps, 0, colSteps.length);
		handler.endElement("", "", "colorSteps");

		handler.endElement("", "", "palette");
	}

	public static void saveColor(final TransformerHandler handler, final Color color) throws SAXException {
		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "red", noAtts);
		final char[] red = Integer.toString(color.getRed()).toCharArray();
		handler.characters(red, 0, red.length);
		handler.endElement("", "", "red");

		handler.startElement("", "", "green", noAtts);
		final char[] green = Integer.toString(color.getGreen()).toCharArray();
		handler.characters(green, 0, green.length);
		handler.endElement("", "", "green");

		handler.startElement("", "", "blue", noAtts);
		final char[] blue = Integer.toString(color.getBlue()).toCharArray();
		handler.characters(blue, 0, blue.length);
		handler.endElement("", "", "blue");

		handler.startElement("", "", "alpha", noAtts);
		final char[] alpha = Integer.toString(color.getAlpha()).toCharArray();
		handler.characters(alpha, 0, alpha.length);
		handler.endElement("", "", "alpha");
	}

	@Override
	public FractXmlPaletteLoader getFractXmlLoader() {
		return new FractXmlSimplePaletteLoader();
	}

	@Override
	public String getName() {
		return "Simple Palette";
	}

	@Override
	public void initMenu(final Menu colorPaletteMenu, final FractalProvider provider, final Frame owner) {
		final MenuItem edit = new MenuItem("Edit Color Palette...", new MenuShortcut(KeyEvent.VK_E, true));
		edit.addActionListener(new SimplePaletteMenuListener(provider, owner, this));
		colorPaletteMenu.add(edit);
	}

	public boolean equals(final SimplePalette otherPalette) {
		return colorSteps == otherPalette.colorSteps && startColor.equals(otherPalette.startColor)
				&& endColor.equals(otherPalette.endColor) && coreColor.equals(otherPalette.coreColor);
	}

	class SimplePaletteMenuListener implements ActionListener {
		private final FractalProvider provider;
		private final Frame owner;
		private SimplePalette start;

		public SimplePaletteMenuListener(final FractalProvider provider, final Frame owner, final SimplePalette start) {
			this.provider = provider;
			this.owner = owner;
			this.start = start;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final SimplePaletteEditDialog d = new SimplePaletteEditDialog(owner, start);
			d.setVisible(true);
			final SimplePalette newPalette = d.getPalette();
			if (!start.equals(newPalette)) {
				start = newPalette;
				provider.stopCalculation();
				provider.setColorPalette(start);
				provider.startCalculation();
			}
		}
	}

	@Override
	public void handleCommandLineOption(String option) {
		// TODO implement
	}
}
