package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import java.awt.Color;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlPaletteLoader;

public class FractXmlHsbRotatePaletteLoader extends FractXmlPaletteLoader {
	private HsbRotatePalette	palette	= new HsbRotatePalette();
	String						currentQName;

	@Override
	public ColorPalette getPalette() {
		return palette;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentQName = qName;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String content = new String(ch);
		switch (currentQName) {
			case "hueStart":
				palette.setHueStart(Float.parseFloat(content));
				break;
			case "hueFactor":
				palette.setHueFactor(Float.parseFloat(content));
				break;
			case "saturation":
				palette.setSaturation(Float.parseFloat(content));
				break;
			case "brightness":
				palette.setBrightness(Float.parseFloat(content));
				break;
			case "red":
				palette.setCoreColor(new Color(Integer.parseInt(content), palette.getCoreColor().getBlue(), palette
						.getCoreColor().getGreen(), palette.getCoreColor().getAlpha()));
				break;
			case "blue":
				palette.setCoreColor(new Color(palette.getCoreColor().getRed(), Integer.parseInt(content), palette
						.getCoreColor().getGreen(), palette.getCoreColor().getAlpha()));
				break;
			case "green":
				palette.setCoreColor(new Color(palette.getCoreColor().getRed(), palette.getCoreColor().getBlue(),
						Integer.parseInt(content), palette.getCoreColor().getAlpha()));
				break;
			case "alpha":
				palette.setCoreColor(new Color(palette.getCoreColor().getRed(), palette.getCoreColor().getBlue(),
						palette.getCoreColor().getGreen(), Integer.parseInt(content)));
				break;
		}
	}
}
