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
package de.lucaswerkmeister.jfractalizer;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * Acts as a wrapper for all functions that plugins may wish to invoke on the
 * core.
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
	private static String currentFormat = "raw-RGB";
	private static Set<Output> outputs = new HashSet<>();
	private static Set<ActionListener> calculationFinishedListeners = new HashSet<>();

	/**
	 * <code>private</code> constructor so the class can't be instantiated.
	 */
	private Core() {
	}

	/**
	 * Gets the image currently displayed by the main window, whether it is
	 * completely calculated or not.
	 * 
	 * @return The image currently displayed by the main window.
	 */
	public static BufferedImage getImage() {
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
		if (currentProvider != null)
			currentProvider.stopCalculation();
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
	 * Sets the text displayed by the status bar of the JFractalizer and its
	 * color.
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
	public static void changeProvider(
			Class<? extends FractalProvider> fractalProviderClass,
			Object... params) throws ReflectiveOperationException,
			IllegalArgumentException {
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
		currentProvider.setColorPalette(newPalette);
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
							o.writeImage(getCurrentProvider().getImage(), 0);
						} catch (IOException e) {
							fatalError("Couldn't write image, aborting!", e);
						}
					System.exit(0);
				}
			});
		case "film":
			calculationFinishedListeners.add(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					for (Output o : outputs)
						try {
							o.writeImage(getCurrentProvider().getImage(),
									getFrameCount());
						} catch (IOException e) {
							fatalError("Couldn't write image no. "
									+ getFrameCount() + ", aborting!", e);
						}
				}
			});
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
			throw new IllegalCommandLineException("Unknown option \"" + option
					+ "\" in realm \"" + realm + "\"!");
		case "input":
			switch (optionName) {
			case "file":
				File file = new File(optionContent);
				if (!file.exists())
					throw new IllegalCommandLineException("File \""
							+ optionContent
							+ "\" does not exist! (Assumed absolute path: \""
							+ file.getAbsolutePath() + "\")");
				if (!optionContent.endsWith(".fractXml"))
					warn("Warning: You are trying to read a file which is apparently not a FractXML file!");
				try {
					loadFile(file);
					return;
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					fatalError(
							"Something went wrong while loading FractXML file \""
									+ optionContent + "\"!", e);
				}
			case "stdin":
				try {
					load(System.in);
					return;
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					fatalError(
							"Something went wrong while reading FractXML stream from stdin!",
							e);
				}
			case "fractal":
				Class<?> fractalClass;
				try {
					fractalClass = Class.forName(optionContent);
				} catch (ClassNotFoundException e) {
					throw new IllegalCommandLineException("Class \""
							+ optionContent + "\" was not found!");
				}
				if (!FractalProvider.class.isAssignableFrom(fractalClass))
					throw new IllegalCommandLineException("\"" + optionContent
							+ "\" is not a FractalProvider class!");
				try {
					Core.setCurrentProvider((FractalProvider) fractalClass
							.newInstance());
					return;
				} catch (InstantiationException e) {
					throw new IllegalCommandLineException(
							"An error occured while creating instance of class \""
									+ optionContent + "\"!", e);
				} catch (IllegalAccessException e) {
					throw new IllegalCommandLineException(
							"Can't access default constructor of class \""
									+ optionContent + "\"!");
				}
			case "palette":
				Class<?> paletteClass;
				try {
					paletteClass = Class.forName(optionContent);
				} catch (ClassNotFoundException e) {
					throw new IllegalCommandLineException("Class \""
							+ optionContent + "\" was not found!");
				}
				if (!ColorPalette.class.isAssignableFrom(paletteClass))
					throw new IllegalCommandLineException("\"" + optionContent
							+ "\" is not a ColorPalette class!");
				try {
					Core.setCurrentColorPalette((ColorPalette) paletteClass
							.newInstance());
					return;
				} catch (InstantiationException e) {
					throw new IllegalCommandLineException(
							"An error occured while creating instance of class \""
									+ optionContent + "\"!", e);
				} catch (IllegalAccessException e) {
					throw new IllegalCommandLineException(
							"Can't access default constructor of class \""
									+ optionContent + "\"!");
				}
			}
		case "fractArgs":
			Core.getCurrentProvider().handleCommandLineOption(option,
					optionName, optionContent);
			return;
		case "paletteArgs":
			Core.getCurrentColorPalette().handleCommandLineOption(option);
			return;
		case "output":
			switch (optionName) {
			case "format":
				currentFormat = optionContent;
				return;
			case "file":
				try {
					outputs.add(new SingleFileOutput(currentFormat,
							optionContent));
				} catch (IOException e) {
					error("An error occured while preparing output file \""
							+ optionContent + "\"!", e);
				}
				return;
			case "files":
				outputs.add(new MultipleFilesOutput(currentFormat,
						optionContent));
				return;
			case "stdout":
				outputs.add(new StdoutOutput(currentFormat));
				return;
			}
		}
	}

	private static void endRealm(String name) {
	}

	protected static int getFrameCount() {
		// TODO Auto-generated method stub
		return 0;
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
	static void loadFile(File file) throws SAXException, IOException,
			ParserConfigurationException {
		load(new FileInputStream(file));
	}

	static void load(InputStream stream) throws SAXException, IOException,
			ParserConfigurationException {
		final SAXParser saxParser = SAXParserFactory.newInstance()
				.newSAXParser();
		final FractXmlLoader loader = new FractalClassReader();
		saxParser.parse(stream, loader);
		Core.setCurrentProvider(loader.getProvider());
		final FractXmlPaletteLoader colorPaletteLoader = new PaletteClassReader();
		saxParser.parse(stream, colorPaletteLoader);
		Core.setCurrentColorPalette(colorPaletteLoader.getPalette());
	}

	public static void main(String[] args) throws IOException {
		// Read command line args
		String realm = "";
		for (String arg : args)
			if (arg.startsWith("--")) {
				endRealm(realm);
				realm = arg.substring("--".length());
				startRealm(realm);
			} else
				handleOption(realm, arg);
		if (!showGui
				&& (currentProvider == null || currentColorPalette == null))
			throw new IllegalCommandLineException(
					"If running without GUI, fractal provider and color palette must be provided!");
		// Create GUI
		if (showGui)
			gui = new MainFrame(currentProvider == null,
					currentColorPalette == null);
		// Start
		running = true;
		startCalculation();
		for (ActionListener l : calculationFinishedListeners)
			currentProvider.addCalculationFinishedListener(l);
	}
}

abstract class Output {
	protected final String format;

	protected Output(String format) {
		if (!Arrays.asList("png", "jpg", "raw-ARGB", "raw-BGR")
				.contains(format))
			throw new IllegalCommandLineException("Unknown output format \""
					+ format + "\"!");
		this.format = format;
	}

	public abstract void writeImage(BufferedImage BufferedImage, int count)
			throws IOException;

	protected void write(BufferedImage image, OutputStream stream)
			throws IOException {
		switch (format) {
		case "png":
		case "jpg":
			ImageIO.write(image, format, stream);
			break;
		case "raw-ARGB": {
			BufferedImage newImage = new BufferedImage(image.getWidth(),
					image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			newImage.getGraphics().drawImage(image, 0, 0, null);
			DataBufferInt buffer = (DataBufferInt) newImage.getRaster()
					.getDataBuffer();
			int[] data = buffer.getData();
			ByteBuffer bbuf = ByteBuffer.allocate(data.length * 4);
			bbuf.asIntBuffer().put(data);
			stream.write(bbuf.array());
			break;
		}
		case "raw-BGR": {
			BufferedImage newImage = new BufferedImage(image.getWidth(),
					image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			newImage.getGraphics().drawImage(image, 0, 0, null);
			DataBufferByte buffer = (DataBufferByte) newImage.getRaster()
					.getDataBuffer();
			byte[] data = buffer.getData();
			stream.write(data);
			break;
		}
		}
	}
}

class SingleFileOutput extends Output {
	private OutputStream stream;

	public SingleFileOutput(String format, String filename) throws IOException {
		super(format);
		File file = new File(filename);
		file.createNewFile();
		this.stream = new FileOutputStream(file);
	}

	@Override
	public void writeImage(BufferedImage image, int count) throws IOException {
		write(image, stream);
	}

	@Override
	protected void finalize() throws Throwable {
		stream.close();
		super.finalize();
	}
}

class MultipleFilesOutput extends Output {
	private String filename;

	public MultipleFilesOutput(String format, String filename) {
		super(format);
		this.filename = filename;
	}

	@Override
	public void writeImage(BufferedImage image, int count) throws IOException {
		write(image,
				new FileOutputStream(filename.replace("?",
						Integer.toString(count))));
	}
}

class StdoutOutput extends Output {
	public StdoutOutput(String format) {
		super(format);
	}

	@Override
	public void writeImage(BufferedImage image, int count) throws IOException {
		write(image, System.out);
	}
}