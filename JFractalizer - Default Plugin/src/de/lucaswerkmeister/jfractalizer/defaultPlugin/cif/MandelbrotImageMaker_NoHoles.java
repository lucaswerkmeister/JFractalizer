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
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import de.lucaswerkmeister.jfractalizer.ColorPalette;

/**
 * This implementation of a MandelbrotImageMaker uses the fact that the
 * Mandelbrot Set is <i>full</i> (i. e. has no holes), which means that all
 * pixels that do not belong to the Mandelbrot Set must be connected. Thus, a
 * lot of pixels can be skipped: If they are not connected to any colored
 * pixels, they must be part of the Mandelbrot Set.
 * 
 * @author Lucas Werkmeister
 * 
 */
public class MandelbrotImageMaker_NoHoles extends CifImageMaker {
	final byte[][] pixels; // 0=not calculated, 1=no colored pixels, 2=has
	// colored pixels
	final Graphics targetGraphics;

	/**
	 * Creates a new instance of the MandelbrotImageMaker_NoHoles with specified
	 * bounds.
	 * 
	 * @param width
	 *            The width of the generated image.
	 * @param height
	 *            The height of the generated image.
	 * @param minReal
	 *            The lower value on the real scale (left boundary).
	 * @param maxReal
	 *            The higher value on the real scale (right boundary).
	 * @param minImag
	 *            The lower value on the imaginary scale (<b>lower</b>
	 *            boundary).
	 * @param maxImag
	 *            The higher value on the imaginary scale (upper boundary).
	 * @param maxPasses
	 *            The number of iterations that a complex number has to pass
	 *            before it is considered a member of the Mandelbrot Set.
	 * @param target
	 *            The BufferedImage to which the calculation results will be
	 *            drawn.
	 * @param targetX
	 *            The x coordinate on the target image to which the generated
	 *            image will be written.
	 * @param targetY
	 *            The y coordinate on the target image to which the generated
	 *            image will be written.
	 * @param superSamplingFactor
	 *            The AntiAliasing SuperSampling factor.
	 * @param provider
	 *            The fractal provider.
	 */
	public MandelbrotImageMaker_NoHoles(final int width, final int height,
			final double minReal, final double maxReal, final double minImag,
			final double maxImag, final int maxPasses,
			final BufferedImage target, final int targetX, final int targetY,
			final ColorPalette palette, final byte superSamplingFactor,
			final CifProvider provider) {
		super(width, height, minReal, maxReal, minImag, maxImag, maxPasses,
				target, targetX, targetY, palette, superSamplingFactor,
				provider);
		pixels = new byte[width][height];
		targetGraphics = target.createGraphics();
	}

	@Override
	public void run() {
		targetGraphics.setColor(palette.getColor(-1));
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
		double centerR, centerI, borderR, borderI;
		double r, i;
		int averageDenominator; // counting the denominator up is needed
		// because, due to rounding errors, sometimes
		// the SuperSampling loop runs less
		// than superSamplingFactor² times.
		int passes;
		Color c;
		int tX, tY;
		boolean addCurrentPoint = true;

		final int lessWidth = width - 1;
		final int lessHeight = height - 1;
		final double halfDeltaR = deltaR / 2;
		final double halfDeltaI = deltaI / 2;
		final double rangeR = (byte) (superSamplingFactor - 1) * halfDeltaR;
		final double rangeI = (byte) (superSamplingFactor - 1) * halfDeltaI;

		final LinkedList<Point> lastColoredPoints = new LinkedList<>();

		// fill lastColoredPoints with border points
		for (int x = 0; x < width; x++) {
			lastColoredPoints.addLast(new Point(x, 0));
			lastColoredPoints.addLast(new Point(x, height - 1));
		}
		for (int y = 0; y < height; y++) {
			lastColoredPoints.addLast(new Point(0, y));
			lastColoredPoints.addLast(new Point(width - 1, y));
		}
		for (int x = 0; x < width;)
			for (int y = 0; y < height;) {
				if (isInterrupted())
					return;
				if (pixels[x][y] == 0) {
					averageR = averageG = averageB = averageDenominator = 0;
					centerR = x * factorR + minReal;
					centerI = y * factorI + maxImag;
					borderR = centerR + rangeR;
					borderI = centerI - rangeI;
					// Save that the pixel is calculated
					pixels[x][y] = 1;
					// Calculate color
					for (r = centerR - rangeR; r <= borderR; r += deltaR)
						for (i = centerI + rangeI; i <= borderI; i -= deltaI) {
							passes = mandelbrotPasses(r, i, maxPasses);
							c = palette.getColor(passes);
							averageR += c.getRed();
							averageG += c.getGreen();
							averageB += c.getBlue();
							averageDenominator++;
							if (pixels[x][y] == 1 && passes != -1)
								// Save that the pixel has colored pixels
								pixels[x][y] = 2;
						}
					// Draw pixel
					c = new Color(averageR / averageDenominator, averageG
							/ averageDenominator, averageB / averageDenominator);
					tX = x + targetX;
					tY = y + targetY;
					targetGraphics.setColor(c);
					targetGraphics.drawLine(tX, tY, tX, tY);
				}
				// Calculate next pixel
				if (pixels[x][y] == 2) {
					addCurrentPoint = true;

					if (y > 0 && pixels[x][y - 1] == 0)
						y--;
					else if (x < lessWidth && pixels[x + 1][y] == 0)
						x++;
					else if (x > 0 && pixels[x - 1][y] == 0)
						x--;
					else if (y < lessHeight && pixels[x][y + 1] == 0)
						y++;
					else {
						addCurrentPoint = false;
						if (!lastColoredPoints.isEmpty()) {
							final Point lastPoint = lastColoredPoints
									.removeLast();
							x = lastPoint.x;
							y = lastPoint.y;
						} else
							return;
					}
					if (addCurrentPoint)
						lastColoredPoints.addLast(new Point(x, y));
				} else if (!lastColoredPoints.isEmpty()) {
					final Point lastPoint = lastColoredPoints.removeLast();
					x = lastPoint.x;
					y = lastPoint.y;
				} else
					return;
			}
	}
}
