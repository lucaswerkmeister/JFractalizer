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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.mandelbrot;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.defaultPlugin.palettes.SimplePalette;

public class MandelbrotCanvas extends Canvas
{
	private static final long			serialVersionUID	= -7909101981418946071L;
	private final MandelbrotProvider	provider;
	public static final int				START_WIDTH			= 1280;
	public static final int				START_HEIGHT		= 720;
	private double						minReal, maxReal, minImag, maxImag;
	ColorPalette						palette;
	private byte						superSamplingFactor;
	private WaitForCalcThreads			waiterThread;
	private int							maxPasses;
	private BufferedImage				tempImg;
	private long						startTime, stopTime;
	MandelbrotMouseListener				mouseListener;
	private Rectangle					selectedArea;
	History<MandelbrotParams>			history;

	private static final LookupOp		inverter;
	// this defines how fast the maxPasses will grow with increasing zoom. Higher number leads to slower growth.
	// TODO Check whether this value is ok; maybe make it configurable.
	static final double					maxPassesFactor		= 40;

	static
	{
		final short[] invertTable = new short[256];
		for (short s = 0; s < 256; s++)
			invertTable[s] = (short) (255 - s);
		inverter = new LookupOp(new ShortLookupTable(0, invertTable), null);
	}

	public MandelbrotCanvas(MandelbrotProvider provider)
	{
		setSize(START_WIDTH, START_HEIGHT);
		this.provider = provider;
		mouseListener = new MandelbrotMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		initDefaultValues();
		history = new History<>(256);
		history.add(getParams());
	}

	public void start()
	{
		tempImg = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		initThreads(tempImg.createGraphics());
		repaint();
	}

	public void stopCalculation()
	{
		if (waiterThread != null && waiterThread.isAlive())
			for (final MandelbrotImageMaker m : waiterThread.threads)
				m.stopCalculation();
	}

	public BufferedImage getImage()
	{
		return tempImg;
	}

	void initDefaultValues()
	{
		minReal = -3.2;
		maxReal = 3.2;
		minImag = -1.8;
		maxImag = 1.8;
		palette = new SimplePalette();
		superSamplingFactor = 1;
		maxPasses = 1000;
	}

	@Override
	public void paint(final Graphics g)
	{
		if (waiterThread == null)
			start();
		g.setPaintMode();
		g.drawImage(tempImg, 0, 0, null);
		if (selectedArea != null)
			g.drawImage(inverter.filter(tempImg, null), selectedArea.x, selectedArea.y, selectedArea.x + selectedArea.width, selectedArea.y
					+ selectedArea.height, selectedArea.x, selectedArea.y, selectedArea.x + selectedArea.width, selectedArea.y + selectedArea.height,
					null);
		if (!waiterThread.isAlive() && stopTime == 0 && startTime != 0)
		{
			stopTime = System.currentTimeMillis();
			System.out.println("Calculation took " + (stopTime - startTime) + "ms.");
		}
	}

	@Override
	public void update(final Graphics g)
	{
		paint(g);
	}

	private void initThreads(final Graphics g)
	{
		if (checkValues())
		{
			stopTime = 0;
			startTime = System.currentTimeMillis();
			final int cpuCount = Runtime.getRuntime().availableProcessors();
			final int lessSections = (int) Math.sqrt(cpuCount);
			final int moreSections = cpuCount / lessSections;
			int horSections, verSections;
			if (getWidth() >= getHeight())
			{
				horSections = moreSections;
				verSections = lessSections;
			}
			else
			{
				horSections = lessSections;
				verSections = moreSections;
			}
			final double realWidth = (maxReal - minReal) / horSections;
			final double imagHeight = (maxImag - minImag) / verSections;
			final int sectionWidth = getWidth() / horSections;
			final int sectionHeight = getHeight() / verSections;
			final MandelbrotImageMaker[] runningThreads = new MandelbrotImageMaker[horSections * verSections];
			for (int x = 0; x < horSections; x++)
				for (int y = 0; y < verSections; y++)
				{
					// TODO the choice of the MandelbrotImageMaker class should probably some time be configurable
					// TODO this code will leave black borders if width or height are not divisible by horSections or verSections
					// TODO check if zoom settings will cause rounding errors due to limited computational accuracy
					final MandelbrotImageMaker maker = new MandelbrotImageMaker_NoHoles(sectionWidth, sectionHeight, minReal + x * realWidth, minReal
							+ (x + 1) * realWidth, minImag + y * imagHeight, minImag + (y + 1) * imagHeight, maxPasses, g, x * sectionWidth,
							(verSections - y - 1) * sectionHeight, palette, superSamplingFactor);
					runningThreads[x * verSections + y] = maker;
					maker.start();
				}
			waiterThread = new WaitForCalcThreads(runningThreads, this);
			waiterThread.start();
		}
		else
		{
			System.out.println("Invalid values!");
			waiterThread = new WaitForCalcThreads(new MandelbrotImageMaker[0], this);
			waiterThread.start();
		}
	}

	private boolean checkValues()
	{
		return maxReal > minReal && maxImag > minImag && superSamplingFactor > 0 && palette != null && maxPasses > 0;
	}

	public void goToSelectedArea()
	{
		final double currentWidth = maxReal - minReal;
		final double newWidth = currentWidth * ((double) selectedArea.width / getWidth());
		final double newMinReal = minReal + ((double) selectedArea.x / getWidth()) * currentWidth;
		final double newMaxReal = newMinReal + newWidth;
		final double currentHeight = maxImag - minImag;
		final double newHeight = currentHeight * ((double) selectedArea.height / getHeight());
		final double newMaxImag = maxImag - ((double) selectedArea.y / getHeight()) * currentHeight;
		final double newMinImag = newMaxImag - newHeight;
		double maxPassesF = Math.max((double) getWidth() / selectedArea.width, (double) getHeight() / selectedArea.height);
		maxPassesF = ((maxPassesF - 1) / maxPassesFactor) + 1;
		maxPasses *= maxPassesF;
		setParams(new MandelbrotParams(newMinReal, newMaxReal, newMinImag, newMaxImag, maxPasses, superSamplingFactor));
		start();
	}

	/**
	 * @return the minReal
	 */
	double getMinReal()
	{
		return minReal;
	}

	/**
	 * @return the maxReal
	 */
	double getMaxReal()
	{
		return maxReal;
	}

	/**
	 * @return the minImag
	 */
	double getMinImag()
	{
		return minImag;
	}

	/**
	 * @return the maxImag
	 */
	double getMaxImag()
	{
		return maxImag;
	}

	/**
	 * @return the palette
	 */
	ColorPalette getPalette()
	{
		return palette;
	}

	/**
	 * @return the superSamplingFactor
	 */
	byte getSuperSamplingFactor()
	{
		return superSamplingFactor;
	}

	/**
	 * @return the maxPasses
	 */
	int getMaxPasses()
	{
		return maxPasses;
	}

	/**
	 * @param minReal
	 *            the minReal to set
	 */
	void setMinReal(final double minReal)
	{
		this.minReal = minReal;
	}

	/**
	 * @param maxReal
	 *            the maxReal to set
	 */
	void setMaxReal(final double maxReal)
	{
		this.maxReal = maxReal;
	}

	/**
	 * @param minImag
	 *            the minImag to set
	 */
	void setMinImag(final double minImag)
	{
		this.minImag = minImag;
	}

	/**
	 * @param maxImag
	 *            the maxImag to set
	 */
	void setMaxImag(final double maxImag)
	{
		this.maxImag = maxImag;
	}

	/**
	 * @param palette
	 *            the palette to set
	 */
	void setPalette(final ColorPalette palette)
	{
		this.palette = palette;
	}

	/**
	 * @param superSamplingFactor
	 *            the superSamplingFactor to set
	 */
	void setSuperSamplingFactor(final byte superSamplingFactor)
	{
		this.superSamplingFactor = superSamplingFactor;
	}

	/**
	 * @param maxPasses
	 *            the maxPasses to set
	 */
	void setMaxPasses(final int maxPasses)
	{
		this.maxPasses = maxPasses;
	}

	/**
	 * @param selectedArea
	 *            the selectedArea to set
	 */
	public void setSelectedArea(final Rectangle selectedArea)
	{
		if (selectedArea == null)
			this.selectedArea = null;
		else
			this.selectedArea = selectedArea.intersection(getBounds());
		repaint();
	}

	MandelbrotParams getParams()
	{
		return new MandelbrotParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	void setParams(MandelbrotParams params)
	{
		setParams(params, true);
	}

	void setParams(MandelbrotParams params, boolean addToHistory)
	{
		minReal = params.minReal;
		maxReal = params.maxReal;
		minImag = params.minImag;
		maxImag = params.maxImag;
		maxPasses = params.maxPasses;
		superSamplingFactor = params.superSamplingFactor;
		if (addToHistory)
			history.add(params);
		provider.undoMenuItem.setEnabled(history.canUndo());
		provider.redoMenuItem.setEnabled(history.canRedo());
	}
}
