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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

import java.awt.Color;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlPaletteLoader;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;

public class NodePalette extends EditDialogPalette {
	final List<ColorNode>	nodes;
	final List<Color>		fastColorStorage;
	Color					coreColor;
	boolean					fromCommandLine		= false;
	public static final int	LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((4 << 5) + (6 << 0)) << 8);
	public static final int	LOG_SAVING			= LOG_CLASS_PREFIX + 0;

	public NodePalette(final ColorNode[] nodes, final Color coreColor) {
		this(Arrays.asList(nodes), coreColor);
	}

	public NodePalette(final List<ColorNode> nodes, final Color coreColor) {
		this.nodes = nodes;
		this.coreColor = coreColor;
		fastColorStorage = new ArrayList<>(nodes.size() * nodes.get(0).getLength());
		makeFastStorage();
	}

	public NodePalette() {
		nodes = new LinkedList<>();
		nodes.add(new ColorNode(Color.red, Color.yellow, 16));
		fastColorStorage = new ArrayList<>();
		coreColor = Color.black;
		makeFastStorage();
	}

	@Override
	public Color getColor(final int passes) {
		if (passes == -1)
			return coreColor;
		try {
			return fastColorStorage.get(passes % fastColorStorage.size());
		}
		catch (final Exception e) {
			makeFastStorage();
			return fastColorStorage.get(passes % fastColorStorage.size());
		}
	}

	@Override
	public void makeFastStorage() {
		fastColorStorage.clear();
		for (final ColorNode node : nodes)
			for (short s = 0; s < node.getLength(); s++) {
				final double endFactor = (double) s / (node.getLength() - 1);
				final double startFactor = 1 - endFactor;
				fastColorStorage.add(new Color((int) Math.round(startFactor * node.getStartColor().getRed() + endFactor
						* node.getEndColor().getRed()), (int) Math.round(startFactor * node.getStartColor().getGreen()
						+ endFactor * node.getEndColor().getGreen()), (int) Math.round(startFactor
						* node.getStartColor().getBlue() + endFactor * node.getEndColor().getBlue())));
			}
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException {
		log(LOG_SAVING, this);

		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "nodes", noAtts);
		for (final ColorNode node : nodes) {
			handler.startElement("", "", "node", noAtts);

			handler.startElement("", "", "startColor", noAtts);
			SimplePalette.saveColor(handler, node.getStartColor());
			handler.endElement("", "", "startColor");

			handler.startElement("", "", "endColor", noAtts);
			SimplePalette.saveColor(handler, node.getEndColor());
			handler.endElement("", "", "endColor");

			handler.startElement("", "", "length", noAtts);
			final char[] length = Integer.toString(node.getLength()).toCharArray();
			handler.characters(length, 0, length.length);
			handler.endElement("", "", "length");

			handler.endElement("", "", "node");
		}
		handler.endElement("", "", "nodes");

		handler.startElement("", "", "coreColor", noAtts);
		SimplePalette.saveColor(handler, coreColor);
		handler.endElement("", "", "coreColor");
	}

	@Override
	public FractXmlPaletteLoader getFractXmlLoader() {
		return new FractXmlNodePaletteLoader();
	}

	@Override
	public String getName() {
		return "Node Palette";
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof NodePalette))
			return false;
		NodePalette otherPalette = (NodePalette) obj;

		if (otherPalette.nodes.size() != nodes.size())
			return false;
		for (int i = 0; i < nodes.size(); i++)
			if (!nodes.get(i).equals(otherPalette.nodes.get(i)))
				return false;
		return otherPalette.coreColor == coreColor;
	}

	@Override
	public int hashCode() {
		return nodes.hashCode() ^ coreColor.hashCode();
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		if (option.contains("-") ^ option.contains(":"))
			throw new IllegalCommandLineException("Unknown NodePalette argument \"" + option + "\"!");
		if (option.contains("-")) {
			if (!fromCommandLine) {
				nodes.clear();
				fromCommandLine = true;
			}
			String[] parts = option.split(":");
			String[] colors = parts[0].split("-");
			nodes.add(new ColorNode(colors[0], colors[1], Integer.parseInt(parts[1])));
		}
		else {
			coreColor = Color.decode(option);
		}
		makeFastStorage();
	}

	@Override
	protected PaletteEditDialog makeEditDialog(Frame owner) {
		return new NodePaletteEditDialog(owner, this);
	}
}
