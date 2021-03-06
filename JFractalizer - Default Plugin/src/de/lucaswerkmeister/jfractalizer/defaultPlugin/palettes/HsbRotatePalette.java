package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

import java.awt.Color;
import java.awt.Frame;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlPaletteLoader;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;

public class HsbRotatePalette extends EditDialogPalette {
	private float			hueStart			= 0;
	private float			hueFactor			= 1 / 16f;
	private float			saturation			= 1;
	private float			brightness			= 1;
	private Color			coreColor			= Color.black;
	public static final int	LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((4 << 5) + (0x0A << 0)) << 8);
	public static final int	LOG_SAVING			= LOG_CLASS_PREFIX + 0;

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
		log(LOG_SAVING, this);

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

	@Override
	protected PaletteEditDialog makeEditDialog(Frame owner) {
		return new HsbRotatePaletteEditDialog(owner, this);
	}
}
