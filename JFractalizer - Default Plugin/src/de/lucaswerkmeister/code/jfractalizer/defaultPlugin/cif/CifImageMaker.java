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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.cif;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;

/**
 * A CifImageMaker draws a snapshot of a CIF (Complex Iterative Fractal) onto a specified graphics. Different implementations may use several
 * optimizations.
 * 
 * @author Lucas Werkmeister
 * 
 */
public abstract class CifImageMaker extends Thread
{
	final int			width;
	final int			height;
	final double		minReal;
	final double		maxReal;
	final double		minImag;
	final double		maxImag;
	final int			maxPasses;
	final BufferedImage		targetImage;
	final int			targetX;
	final int			targetY;
	final ColorPalette	palette;
	final byte			superSamplingFactor;
	final CifProvider	provider;

	protected boolean	running	= true;

	/**
	 * Creates a new instance of the CifImageMaker with specified bounds.
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
	 *            The lower value on the real scale (<b>upper</b> boundary).
	 * @param maxImag
	 *            The higher value on the real scale (lower boundary).
	 * @param maxPasses
	 *            The number of iterations that a complex number has to pass before it is considered a member of the fractal.
	 * @param targetImage
	 *            The image to which the calculation results will be drawn.
	 * @param targetX
	 *            The x coordinate on the target image to which the generated image will be written.
	 * @param targetY
	 *            The y coordinate on the target image to which the generated image will be written.
	 * @param superSamplingFactor
	 *            The AntiAliasing SuperSampling factor.
	 * @param provider
	 *            The fractal provider for this image maker.
	 */
	public CifImageMaker(final int width, final int height, final double minReal, final double maxReal, final double minImag, final double maxImag,
			final int maxPasses, final BufferedImage targetImage, final int targetX, final int targetY, final ColorPalette palette,
			final byte superSamplingFactor, CifProvider provider)
	{
		this.width = width;
		this.height = height;
		this.minReal = minReal;
		this.maxReal = maxReal;
		this.minImag = minImag;
		this.maxImag = maxImag;
		this.maxPasses = maxPasses;
		this.targetImage = targetImage;
		this.targetX = targetX;
		this.targetY = targetY;
		this.palette = palette;
		this.superSamplingFactor = superSamplingFactor;
		this.provider = provider;
	}

	protected static int mandelbrotPasses(final double cReal, final double cImag, final int maxPasses)
	{
		double zReal = 0, zImag = 0, zRealNew = 0;
		int passes = 0;
		while (Math.sqrt(zReal * zReal + zImag * zImag) < 2)
		{
			if (++passes > maxPasses)
				return -1;
			zRealNew = zReal * zReal - zImag * zImag + cReal;
			zImag = 2 * zReal * zImag + cImag;
			zReal = zRealNew;
		}
		return passes;
	}

	protected static final int mandelbrotPasses_notFinal(final double cReal, final double cImag, final int maxPasses)
	{
		double zReal = 0, zImag = 0, zRealNew = 0;
		int passes = 0;
		while (Math.sqrt(zReal * zReal + zImag * zImag) < 2)
		{
			if (++passes > maxPasses)
				return -1;
			zRealNew = zReal * zReal - zImag * zImag + cReal;
			zImag = 2 * zReal * zImag + cImag;
			zReal = zRealNew;
		}
		return passes;
	}

	protected static final int mandelbrotPasses_final(final double cReal, final double cImag, final int maxPasses)
	{
		double zReal = 0, zImag = 0;
		int passes = 0;
		while (Math.sqrt(zReal * zReal + zImag * zImag) < 2)
		{
			if (++passes > maxPasses)
				return -1;
			final double zRealNew = zReal * zReal - zImag * zImag + cReal;
			zImag = 2 * zReal * zImag + cImag;
			zReal = zRealNew;
		}
		return passes;
	}

	protected static final int mandelbrotPasses_twoPass(final double cReal, final double cImag, final int maxPasses)
	{
		double zReal1 = 0, zImag1 = 0, zReal2 = 0, zImag2 = 0;
		int passes = 0;
		while (Math.sqrt(zReal1 * zReal1 + zImag1 * zImag1) < 2)
		{
			if (++passes > maxPasses)
				return -1;
			zReal2 = zReal1 * zReal1 - zImag1 * zImag1 + cReal;
			zImag2 = 2 * zReal1 * zImag1 + cImag;
			if (Math.sqrt(zReal2 * zReal2 + zImag2 * zImag2) >= 2)
				break;
			if (++passes > maxPasses)
				return -1;
			zReal1 = zReal2 * zReal2 - zImag2 * zImag2 + cReal;
			zImag1 = 2 * zReal2 * zImag2 + cImag;
		}
		return passes;
	}

	protected static final int juliaPasses(double zReal, double zImag, final double cReal, final double cImag, final int maxPasses)
	{
		int passes = 0;
		while (Math.sqrt(zReal * zReal + zImag * zImag) < 2)
		{
			if (++passes > maxPasses)
				return -1;
			final double zRealNew = zReal * zReal - zImag * zImag + cReal;
			zImag = 2 * zReal * zImag + cImag;
			zReal = zRealNew;
		}
		return passes;
	}

	@Override
	public abstract void run();

	public void stopCalculation()
	{
		running = false;
	}
}
