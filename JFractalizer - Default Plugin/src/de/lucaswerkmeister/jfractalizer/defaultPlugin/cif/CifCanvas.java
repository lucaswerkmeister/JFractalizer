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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;

public class CifCanvas<T extends CifImageMaker> extends Canvas {

	private static final long		serialVersionUID	= -7909101981418946071L;

	private final CifFractal		fractal;
	CifMouseListener				mouseListener;
	private Rectangle				selectedArea;

	private static final LookupOp	inverter;
	// this defines how fast the maxPasses will grow with increasing zoom.
	// Higher number leads to slower growth.
	// TODO Check whether this value is ok; maybe make it configurable.
	static final double				maxPassesFactor		= 40;

	static {
		final short[] invertTable = new short[256];
		for (short s = 0; s < 256; s++)
			invertTable[s] = (short) (255 - s);
		inverter = new LookupOp(new ShortLookupTable(0, invertTable), null);
	}

	public CifCanvas(final CifFractal fractal) {
		this.fractal = fractal;
		setPreferredSize(fractal.getImageSize());
		mouseListener = new CifMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	@Override
	public void paint(final Graphics g) {
		BufferedImage image = fractal.getImage();
		g.drawImage(image, 0, 0, null);
		if (selectedArea != null)
			g.drawImage(inverter.filter(image, null), selectedArea.x, selectedArea.y, selectedArea.x
					+ selectedArea.width, selectedArea.y + selectedArea.height, selectedArea.x, selectedArea.y,
					selectedArea.x + selectedArea.width, selectedArea.y + selectedArea.height, null);
		if (fractal.isRunning())
			repaint(10);
	}

	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	public void goToSelectedArea() {
		final double currentWidth = fractal.getMaxReal() - fractal.getMinReal();
		final double newWidth = currentWidth * ((double) selectedArea.width / fractal.getImageSize().width);
		final double newMinReal = fractal.getMinReal() + ((double) selectedArea.x / fractal.getImageSize().width)
				* currentWidth;
		final double newMaxReal = newMinReal + newWidth;
		final double currentHeight = fractal.getMaxImag() - fractal.getMinImag();
		final double newHeight = currentHeight * ((double) selectedArea.height / fractal.getImageSize().height);
		final double newMaxImag = fractal.getMaxImag() - ((double) selectedArea.y / fractal.getImageSize().height)
				* currentHeight;
		final double newMinImag = newMaxImag - newHeight;
		double maxPassesF = Math.max((double) fractal.getImageSize().width / selectedArea.width,
				(double) fractal.getImageSize().height / selectedArea.height);
		maxPassesF = ((maxPassesF - 1) / maxPassesFactor) + 1;
		int maxPasses = (int) (fractal.getMaxPasses() * maxPassesF);
		fractal.setParams(new CifParams(newMinReal, newMaxReal, newMinImag, newMaxImag, maxPasses, fractal
				.getSuperSamplingFactor()));
		fractal.startCalculation();
	}

	/**
	 * @param selectedArea
	 *            the selectedArea to set
	 */
	public void setSelectedArea(final Rectangle selectedArea) {
		if (selectedArea == null)
			this.selectedArea = null;
		else
			this.selectedArea = selectedArea.intersection(getBounds());
		repaint();
	}
}