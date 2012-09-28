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

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;

import de.lucaswerkmeister.code.jfractalizer.ColorPalette;
import de.lucaswerkmeister.code.jfractalizer.Core;
import de.lucaswerkmeister.code.jfractalizer.defaultPlugin.palettes.SimplePalette;

public class CifCanvas<T extends CifImageMaker> extends Canvas
{
	private static final long		serialVersionUID	= -7909101981418946071L;
	private final CifProvider		provider;
	public static final int			START_WIDTH			= 1280;
	public static final int			START_HEIGHT		= 720;
	private double					minReal, maxReal, minImag, maxImag;
	ColorPalette					palette;
	private byte					superSamplingFactor;
	private WaitForCalcThreads		waiterThread;
	private int						maxPasses;
	private BufferedImage			tempImg;
	private long					startTime, stopTime;
	CifMouseListener				mouseListener;
	private Rectangle				selectedArea;
	History<CifParams>				history;
	private final Class<T>			imageMakerClass;

	private static final LookupOp	inverter;
	// this defines how fast the maxPasses will grow with increasing zoom. Higher number leads to slower growth.
	// TODO Check whether this value is ok; maybe make it configurable.
	static final double				maxPassesFactor		= 40;

	static
	{
		final short[] invertTable = new short[256];
		for (short s = 0; s < 256; s++)
			invertTable[s] = (short) (255 - s);
		inverter = new LookupOp(new ShortLookupTable(0, invertTable), null);
	}

	public CifCanvas(final CifProvider provider, Class<T> imageMakerClass)
	{
		this.imageMakerClass = imageMakerClass;
		setSize(START_WIDTH, START_HEIGHT);
		this.provider = provider;
		mouseListener = new CifMouseListener(this);
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
			for (final CifImageMaker m : (CifImageMaker[]) waiterThread.getThreads())
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
		g.drawImage(tempImg, 0, 0, null);
		if (selectedArea != null)
			g.drawImage(inverter.filter(tempImg, null), selectedArea.x, selectedArea.y, selectedArea.x + selectedArea.width, selectedArea.y
					+ selectedArea.height, selectedArea.x, selectedArea.y, selectedArea.x + selectedArea.width, selectedArea.y + selectedArea.height,
					null);
		if (!waiterThread.isAlive() && stopTime == 0 && startTime != 0)
		{
			stopTime = System.currentTimeMillis();
			long interval = stopTime - startTime;
			final short milliseconds = (short) (interval % 1000);
			interval -= milliseconds;
			interval /= 1000;
			final byte seconds = (byte) (interval % 60);
			interval -= seconds;
			interval /= 60;
			final byte minutes = (byte) (interval % 60);
			interval -= minutes;
			interval /= 60;
			final byte hours = (byte) (interval % 24);
			interval -= hours;
			interval /= 24;
			final long days = interval;
			final StringBuilder status = new StringBuilder("Calculation took ");
			if (days != 0)
			{
				status.append(days);
				status.append(" day");
				if (days != 1)
					status.append('s');
				status.append(' ');
			}
			if (hours != 0)
			{
				status.append(hours);
				status.append(" hour");
				if (hours != 1)
					status.append('s');
				status.append(' ');
			}
			if (minutes != 0)
			{
				status.append(minutes);
				status.append(" minute");
				if (minutes != 1)
					status.append('s');
				status.append(' ');
			}
			if (seconds != 0)
			{
				status.append(seconds);
				status.append(" second");
				if (seconds != 1)
					status.append('s');
				status.append(' ');
			}
			if (milliseconds != 0)
			{
				status.append(milliseconds);
				status.append(" millisecond");
				if (milliseconds != 1)
					status.append('s');
				status.append(' ');
			}
			status.append('(');
			status.append(stopTime - startTime);
			status.append(" ms).");
			Core.setStatus(status.toString());
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
			Core.setStatus("Calculating...");
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
			final CifImageMaker[] runningThreads = new CifImageMaker[horSections * verSections];
			try
			{
				for (int x = 0; x < horSections; x++)
					for (int y = 0; y < verSections; y++)
					{
						// TODO check if zoom settings will cause rounding errors due to limited computational accuracy
						final CifImageMaker maker = imageMakerClass.getConstructor(int.class, int.class, double.class, double.class, double.class,
								double.class, int.class, Graphics.class, int.class, int.class, ColorPalette.class, byte.class, CifProvider.class)
								.newInstance(x == horSections - 1 ? sectionWidth + getWidth() % horSections : sectionWidth,
										y == 0 ? sectionHeight + getHeight() % verSections : sectionHeight, minReal + x * realWidth,
										x == horSections - 1 ? maxReal : minReal + (x + 1) * realWidth, minImag + y * imagHeight,
										y == verSections - 1 ? maxImag : minImag + (y + 1) * imagHeight, maxPasses, g, x * sectionWidth,
										(verSections - y - 1) * sectionHeight, palette, superSamplingFactor, provider);
						runningThreads[x * verSections + y] = maker;
						maker.start();
					}
				waiterThread = new WaitForCalcThreads(runningThreads, this);
				waiterThread.start();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Invalid values!");
			waiterThread = new WaitForCalcThreads(new CifImageMaker[0], this);
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
		setParams(new CifParams(newMinReal, newMaxReal, newMinImag, newMaxImag, maxPasses, superSamplingFactor));
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

	CifParams getParams()
	{
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
	}

	void setParams(final CifParams params)
	{
		setParams(params, true);
	}

	void setParams(final CifParams params, final boolean addToHistory)
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
