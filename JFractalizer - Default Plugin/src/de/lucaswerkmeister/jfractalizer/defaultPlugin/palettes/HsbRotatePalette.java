package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlPaletteLoader;
import de.lucaswerkmeister.jfractalizer.framework.Fractal;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;

public class HsbRotatePalette implements ColorPalette {
	private float	hueStart	= 0;
	private float	hueFactor	= 1 / 16f;
	private float	saturation	= 1;
	private float	brightness	= 1;
	private Color	coreColor	= Color.black;

	public HsbRotatePalette() {

	}

	public HsbRotatePalette(float hueStart, float hueFactor, float saturation, float brightness, Color coreColor) {
		this.hueStart = hueStart;
		this.hueFactor = hueFactor;
		this.saturation = saturation;
		this.brightness = brightness;
		this.coreColor = coreColor;
	}

	@Override
	public String getName() {
		return "HSB Rotating Palette";
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		switch (optionName) {
			case "hueStart":
				hueStart = Float.parseFloat(optionContent);
				break;
			case "hueFactor":
				hueFactor = Float.parseFloat(optionContent);
				break;
			case "huePeriod":
				hueFactor = 1 / Float.parseFloat(optionContent);
				break;
			case "saturation":
				saturation = Float.parseFloat(optionContent);
				break;
			case "brightness":
				brightness = Float.parseFloat(optionContent);
				break;
			case "coreColor":
				coreColor = Color.decode(optionContent);
				break;
			default:
				throw new IllegalCommandLineException("Unknown option \"" + optionName + "\" for HsbRotatePalette!");
		}
	}

	@Override
	public Color getColor(int passes) {
		if (passes == -1)
			return coreColor;
		return Color.getHSBColor(hueStart + hueFactor * passes, saturation, brightness);
	}

	@Override
	public void saveFractXml(TransformerHandler handler) throws SAXException {
		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "hueStart", noAtts);
		char[] chars = Float.toString(hueStart).toCharArray();
		handler.characters(chars, 0, chars.length);
		handler.endElement("", "", "hueStart");

		handler.startElement("", "", "hueFactor", noAtts);
		chars = Float.toString(hueFactor).toCharArray();
		handler.characters(chars, 0, chars.length);
		handler.endElement("", "", "hueFactor");

		handler.startElement("", "", "saturation", noAtts);
		chars = Float.toString(saturation).toCharArray();
		handler.characters(chars, 0, chars.length);
		handler.endElement("", "", "saturation");

		handler.startElement("", "", "brightness", noAtts);
		chars = Float.toString(brightness).toCharArray();
		handler.characters(chars, 0, chars.length);
		handler.endElement("", "", "brightness");

		handler.startElement("", "", "coreColor", noAtts);
		SimplePalette.saveColor(handler, coreColor);
		handler.endElement("", "", "coreColor");
	}

	@Override
	public FractXmlPaletteLoader getFractXmlLoader() {
		return new FractXmlHsbRotatePaletteLoader();
	}

	@Override
	public void makeFastStorage() {
		// Do nothing
	}

	@Override
	public void initMenu(Menu colorPaletteMenu, Fractal fractal, Frame owner) {
		MenuItem editPalette = new MenuItem("Edit Color Palette...", new MenuShortcut(KeyEvent.VK_E, true));
		editPalette.addActionListener(new HsbRotatePaletteMenuListener(fractal, owner, this));
		colorPaletteMenu.add(editPalette);
	}

	public float getHueStart() {
		return hueStart;
	}

	public void setHueStart(float hueStart) {
		this.hueStart = hueStart;
	}

	public float getHueFactor() {
		return hueFactor;
	}

	public void setHueFactor(float hueFactor) {
		this.hueFactor = hueFactor;
	}

	public float getSaturation() {
		return saturation;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
	}

	public float getBrightness() {
		return brightness;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
	}

	public Color getCoreColor() {
		return coreColor;
	}

	public void setCoreColor(Color coreColor) {
		this.coreColor = coreColor;
	}

	class HsbRotatePaletteMenuListener implements ActionListener {
		private final Fractal		fractal;
		private final Frame			owner;
		private HsbRotatePalette	palette;

		public HsbRotatePaletteMenuListener(Fractal fractal, Frame owner, HsbRotatePalette palette) {
			this.fractal = fractal;
			this.owner = owner;
			this.palette = palette;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (e.getActionCommand()) {
				case "Edit Color Palette...":
					HsbRotatePaletteEditDialog d = new HsbRotatePaletteEditDialog(owner, palette);
					d.setVisible(true);
					final HsbRotatePalette newPalette = d.getPalette();
					if (!palette.equals(newPalette)) {
						palette = newPalette;
						fractal.stopCalculation();
						fractal.setColorPalette(palette);
						fractal.startCalculation();
					}
					break;
			}
		}
	}
}
