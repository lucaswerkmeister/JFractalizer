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
package de.lucaswerkmeister.code.jfractalizer;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
	private static FractalProvider currentProvider;
	private static ColorPalette currentColorPalette;

	private static boolean showGui = true;
	private static MainFrame gui;
	private static boolean running = false;

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
	public static RenderedImage getImage() {
		if (MainFrame.getInstance() == null)
			return null;
		return getCurrentProvider().getImage();
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
		getCurrentProvider().stopCalculation();
	}

	/**
	 * Starts calculation of the current fractal image.
	 */
	public static void startCalculation() {
		if (running)
			getCurrentProvider().startCalculation();
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
	 * Changes the current provider to the specified type.
	 * 
	 * @param fractalProviderClass
	 *            The class of the new provider type.
	 * @param params
	 *            Any parameters that will be passed to the new provider.
	 * @throws ReflectiveOperationException
	 *             If anything goes wrong instantiating the new provider.
	 */
	public static void changeProvider(Class<? extends FractalProvider> fractalProviderClass, Object... params)
			throws ReflectiveOperationException, IllegalArgumentException {
		stopCalculation();
		setCurrentProvider(fractalProviderClass.newInstance());
		getCurrentProvider().onProviderChange(params);
		startCalculation();
	}

	/**
	 * @return the currentProvider
	 */
	static FractalProvider getCurrentProvider() {
		return currentProvider;
	}

	/**
	 * @param newProvider
	 *            the currentProvider to set
	 */
	static void setCurrentProvider(final FractalProvider newProvider) {
		currentProvider = newProvider;
		if (showGui)
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
		currentProvider.setColorPalette(newPalette);
		startCalculation();
	}

	private static void handleOption(String realm, String option) {
		String optionName = option.substring(0, option.indexOf('='));
		String optionContent = option.substring(option.indexOf('=') + 1);
		switch (realm) {
		case "":
			throw new IllegalCommandLineException("No realm specified!");
		case "ui":
			switch (optionName) {
			case "show-gui":
				switch (optionContent) {
				case "true":
					showGui = true;
					return;
				case "false":
					showGui = false;
					return;
				default:
					throw new IllegalCommandLineException("\"" + optionContent + "\" is not a valid boolean value!");
				}
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
					return;
				} catch (SAXException | IOException | ParserConfigurationException e) {
					fatalError("Something went wrong while loading FractXML file \"" + optionContent + "\"!", e);
				}
			case "stdin":
				try {
					load(System.in);
					return;
				} catch (SAXException | IOException | ParserConfigurationException e) {
					fatalError("Something went wrong while reading FractXML stream from stdin!", e);
				}
			case "fractal":
				Class<?> fractalClass;
				try {
					fractalClass = Class.forName(optionContent);
				} catch (ClassNotFoundException e) {
					throw new IllegalCommandLineException("Class \"" + optionContent + "\" was not found!");
				}
				if (!FractalProvider.class.isAssignableFrom(fractalClass))
					throw new IllegalCommandLineException("\"" + optionContent + "\" is not a FractalProvider class!");
				try {
					Core.setCurrentProvider((FractalProvider) fractalClass.newInstance());
					return;
				} catch (InstantiationException e) {
					throw new IllegalCommandLineException("An error occured while creating instance of class \""
							+ optionContent + "\"!", e);
				} catch (IllegalAccessException e) {
					throw new IllegalCommandLineException("Can't access default constructor of class \""
							+ optionContent + "\"!");
				}
			case "palette":
				Class<?> paletteClass;
				try {
					paletteClass = Class.forName(optionContent);
				} catch (ClassNotFoundException e) {
					throw new IllegalCommandLineException("Class \"" + optionContent + "\" was not found!");
				}
				if (!ColorPalette.class.isAssignableFrom(paletteClass))
					throw new IllegalCommandLineException("\"" + optionContent + "\" is not a ColorPalette class!");
				try {
					Core.setCurrentColorPalette((ColorPalette) paletteClass.newInstance());
					return;
				} catch (InstantiationException e) {
					throw new IllegalCommandLineException("An error occured while creating instance of class \""
							+ optionContent + "\"!", e);
				} catch (IllegalAccessException e) {
					throw new IllegalCommandLineException("Can't access default constructor of class \""
							+ optionContent + "\"!");
				}
			}
		case "fractArgs":
			Core.getCurrentProvider().handleCommandLineOption(option);
			return;
		case "paletteArgs":
			Core.getCurrentColorPalette().handleCommandLineOption(option);
			return;
		}
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
		final FractXmlLoader loader = new FractalClassReader();
		saxParser.parse(stream, loader);
		Core.setCurrentProvider(loader.getProvider());
		final FractXmlPaletteLoader colorPaletteLoader = new PaletteClassReader();
		saxParser.parse(stream, colorPaletteLoader);
		Core.setCurrentColorPalette(colorPaletteLoader.getPalette());
	}

	public static void main(String[] args) {
		// Read command line args
		String realm = "";
		for (String arg : args)
			if (arg.startsWith("--"))
				realm = arg.substring("--".length());
			else
				handleOption(realm, arg);
		// Create GUI
		if (showGui)
			gui = new MainFrame(currentProvider == null, currentColorPalette == null);
		// Start
		running = true;
		startCalculation();
	}
}
