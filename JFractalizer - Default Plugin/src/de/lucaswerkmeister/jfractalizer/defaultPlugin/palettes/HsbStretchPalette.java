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

/**
 * Similar to {@link HsbRotatePalette}, but with the hue stretching as the passes grow.
 * For example, given a base width of 2,
 * the cycles through the hue will finish at passes 1, 2, 4, 8, ...
 * This way, the same palette configuration is suitable for different depths of the fractal.
 *
 * @author Lucas Werkmeister
 */
public class HsbStretchPalette extends EditDialogPalette {
	private float			hueStart			= 0;
	private double			hueBaseWidth		= 6f;
	private float			saturation			= 1;
	private float			brightness			= 1;
	private Color			coreColor			= Color.black;
	public static final int	LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((4 << 5) + (0x0A << 0)) << 8);
	public static final int	LOG_SAVING			= LOG_CLASS_PREFIX + 0;

	public HsbStretchPalette() {

	}

	public HsbStretchPalette(float hueStart, double hueBaseWidth, float saturation, float brightness, Color coreColor) {
		this.hueStart = hueStart;
		this.hueBaseWidth = hueBaseWidth;
		this.saturation = saturation;
		this.brightness = brightness;
		this.coreColor = coreColor;
	}

	@Override
	public String getName() {
		return "HSB Stretching Palette";
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		switch (optionName) {
			case "hueStart":
				hueStart = Float.parseFloat(optionContent);
				break;
			case "hueBaseWidth":
				hueBaseWidth = Double.parseDouble(optionContent);
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
				throw new IllegalCommandLineException("Unknown option \"" + optionName + "\" for HsbStretchPalette!");
		}
	}

	@Override
	public Color getColor(int passes) {
		if (passes == -1)
			return coreColor;
		double passes1p = passes + 1.0; // this formula doesn’t work well for 0
		double log = Math.floor(Math.log(passes1p) / Math.log(hueBaseWidth));
		double lower = Math.pow(hueBaseWidth, log);
		double upper = Math.pow(hueBaseWidth, log + 1.0);
		float rotation = (float) ((passes - lower) / (upper - lower));
		return Color.getHSBColor(hueStart + rotation, saturation, brightness);
	}

	@Override
	public void saveFractXml(TransformerHandler handler) throws SAXException {
		log(LOG_SAVING, this);

		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "hueStart", noAtts);
		char[] chars = Float.toString(hueStart).toCharArray();
		handler.characters(chars, 0, chars.length);
		handler.endElement("", "", "hueStart");

		handler.startElement("", "", "hueBaseWidth", noAtts);
		chars = Double.toString(hueBaseWidth).toCharArray();
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
		return new FractXmlHsbStretchPaletteLoader();
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

	public double getHueBaseWidth() {
		return hueBaseWidth;
	}

	public void setHueBaseWidth(double hueBaseWidth) {
		this.hueBaseWidth = hueBaseWidth;
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
		return new HsbStretchPaletteEditDialog(owner, this);
	}
}
