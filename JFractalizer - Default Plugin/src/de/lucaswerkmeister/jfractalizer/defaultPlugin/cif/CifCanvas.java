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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.LookupOp;
import java.awt.image.ShortLookupTable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.lucaswerkmeister.jfractalizer.ColorPalette;
import de.lucaswerkmeister.jfractalizer.Core;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.SimplePalette;

public class CifCanvas<T extends CifImageMaker> extends Canvas {

	private static final long serialVersionUID = -7909101981418946071L;
	private static final boolean USE_MORE_THREADS_THAN_CORES = true;

	private final CifFractal fractal;
	public static final int START_WIDTH = 960;
	public static final int START_HEIGHT = 540;
	private int width = START_WIDTH;
	private int height = START_HEIGHT;
	private double minReal, maxReal, minImag, maxImag;
	ColorPalette palette;
	private byte superSamplingFactor;
	private ExecutorService executorService;
	private List<Future<?>> runningTasks; // <?> because no result is returned
	private BufferedImage tempImg;
	private int maxPasses;
	private long startTime, stopTime;
	CifMouseListener mouseListener;
	private Rectangle selectedArea;
	History<CifParams> history;
	private final Class<T> imageMakerClass;
	SubImage[] subImages;

	private static final LookupOp inverter;
	// this defines how fast the maxPasses will grow with increasing zoom.
	// Higher number leads to slower growth.
	// TODO Check whether this value is ok; maybe make it configurable.
	static final double maxPassesFactor = 40;

	static {
		final short[] invertTable = new short[256];
		for (short s = 0; s < 256; s++)
			invertTable[s] = (short) (255 - s);
		inverter = new LookupOp(new ShortLookupTable(0, invertTable), null);
	}

	public CifCanvas(final CifFractal fractal, Class<T> imageMakerClass) {
		this.imageMakerClass = imageMakerClass;
		setPreferredSize(new Dimension(width, height));
		this.fractal = fractal;
		mouseListener = new CifMouseListener(this);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		initDefaultValues();
		history = new History<>(256);
		history.add(getParams());
	}

	public void start() {
		tempImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		initThreads();
		repaint();
	}

	public void stopCalculation() {
		if (runningTasks != null)
			for (Future<?> f : runningTasks)
				f.cancel(true);
	}

	public BufferedImage getImage() {
		if (getParent() == null)
			constructImage();
		return tempImg;
	}

	void initDefaultValues() {
		minReal = -3.2;
		maxReal = 3.2;
		minImag = -1.8;
		maxImag = 1.8;
		palette = new SimplePalette();
		superSamplingFactor = 1;
		maxPasses = 1000;
	}

	@Override
	public void paint(final Graphics g) {
		if (executorService == null)
			return;
		constructImage();
		g.drawImage(tempImg, 0, 0, null);
		if (selectedArea != null)
			g.drawImage(inverter.filter(tempImg, null), selectedArea.x,
					selectedArea.y, selectedArea.x + selectedArea.width,
					selectedArea.y + selectedArea.height, selectedArea.x,
					selectedArea.y, selectedArea.x + selectedArea.width,
					selectedArea.y + selectedArea.height, null);
		if (isRunning())
			repaint(10);
		else if (stopTime == 0 && startTime != 0) {
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
			if (days != 0) {
				status.append(days);
				status.append(" day");
				if (days != 1)
					status.append('s');
				status.append(' ');
			}
			if (hours != 0) {
				status.append(hours);
				status.append(" hour");
				if (hours != 1)
					status.append('s');
				status.append(' ');
			}
			if (minutes != 0) {
				status.append(minutes);
				status.append(" minute");
				if (minutes != 1)
					status.append('s');
				status.append(' ');
			}
			if (seconds != 0) {
				status.append(seconds);
				status.append(" second");
				if (seconds != 1)
					status.append('s');
				status.append(' ');
			}
			if (milliseconds != 0) {
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

	private void constructImage() {
		for (int i = 0; i < subImages.length; i++)
			tempImg.getGraphics().drawImage(subImages[i].subImage,
					subImages[i].offsetX, subImages[i].offsetY, null);
	}

	@Override
	public void update(final Graphics g) {
		paint(g);
	}

	private void initThreads() {
		final int cpuCount = Runtime.getRuntime().availableProcessors();
		if (executorService == null)
			executorService = Executors.newFixedThreadPool(cpuCount);
		if (runningTasks == null)
			runningTasks = new LinkedList<>();
		if (checkValues()) {
			Core.setStatus("Calculating...");
			stopTime = 0;
			startTime = System.currentTimeMillis();
			int lessSections = (int) Math.sqrt(cpuCount);
			int moreSections = (lessSections == 1) ? cpuCount : cpuCount
					/ lessSections;
			if (USE_MORE_THREADS_THAN_CORES) {
				final int temp = lessSections;
				lessSections = moreSections;
				moreSections = 2 * temp;
			}
			int horSections, verSections;
			if (width >= height) {
				horSections = moreSections;
				verSections = lessSections;
			} else {
				horSections = lessSections;
				verSections = moreSections;
			}
			final double realWidth = (maxReal - minReal) / horSections;
			final double imagHeight = (maxImag - minImag) / verSections;
			final int sectionWidth = width / horSections;
			final int sectionHeight = height / verSections;
			subImages = new SubImage[horSections * verSections];
			try {
				for (int x = 0; x < horSections; x++)
					for (int y = 0; y < verSections; y++) {
						// TODO check if zoom settings will cause rounding
						// errors due to limited computational accuracy
						int currentWidth = x == horSections - 1 ? sectionWidth
								+ width % horSections : sectionWidth;
						int currentHeight = y == 0 ? sectionHeight + height
								% verSections : sectionHeight;
						BufferedImage subImage = new BufferedImage(
								currentWidth, currentHeight,
								BufferedImage.TYPE_INT_ARGB);
						final CifImageMaker maker = imageMakerClass
								.getConstructor(int.class, int.class,
										double.class, double.class,
										double.class, double.class, int.class,
										BufferedImage.class, int.class,
										int.class, ColorPalette.class,
										byte.class, CifFractal.class)
								.newInstance(
										currentWidth,
										currentHeight,
										minReal + x * realWidth,
										x == horSections - 1 ? maxReal
												: minReal + (x + 1) * realWidth,
										minImag + y * imagHeight,
										y == verSections - 1 ? maxImag
												: minImag + (y + 1)
														* imagHeight,
										maxPasses, subImage, 0, 0, palette,
										superSamplingFactor, fractal);
						runningTasks.add(executorService.submit(maker));
						subImages[x * verSections + y] = new SubImage(x
								* sectionWidth, (verSections - y - 1)
								* sectionHeight, subImage);
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Invalid values!");
		}
	}

	private boolean isRunning() {
		if (runningTasks == null)
			return false;
		for (Future<?> f : runningTasks)
			if (!f.isDone())
				return true;
		return false;
	}

	private boolean checkValues() {
		return maxReal > minReal && maxImag > minImag
				&& superSamplingFactor > 0 && palette != null && maxPasses > 0;
	}

	public void goToSelectedArea() {
		final double currentWidth = maxReal - minReal;
		final double newWidth = currentWidth
				* ((double) selectedArea.width / width);
		final double newMinReal = minReal + ((double) selectedArea.x / width)
				* currentWidth;
		final double newMaxReal = newMinReal + newWidth;
		final double currentHeight = maxImag - minImag;
		final double newHeight = currentHeight
				* ((double) selectedArea.height / height);
		final double newMaxImag = maxImag - ((double) selectedArea.y / height)
				* currentHeight;
		final double newMinImag = newMaxImag - newHeight;
		double maxPassesF = Math.max((double) width / selectedArea.width,
				(double) height / selectedArea.height);
		maxPassesF = ((maxPassesF - 1) / maxPassesFactor) + 1;
		maxPasses *= maxPassesF;
		setParams(new CifParams(newMinReal, newMaxReal, newMinImag, newMaxImag,
				maxPasses, superSamplingFactor));
		start();
	}

	/**
	 * @return the minReal
	 */
	double getMinReal() {
		return minReal;
	}

	/**
	 * @return the maxReal
	 */
	double getMaxReal() {
		return maxReal;
	}

	/**
	 * @return the minImag
	 */
	double getMinImag() {
		return minImag;
	}

	/**
	 * @return the maxImag
	 */
	double getMaxImag() {
		return maxImag;
	}

	/**
	 * @return the palette
	 */
	ColorPalette getPalette() {
		return palette;
	}

	/**
	 * @return the superSamplingFactor
	 */
	byte getSuperSamplingFactor() {
		return superSamplingFactor;
	}

	/**
	 * @return the maxPasses
	 */
	int getMaxPasses() {
		return maxPasses;
	}

	/**
	 * @param minReal
	 *            the minReal to set
	 */
	void setMinReal(final double minReal) {
		this.minReal = minReal;
	}

	/**
	 * @param maxReal
	 *            the maxReal to set
	 */
	void setMaxReal(final double maxReal) {
		this.maxReal = maxReal;
	}

	/**
	 * @param minImag
	 *            the minImag to set
	 */
	void setMinImag(final double minImag) {
		this.minImag = minImag;
	}

	/**
	 * @param maxImag
	 *            the maxImag to set
	 */
	void setMaxImag(final double maxImag) {
		this.maxImag = maxImag;
	}

	/**
	 * @param palette
	 *            the palette to set
	 */
	void setPalette(final ColorPalette palette) {
		this.palette = palette;
	}

	/**
	 * @param superSamplingFactor
	 *            the superSamplingFactor to set
	 */
	void setSuperSamplingFactor(final byte superSamplingFactor) {
		this.superSamplingFactor = superSamplingFactor;
	}

	/**
	 * @param maxPasses
	 *            the maxPasses to set
	 */
	void setMaxPasses(final int maxPasses) {
		this.maxPasses = maxPasses;
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

	CifParams getParams() {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses,
				superSamplingFactor);
	}

	void setParams(final CifParams params) {
		setParams(params, true);
	}

	void setParams(final CifParams params, final boolean addToHistory) {
		minReal = params.minReal;
		maxReal = params.maxReal;
		minImag = params.minImag;
		maxImag = params.maxImag;
		maxPasses = params.maxPasses;
		superSamplingFactor = params.superSamplingFactor;
		if (getParent() != null) {
			if (addToHistory)
				history.add(params);
			fractal.undoMenuItem.setEnabled(history.canUndo());
			fractal.redoMenuItem.setEnabled(history.canRedo());
		}
	}

	public void setImageSize(Dimension d) {
		width = d.width;
		height = d.height;
	}

	public Dimension getImageSize() {
		return new Dimension(width, height);
	}

	public void awaitCalculation() {
		try {
			for (Future<?> f : runningTasks)
				f.get();
		} catch (InterruptedException | ExecutionException e) {
			// do nothing
		}
	}
}

class SubImage {
	final int offsetX;
	final int offsetY;
	final Image subImage;

	SubImage(int offsetX, int offsetY, Image subImage) {
		super();
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.subImage = subImage;
	}
}