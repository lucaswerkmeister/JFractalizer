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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;

public class ColorNode extends Panel implements ActionListener {
	private static final long		serialVersionUID	= -5555238254555730578L;
	private Color					startColor, endColor;
	private int						length;
	private final SelectableColor	startC, endC;
	private final JSpinner			lengthC;
	private ColorNode				linkedNode;
	public static final int			LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX
																+ (((4 << 5) + (9 << 0)) << 8);
	public static final int			LOG_UPDATE			= LOG_CLASS_PREFIX + 0;

	/**
	 * Creates a new ColorNode with the specified parameters.
	 * 
	 * @param startColor
	 *            The starting color of the new ColorNode.
	 * @param endColor
	 *            The ending color of the new ColorNode.
	 * @param length
	 *            The length of the new ColorNode.
	 */
	public ColorNode(final Color startColor, final Color endColor, final int length) {
		super(new GridLayout(3, 0));
		this.startColor = startColor;
		this.endColor = endColor;
		this.length = length;
		startC = new SelectableColor(startColor);
		endC = new SelectableColor(endColor);
		endC.addActionListener(this);
		lengthC = new JSpinner(new SpinnerNumberModel(length, 1, null, 1));
		lengthC.setPreferredSize(new Dimension(40, lengthC.getPreferredSize().height));
		add(startC);
		add(endC);
		add(lengthC);
	}

	/**
	 * Creates a new {@link ColorNode} with the specified parameters.
	 * <p>
	 * The colors are decoded with {@link Color#decode(String)} and must match its requirements. (Typically, the colors
	 * will be given in hex format: #00FF77)
	 * 
	 * @param startColor
	 *            The starting color of the new ColorNode.
	 * @param endColor
	 *            The ending color of the new ColorNode.
	 * @param length
	 *            The length of the new ColorNode.
	 */
	public ColorNode(String startColor, String endColor, int length) {
		this(Color.decode(startColor), Color.decode(endColor), length);
	}

	@Override
	public String toString() {
		return startColor.toString() + "..." + length + "..." + endColor.toString();
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof ColorNode) {
			ColorNode otherNode = (ColorNode) other;
			return length == otherNode.length && startColor == otherNode.startColor && endColor == otherNode.endColor;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(length).hashCode() ^ startColor.hashCode() ^ endColor.hashCode();
	}

	/**
	 * @return the startColor
	 */
	public Color getStartColor() {
		return startColor;
	}

	/**
	 * @return the endColor
	 */
	public Color getEndColor() {
		return endColor;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	public void link(final ColorNode nextNode) {
		linkedNode = nextNode;
		linkedNode.update(endC.getColor());
	}

	public void unlink() {
		linkedNode = null;
	}

	private void update(final Color newColor) {
		log(LOG_UPDATE, this, startC.getColor(), newColor);
		startC.setColor(newColor);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("Color changed"))
			if (linkedNode != null)
				linkedNode.update(endC.getColor());
	}

	public ColorNode copy() {
		return new ColorNode(startColor, endColor, length);
	}

	public void prepare() {
		startColor = startC.getColor();
		endColor = endC.getColor();
		length = (int) lengthC.getValue();
	}
}
