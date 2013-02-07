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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import de.lucaswerkmeister.jfractalizer.ColorPalette;

public class JuliaImageMaker_CalcAll extends CifImageMaker {
	private final double cReal, cImag;
	final Graphics targetGraphics;

	public JuliaImageMaker_CalcAll(int width, int height, double minReal,
			double maxReal, double minImag, double maxImag, int maxPasses,
			BufferedImage target, int targetX, int targetY,
			ColorPalette palette, byte superSamplingFactor, CifProvider provider) {
		super(width, height, minReal, maxReal, minImag, maxImag, maxPasses,
				target, targetX, targetY, palette, superSamplingFactor,
				provider);
		cReal = ((JuliaProvider) provider).getCReal();
		cImag = ((JuliaProvider) provider).getCImag();
		targetGraphics = target.createGraphics();
	}

	@Override
	public void run() {
		targetGraphics.setColor(new Color(0, 0, 0, 0));
		targetGraphics.fillRect(0, 0, height, width);

		final double factorR = (maxReal - minReal) / width;
		final double factorI = (minImag - maxImag) / height; // imaginary scale
		// goes up, but
		// computer
		// graphics y goes
		// down, so min-
		// and maxImag have
		// to be swapped
		final double deltaR = factorR / superSamplingFactor;
		final double deltaI = factorI / superSamplingFactor;

		// declare all these variables outside of the loop for slightly better
		// performance
		int averageR, averageG, averageB;
		double centerR, centerI, rangeR, rangeI, borderR, borderI;
		double r, i;
		byte averageDenominator;
		int passes;
		Color c;
		int tX, tY;
		final byte lessSuperSamplingFactor = (byte) (superSamplingFactor - 1);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				if (isInterrupted())
					return;
				averageR = 0;
				averageG = 0;
				averageB = 0;
				centerR = x * factorR + minReal;
				centerI = y * factorI + maxImag;
				rangeR = lessSuperSamplingFactor * deltaR / 2;
				rangeI = lessSuperSamplingFactor * deltaI / 2;
				averageDenominator = 0; // counting the denominator up is needed
				// because due to rounding errors,
				// sometimes the SuperSampling loop runs
				// less than
				// superSamplingFactor*superSamplingFactor
				// times.
				borderR = centerR + rangeR;
				borderI = centerI - rangeI;
				for (r = centerR - rangeR; r <= borderR; r += deltaR)
					for (i = centerI + rangeI; i <= borderI; i -= deltaI) {
						passes = juliaPasses(r, i, cReal, cImag, maxPasses);
						c = palette.getColor(passes);
						averageR += c.getRed();
						averageG += c.getGreen();
						averageB += c.getBlue();
						averageDenominator++;
					}
				tX = x + targetX;
				tY = y + targetY;
				c = new Color(averageR / averageDenominator, averageG
						/ averageDenominator, averageB / averageDenominator);
				targetGraphics.setColor(c);
				targetGraphics.drawLine(tX, tY, tX, tY);
			}
	}
}
