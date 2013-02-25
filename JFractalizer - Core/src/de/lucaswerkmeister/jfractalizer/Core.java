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
package de.lucaswerkmeister.jfractalizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JColorChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * Acts as a wrapper for all functions that plugins may wish to invoke on the core.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public final class Core {
	private static Fractal				currentFractal;
	private static ColorPalette			currentColorPalette;

	private static boolean				showGui							= true;
	private static MainFrame			gui;
	private static boolean				running							= false;
	private static String				currentFormat					= "raw-RGB";
	private static Set<Output>			outputs							= new HashSet<>();
	private static Set<ActionListener>	calculationFinishedListeners	= new HashSet<>();
	private static Camera				camera;

	/**
	 * <code>private</code> constructor so the class can't be instantiated.
	 */
	private Core() {
	}

	/**
	 * Gets the image currently displayed by the main window, whether it is completely calculated or not.
	 * 
	 * @return The image currently displayed by the main window.
	 */
	public static BufferedImage getImage() {
		if (MainFrame.getInstance() == null)
			return null;
		return getCurrentFractal().getImage();
	}

	/**
	 * Gets the global <code>JColorChooser</code> of the JFractalizer.
	 * 
	 * @return The global <code>JColorChooser</code> of the JFractalizer.
	 */
	public static JColorChooser getGlobalColorChooser() {
		if (MainFrame.getInstance() == null)
			return null;
		return MainFrame.getInstance().colorChooser;
	}

	/**
	 * Stops the currently running calculation.
	 */
	public static void stopCalculation() {
		if (currentFractal != null)
			currentFractal.stopCalculation();
	}

	/**
	 * Starts calculation of the current fractal image.
	 */
	public static void startCalculation() {
		if (running)
			getCurrentFractal().startCalculation();
	}

	/**
	 * Sets the text displayed by the status bar of the JFractalizer.
	 * 
	 * @param status
	 *            The new status.
	 */
	public static void setStatus(final String status) {
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().setStatus(status, Color.black);
	}

	/**
	 * Sets the text displayed by the status bar of the JFractalizer and its color.
	 * 
	 * @param status
	 *            The new status.
	 * @param color
	 *            The color of the new status.
	 */
	public static void setStatus(final String status, final Color color) {
		if (MainFrame.getInstance() == null)
			return;
		MainFrame.getInstance().setStatus(status, color);
	}

	/**
	 * Changes the current fractal to the specified type.
	 * 
	 * @param fractalClass
	 *            The class of the new fractal type.
	 * @param params
	 *            Any parameters that will be passed to the new fractal.
	 * @throws ReflectiveOperationException
	 *             If anything goes wrong instantiating the new fractal.
	 */
	public static void changeFractal(Class<? extends Fractal> fractalClass, Object... params)
			throws ReflectiveOperationException, IllegalArgumentException {
		stopCalculation();
		setCurrentFractal(fractalClass.newInstance());
		getCurrentFractal().onFractalChange(params);
		startCalculation();
	}

	/**
	 * @return The current fractal.
	 */
	static Fractal getCurrentFractal() {
		return currentFractal;
	}

	/**
	 * @param newFractal
	 *            The new fractal.
	 */
	static void setCurrentFractal(final Fractal newFractal) {
		currentFractal = newFractal;
		if (showGui && gui != null)
			gui.reset();
	}

	/**
	 * @return the currentColorPalette
	 */
	public static ColorPalette getCurrentColorPalette() {
		return Core.currentColorPalette;
	}

	static void setCurrentColorPalette(final ColorPalette newPalette) {
		currentColorPalette = newPalette;
		if (showGui && gui != null)
			gui.initMenu();
		stopCalculation();
		currentFractal.setColorPalette(newPalette);
		startCalculation();
	}

	private static void startRealm(String name) {
		switch (name) {
			case "image":
				calculationFinishedListeners.add(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						for (Output o : outputs)
							try {
								o.writeImage(getCurrentFractal().getImage());
							}
							catch (IOException e) {
								fatalError("Couldn't write image, aborting!", e);
							}
						System.exit(0);
					}
				});
				break;
		}
	}

	private static void handleOption(String realm, String option) {
		String optionName = option;
		String optionContent = "";
		if (option.contains("=")) {
			optionName = option.substring(0, option.indexOf('='));
			optionContent = option.substring(option.indexOf('=') + 1);
		}
		switch (realm) {
			case "":
				throw new IllegalCommandLineException("No realm specified!");
			case "ui":
				switch (optionName) {
					case "no-gui":
						showGui = false;
						return;
				}
				throw new IllegalCommandLineException("Unknown option \"" + option + "\" in realm \"" + realm + "\"!");
			case "input":
				switch (optionName) {
					case "file":
						File file = new File(optionContent);
						if (!file.exists())
							throw new IllegalCommandLineException("File \"" + optionContent
									+ "\" does not exist! (Assumed absolute path: \"" + file.getAbsolutePath() + "\")");
						if (!optionContent.endsWith(".fractXml"))
							warn("Warning: You are trying to read a file which is apparently not a FractXML file!");
						try {
							loadFile(file);
						}
						catch (SAXException | IOException | ParserConfigurationException e) {
							fatalError("Something went wrong while loading FractXML file \"" + optionContent + "\"!", e);
						}
						return;
					case "stdin":
						try {
							load(System.in);
						}
						catch (SAXException | IOException | ParserConfigurationException e) {
							fatalError("Something went wrong while reading FractXML stream from stdin!", e);
						}
						return;
					case "fractal":
						Class<?> fractalClass;
						try {
							fractalClass = Class.forName(optionContent);
						}
						catch (ClassNotFoundException e) {
							throw new IllegalCommandLineException("Class \"" + optionContent + "\" was not found!");
						}
						if (!Fractal.class.isAssignableFrom(fractalClass))
							throw new IllegalCommandLineException("\"" + optionContent + "\" is not a Fractal class!");
						try {
							Core.setCurrentFractal((Fractal) fractalClass.newInstance());
							return;
						}
						catch (InstantiationException e) {
							throw new IllegalCommandLineException(
									"An error occured while creating instance of class \"" + optionContent + "\"!", e);
						}
						catch (IllegalAccessException e) {
							throw new IllegalCommandLineException("Can't access default constructor of class \""
									+ optionContent + "\"!");
						}
					case "palette":
						Class<?> paletteClass;
						try {
							paletteClass = Class.forName(optionContent);
						}
						catch (ClassNotFoundException e) {
							throw new IllegalCommandLineException("Class \"" + optionContent + "\" was not found!");
						}
						if (!ColorPalette.class.isAssignableFrom(paletteClass))
							throw new IllegalCommandLineException("\"" + optionContent
									+ "\" is not a ColorPalette class!");
						try {
							Core.setCurrentColorPalette((ColorPalette) paletteClass.newInstance());
							return;
						}
						catch (InstantiationException e) {
							throw new IllegalCommandLineException(
									"An error occured while creating instance of class \"" + optionContent + "\"!", e);
						}
						catch (IllegalAccessException e) {
							throw new IllegalCommandLineException("Can't access default constructor of class \""
									+ optionContent + "\"!");
						}
				}
				return;
			case "film":
				if (optionName.equals("camera")) {
					Class<?> cameraClass;
					try {
						cameraClass = Class.forName(optionContent);
					}
					catch (ClassNotFoundException e) {
						throw new IllegalCommandLineException("Class \"" + optionContent + "\" was not found!");
					}
					if (!Camera.class.isAssignableFrom(cameraClass))
						throw new IllegalCommandLineException("\"" + optionContent + "\" is not a Camera class!");
					try {
						camera = (Camera) cameraClass.newInstance();
						return;
					}
					catch (InstantiationException e) {
						throw new IllegalCommandLineException("An error occured while creating instance of class \""
								+ optionContent + "\"!", e);
					}
					catch (IllegalAccessException e) {
						throw new IllegalCommandLineException("Can't access default constructor of class \""
								+ optionContent + "\"!");
					}
				}
				else
					warn("Unknown option \"" + option + "\" in realm --film!");
				break;
			case "fractArgs":
				Core.getCurrentFractal().handleCommandLineOption(option, optionName, optionContent);
				return;
			case "paletteArgs":
				Core.getCurrentColorPalette().handleCommandLineOption(option, optionName, optionContent);
				return;
			case "camArgs":
				if (camera == null)
					warn("No camera specified, ignoring --camArgs option");
				else
					camera.handleCommandLineOption(option, optionName, optionContent);
				break;
			case "output":
				switch (optionName) {
					case "format":
						currentFormat = optionContent;
						return;
					case "file":
						try {
							outputs.add(new SingleFileOutput(currentFormat, optionContent));
						}
						catch (IOException e) {
							error("An error occured while preparing output file \"" + optionContent + "\"!", e);
						}
						return;
					case "files":
						outputs.add(new MultipleFilesOutput(currentFormat, optionContent));
						return;
					case "stdout":
						outputs.add(new StdoutOutput(currentFormat));
						return;
				}
		}
	}

	private static void endRealm(String name) {
	}

	private static void warn(String warning) {
		System.out.println(warning);
	}

	private static void error(String message, Throwable t) {
		System.out.println(message);
		t.printStackTrace();
	}

	private static void fatalError(String message, Throwable t) {
		error(message, t);
		System.exit(1);
	}

	/**
	 * Loads the specified file.
	 * 
	 * @param saveFile
	 *            The file that should be loaded.
	 * @throws SAXException
	 *             If anything goes wrong while loading the file.
	 * @throws IOException
	 *             If anything goes wrong while loading the file.
	 * @throws ParserConfigurationException
	 *             If anything goes wrong while loading the file.
	 */
	static void loadFile(File file) throws SAXException, IOException, ParserConfigurationException {
		load(new FileInputStream(file));
	}

	static void load(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final FractXmlReader reader = new FractXmlReader();
		saxParser.parse(stream, reader);
		Core.setCurrentFractal(reader.getFractal());
		Core.setCurrentColorPalette(reader.getPalette());
	}

	public static void main(String[] args) throws IOException {
		// Read command line args
		String realm = "";
		for (String arg : args)
			if (arg.startsWith("--")) {
				endRealm(realm);
				realm = arg.substring("--".length());
				startRealm(realm);
			}
			else
				handleOption(realm, arg);
		if (!showGui && (currentFractal == null || currentColorPalette == null))
			throw new IllegalCommandLineException(
					"If running without GUI, fractal fractal and color palette must be provided!");
		// Create GUI
		if (showGui)
			gui = new MainFrame(currentFractal == null, currentColorPalette == null);
		if (outputs.size() == 1) {
			Output o = outputs.iterator().next();
			switch (o.format) {
				case "raw-ARGB":
					currentFractal.suggestImageType(BufferedImage.TYPE_INT_ARGB);
					break;
				case "raw-BGR":
					currentFractal.suggestImageType(BufferedImage.TYPE_3BYTE_BGR);
					break;
			}
		}
		if (camera != null) {
			// Film
			for (Output o : outputs)
				camera.addOutput(o);
			camera.startFilming((ZoomableFractal) currentFractal);
			camera.awaitCalculation();
		}
		else {
			// Start regularly
			running = true;
			startCalculation();
			for (ActionListener l : calculationFinishedListeners)
				currentFractal.addCalculationFinishedListener(l);
			currentFractal.awaitCalculation();
		}
		if (!showGui)
			currentFractal.shutdown();
	}
}

class SingleFileOutput extends Output {
	private OutputStream	stream;

	public SingleFileOutput(String format, String filename) throws IOException {
		super(format);
		File file = new File(filename);
		file.createNewFile();
		this.stream = new FileOutputStream(file);
	}

	public SingleFileOutput(String format, String filename, Iterator<Integer> numbers) throws IOException {
		super(format, numbers);
		File file = new File(filename);
		file.createNewFile();
		this.stream = new FileOutputStream(file);
	}

	@Override
	public void writeImage(BufferedImage image) throws IOException {
		write(image, stream);
	}

	@Override
	protected void finalize() throws Throwable {
		stream.close();
		super.finalize();
	}
}

class MultipleFilesOutput extends Output {
	private String	filename;

	public MultipleFilesOutput(String format, String filename) {
		super(format);
		this.filename = filename;
	}

	public MultipleFilesOutput(String format, String filename, Iterator<Integer> numbers) {
		super(format, numbers);
		this.filename = filename;
	}

	@Override
	public void writeImage(BufferedImage image) throws IOException {
		write(image, new FileOutputStream(filename.replace("?", Integer.toString(getNumbers().next()))));
	}
}

class StdoutOutput extends Output {
	public StdoutOutput(String format) {
		super(format);
	}

	@Override
	public void writeImage(BufferedImage image) throws IOException {
		write(image, System.out);
	}
}