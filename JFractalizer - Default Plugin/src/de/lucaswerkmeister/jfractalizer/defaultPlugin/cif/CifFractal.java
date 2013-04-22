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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.core.Core;
import de.lucaswerkmeister.jfractalizer.core.TimeSpan;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.SimplePalette;
import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;
import de.lucaswerkmeister.jfractalizer.framework.ZoomableFractal;

public abstract class CifFractal implements ZoomableFractal {
	CifCanvas<?>									canvas;
	MenuItem										undoMenuItem, redoMenuItem;

	public static final int							START_WIDTH					= 960;
	public static final int							START_HEIGHT				= 540;
	private int										width						= START_WIDTH;
	private int										height						= START_HEIGHT;
	private double									minReal, maxReal, minImag, maxImag;
	ColorPalette									palette;
	private byte									superSamplingFactor;
	private ExecutorService							executorService;
	private List<Future<?>>							runningTasks;
	private int										maxPasses;
	SubImage[]										subImages;
	private final Class<? extends CifImageMaker>	imageMakerClass;
	private int										imageType					= BufferedImage.TYPE_INT_ARGB;
	History<CifParams>								history;
	private long									startTime, stopTime;
	private final Set<ActionListener>				calculationFinishedListeners;

	private static final boolean					USE_MORE_THREADS_THAN_CORES	= true;						// TODO
																												// cc #5

	protected CifFractal(Class<? extends CifImageMaker> imageMakerClass) {
		this.imageMakerClass = imageMakerClass;
		this.calculationFinishedListeners = new HashSet<>();
		initDefaultValues();
	}

	@Override
	public Canvas getCanvas() {
		if (canvas == null) {
			canvas = new CifCanvas<>(this);
			addCalculationFinishedListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Core.setStatus("Calculation finished in " + new TimeSpan(stopTime - startTime).toString() + ".");
				}
			});
			history = new History<>(256);
			history.add(getParams());
		}
		return canvas;
	}

	@Override
	public BufferedImage getImage() {
		BufferedImage ret = new BufferedImage(width, height, imageType);
		Graphics g = ret.getGraphics();
		g.setColor(palette.getColor(-1));
		g.fillRect(0, 0, width, height);
		if (subImages != null)
			for (SubImage img : subImages)
				ret.getGraphics().drawImage(img.subImage, img.offsetX, img.offsetY, null);
		return ret;
	}

	@Override
	public void suggestImageType(int imageType) {
		this.imageType = imageType;
	}

	@Override
	public void saveFractXml(final TransformerHandler handler) throws SAXException {
		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "width", noAtts);
		final char[] width = Integer.toString(this.width).toCharArray();
		handler.characters(width, 0, width.length);
		handler.endElement("", "", "width");

		handler.startElement("", "", "height", noAtts);
		final char[] height = Integer.toString(this.height).toCharArray();
		handler.characters(height, 0, height.length);
		handler.endElement("", "", "height");

		handler.startElement("", "", "minReal", noAtts);
		final char[] minReal = Double.toString(this.getMinReal()).toCharArray();
		handler.characters(minReal, 0, minReal.length);
		handler.endElement("", "", "minReal");

		handler.startElement("", "", "maxReal", noAtts);
		final char[] maxReal = Double.toString(this.getMaxReal()).toCharArray();
		handler.characters(maxReal, 0, maxReal.length);
		handler.endElement("", "", "maxReal");

		handler.startElement("", "", "minImag", noAtts);
		final char[] minImag = Double.toString(this.getMinImag()).toCharArray();
		handler.characters(minImag, 0, minImag.length);
		handler.endElement("", "", "minImag");

		handler.startElement("", "", "maxImag", noAtts);
		final char[] maxImag = Double.toString(this.getMaxImag()).toCharArray();
		handler.characters(maxImag, 0, maxImag.length);
		handler.endElement("", "", "maxImag");

		handler.startElement("", "", "maxPasses", noAtts);
		final char[] maxPasses = Integer.toString(this.getMaxPasses()).toCharArray();
		handler.characters(maxPasses, 0, maxPasses.length);
		handler.endElement("", "", "maxPasses");

		handler.startElement("", "", "superSamplingFactor", noAtts);
		final char[] superSamplingFactor = Byte.toString(this.getSuperSamplingFactor()).toCharArray();
		handler.characters(superSamplingFactor, 0, superSamplingFactor.length);
		handler.endElement("", "", "superSamplingFactor");
	}

	public void setCanvas(final CifCanvas<?> newCanvas) {
		canvas = newCanvas;
	}

	@Override
	public void setColorPalette(final ColorPalette newPalette) {
		palette = newPalette;
	}

	@Override
	public void startCalculation() {
		initThreads();
		if (canvas != null)
			canvas.repaint();
	}

	@Override
	public void stopCalculation() {
		if (runningTasks != null) {
			for (Future<?> f : runningTasks)
				f.cancel(true);
			runningTasks.clear();
		}
	}

	@Override
	public void initMenu(final Menu fractalMenu) {
		final MenuItem recalculate = new MenuItem("Recalculate", new MenuShortcut(KeyEvent.VK_R));
		recalculate.addActionListener(getMenuListener());
		fractalMenu.add(recalculate);
		fractalMenu.addSeparator();
		final MenuItem editBoundaries = new MenuItem("Edit boundaries...", new MenuShortcut(KeyEvent.VK_E));
		editBoundaries.addActionListener(getMenuListener());
		fractalMenu.add(editBoundaries);
		final MenuItem additionalParams = new MenuItem("Edit additional parameters...", new MenuShortcut(KeyEvent.VK_A));
		additionalParams.addActionListener(getMenuListener());
		fractalMenu.add(additionalParams);
		fractalMenu.addSeparator();
		undoMenuItem = new MenuItem("Undo", new MenuShortcut(KeyEvent.VK_Z));
		undoMenuItem.addActionListener(getMenuListener());
		undoMenuItem.setEnabled(history.canUndo());
		fractalMenu.add(undoMenuItem);
		redoMenuItem = new MenuItem("Redo", new MenuShortcut(KeyEvent.VK_Y));
		redoMenuItem.addActionListener(getMenuListener());
		redoMenuItem.setEnabled(history.canRedo());
		fractalMenu.add(redoMenuItem);
	}

	protected abstract CifMenuListener getMenuListener();

	@Override
	public void initContextMenu(final PopupMenu contextMenu) {
		// do nothing
	}

	@Override
	public void zoom(final int x, final int y, final double factor) {
		final double currentWidth = (getMaxReal() - getMinReal());
		final double currentHeight = (getMaxImag() - getMinImag());
		final double centerR = getMinReal() + currentWidth * ((double) x / getImageSize().width);
		final double centerI = getMinImag() + currentHeight * (1 - ((double) y / getImageSize().height));
		final double halfSizeR = currentWidth * factor / 2;
		final double halfSizeI = currentHeight * factor / 2;
		setMinReal(centerR - halfSizeR);
		setMaxReal(centerR + halfSizeR);
		setMinImag(centerI - halfSizeI);
		setMaxImag(centerI + halfSizeI);
		double maxPassesF = 1 / factor;
		maxPassesF = ((maxPassesF - 1) / CifCanvas.maxPassesFactor) + 1;
		setMaxPasses((int) Math.round(getMaxPasses() * maxPassesF));
	}

	@Override
	public void zoomToStart(int x, int y, double factor) {
		zoom(x, y, factor);
		Rectangle2D.Double start = getStartArea();
		double realSize = getMaxReal() - getMinReal();
		double imagSize = getMaxImag() - getMinImag();
		if (realSize < start.width) {
			// move horizontally
			if (getMaxReal() > start.getMaxX()) {
				// clamp to east edge
				setMaxReal(start.getMaxX());
				setMinReal(start.getMaxX() - realSize);
			}
			else if (getMinReal() < start.getMinX()) {
				// clamp to west edge
				setMinReal(start.getMinX());
				setMaxReal(start.getMinX() + realSize);
			}
		}
		else {
			// center horizontally
			setMinReal(start.getCenterX() - realSize / 2);
			setMaxReal(start.getCenterX() + realSize / 2);
		}
		if (imagSize < start.height) {
			// move vertically
			if (getMaxImag() > start.getMaxY()) {
				// clamp to north edge
				setMaxImag(start.getMaxY());
				setMinImag(start.getMaxY() - imagSize);
			}
			else if (getMinImag() < start.getMinY()) {
				// clamp to south edge
				setMinImag(start.getMinY());
				setMaxImag(start.getMinY() + imagSize);
			}
		}
		else {
			// center vertically
			setMinImag(start.getCenterY() - imagSize / 2);
			setMaxImag(start.getCenterY() + imagSize / 2);
		}
	}

	@Override
	public double getZoomFactor() {
		Rectangle2D.Double start = getStartArea();
		double xZoomFactor = start.width / (getMaxReal() - getMinReal());
		double yZoomFactor = start.height / (getMaxImag() - getMinImag());
		return Math.max(xZoomFactor, yZoomFactor);
	}

	@Override
	public void addCalculationFinishedListener(final ActionListener listener) {
		calculationFinishedListeners.add(listener);
	}

	private void initThreads() {
		final int cpuCount = Runtime.getRuntime().availableProcessors();
		if (executorService == null)
			executorService = Executors.newFixedThreadPool(cpuCount);
		runningTasks = new LinkedList<>();
		if (checkValues()) {
			Core.setStatus("Calculating...");
			startTime = System.nanoTime();
			int lessSections = (int) Math.sqrt(cpuCount);
			int moreSections = (lessSections == 1) ? cpuCount : cpuCount / lessSections;
			if (USE_MORE_THREADS_THAN_CORES) {
				final int temp = lessSections;
				lessSections = moreSections;
				moreSections = 2 * temp;
			}
			int horSections, verSections;
			if (width >= height) {
				horSections = moreSections;
				verSections = lessSections;
			}
			else {
				horSections = lessSections;
				verSections = moreSections;
			}
			final double realWidth = (maxReal - minReal) / horSections;
			final double imagHeight = (maxImag - minImag) / verSections;
			final int sectionWidth = width / horSections;
			final int sectionHeight = height / verSections;
			boolean canRecycleSubimages = subImages != null && subImages.length == horSections * verSections;
			if (canRecycleSubimages) {
				outer: for (int x = 0; x < horSections; x++)
					for (int y = 0; y < verSections; y++) {
						SubImage img = subImages[x * verSections + y];
						if (!(img != null && img.offsetX == x * sectionWidth && img.offsetY == (verSections - y - 1)
								* sectionHeight)) {
							canRecycleSubimages = false;
							break outer;
						}
					}
			}
			if (canRecycleSubimages)
				// clear subImages
				for (SubImage subImage : subImages) {
					Graphics g = subImage.subImage.getGraphics();
					g.setColor(Color.black);
					g.fillRect(0, 0, subImage.subImage.getWidth(null), subImage.subImage.getHeight(null));
				}
			else
				subImages = new SubImage[horSections * verSections];
			try {
				for (int x = 0; x < horSections; x++)
					for (int y = 0; y < verSections; y++) {
						// TODO check if zoom settings will cause rounding
						// errors due to limited computational accuracy
						int currentWidth = x == horSections - 1 ? sectionWidth + width % horSections : sectionWidth;
						int currentHeight = y == 0 ? sectionHeight + height % verSections : sectionHeight;
						SubImage subImage;
						if (canRecycleSubimages)
							subImage = subImages[x * verSections + y];
						else {
							BufferedImage image = new BufferedImage(currentWidth, currentHeight, imageType);
							subImage = new SubImage(x * sectionWidth, (verSections - y - 1) * sectionHeight, image);
						}
						final CifImageMaker maker = imageMakerClass.getConstructor(int.class, int.class, double.class,
								double.class, double.class, double.class, int.class, BufferedImage.class, int.class,
								int.class, ColorPalette.class, byte.class, CifFractal.class).newInstance(currentWidth,
								currentHeight, minReal + x * realWidth,
								x == horSections - 1 ? maxReal : minReal + (x + 1) * realWidth,
								minImag + y * imagHeight,
								y == verSections - 1 ? maxImag : minImag + (y + 1) * imagHeight, maxPasses,
								subImage.subImage, 0, 0, palette, superSamplingFactor, this);
						runningTasks.add(executorService.submit(maker));
						if (!canRecycleSubimages)
							subImages[x * verSections + y] = subImage;
					}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			new Thread(new Runnable() {
				@Override
				public void run() {
					awaitCalculation();
					stopTime = System.nanoTime();
					for (ActionListener listener : calculationFinishedListeners)
						listener.actionPerformed(null);
				}
			}).start();
		}
		else {
			throw new IllegalStateException("Invalid values!");
		}
	}

	boolean isRunning() {
		if (runningTasks == null)
			return false;
		for (Future<?> f : runningTasks)
			if (!f.isDone())
				return true;
		return false;
	}

	private boolean checkValues() {
		return maxReal > minReal && maxImag > minImag && superSamplingFactor > 0 && palette != null && maxPasses > 0;
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		CifParams params = getParams();
		switch (optionName) {
			case "width":
				setImageSize(new Dimension(Integer.parseInt(optionContent), getImageSize().height));
				return;
			case "height":
				setImageSize(new Dimension(getImageSize().width, Integer.parseInt(optionContent)));
				return;
			case "minReal":
				params = params.copyChangeMinReal(Double.parseDouble(optionContent));
				break;
			case "maxReal":
				params = params.copyChangeMaxReal(Double.parseDouble(optionContent));
				break;
			case "minImag":
				params = params.copyChangeMinImag(Double.parseDouble(optionContent));
				break;
			case "maxImag":
				params = params.copyChangeMaxImag(Double.parseDouble(optionContent));
				break;
			case "maxPasses":
				params = params.copyChangeMaxPasses(Integer.parseInt(optionContent));
				break;
			case "superSamplingFactor":
				params = params.copyChangeSuperSamplingFactor(Byte.parseByte(optionContent));
				break;
			default:
				throw new IllegalCommandLineException(
						"Unknown option \""
								+ optionName
								+ "\" for fractal "
								+ getClass().getSimpleName()
								+ "! Known options: width, height, minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor");
		}
		setParams(params, false);
	}

	/**
	 * Determines the area that should be included in the start area.
	 * <p>
	 * Due to different aspect ratios, the actual starting area will most likely be a superarea (never a subarea, or a
	 * partially disjunct area) of this area.
	 * 
	 * @return A double-precision rectangle specifying the start area; x axis is real, and y axis is imaginary.
	 */
	protected abstract java.awt.geom.Rectangle2D.Double getStartArea();

	@Override
	public void shutdown() {
		executorService.shutdown();
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

	public void setImageSize(Dimension d) {
		width = d.width;
		height = d.height;
		if (canvas != null) {
			canvas.setSize(d);
			canvas.setPreferredSize(d);
		}
	}

	@Override
	public Dimension getImageSize() {
		return new Dimension(width, height);
	}

	CifParams getParams() {
		return new CifParams(minReal, maxReal, minImag, maxImag, maxPasses, superSamplingFactor);
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
		if (canvas != null && history != null) {
			if (addToHistory)
				history.add(params);
			undoMenuItem.setEnabled(history.canUndo());
			redoMenuItem.setEnabled(history.canRedo());
		}
	}

	public void awaitCalculation() {
		try {
			for (Future<?> f : runningTasks)
				f.get();
		}
		catch (InterruptedException | ExecutionException | NullPointerException e) {
			// do nothing
			// a NPE indicates no running tasks, in which case returning immediately is the intended result
		}
	}

	void initDefaultValues() {
		Rectangle2D.Double start = getStartArea();
		Dimension imageSize = getImageSize();
		double startAR = start.width / start.height;
		double targetAR = imageSize.width / (double) imageSize.height;
		if (startAR > targetAR) {
			double startCenter = start.y + (start.height / 2);
			start.height = start.width / targetAR;
			start.y = startCenter - start.height / 2;
		}
		else if (targetAR > startAR) {
			double startCenter = start.x + (start.width / 2);
			start.width = targetAR * start.height;
			start.x = startCenter - start.width / 2;
		}
		minReal = start.x;
		maxReal = start.x + start.width;
		minImag = start.y;
		maxImag = start.y + start.height;
		palette = new SimplePalette();
		superSamplingFactor = 1;
		maxPasses = 1000;
	}
}

class SubImage {
	final int			offsetX;
	final int			offsetY;
	final BufferedImage	subImage;

	SubImage(int offsetX, int offsetY, BufferedImage subImage) {
		super();
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.subImage = subImage;
	}
}
