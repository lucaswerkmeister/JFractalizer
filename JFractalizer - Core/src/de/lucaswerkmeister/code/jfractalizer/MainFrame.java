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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.PopupMenu;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JColorChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

public class MainFrame extends Frame {
	private static final long serialVersionUID = 8587484082717377870L;
	private static MainFrame instance;
	private FractalProvider currentProvider;
	private ColorPalette currentColorPalette;

	private boolean showGui = true;
	MenuBar menuBar;
	Menu fileMenu, fractalMenu, colorPaletteMenu;
	JColorChooser colorChooser;
	int zoomMenuX, zoomMenuY;
	private final Label statusBar;

	private MainFrame(String[] args) {
		super("JFractalizer");

		boolean askForClasses = true;

		// Read command line args
		String realm = "";
		for (String arg : args)
			if (arg.startsWith("--"))
				realm = arg.substring("--".length());
			else
				handleOption(realm, arg);

		if (showGui) {
			// Init GUI
			statusBar = new Label("Calculating...");
			setLayout(new BorderLayout());
			if (askForClasses) {
				// Let the user choose the fractal
				final ClassChooserDialog<FractalProvider> fractalChooserDialog = new ClassChooserDialog<>(this,
						"Choose Fractal", FractalProvider.class);
				fractalChooserDialog.setVisible(true);
				try {
					setCurrentProvider(fractalChooserDialog.getSelectedService());
				} catch (final NullPointerException e) {
					// Do nothing, currentColorPalette wasn't set
				}
				// Let the user choose the color palette
				final ClassChooserDialog<ColorPalette> colorPaletteDialog = new ClassChooserDialog<>(this,
						"Choose Color Palette", ColorPalette.class);
				colorPaletteDialog.setVisible(true);
				currentColorPalette = colorPaletteDialog.getSelectedService();
			}
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(final WindowEvent e) {
					System.exit(0);
				}
			});
			initMenu();
			initContextMenu();
			pack();
			setVisible(true);
			colorChooser = new JColorChooser();
		} else {
			statusBar = null; // TODO this is just so the code compiles, think
			// of something better!
		}
	}

	private void handleOption(String realm, String option) {
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
					setCurrentProvider((FractalProvider) fractalClass.newInstance());
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
					setCurrentColorPalette((ColorPalette) paletteClass.newInstance());
					return;
				} catch (InstantiationException e) {
					throw new IllegalCommandLineException("An error occured while creating instance of class \""
							+ optionContent + "\"!", e);
				} catch (IllegalAccessException e) {
					throw new IllegalCommandLineException("Can't access default constructor of class \""
							+ optionContent + "\"!");
				}
			}
		}
	}

	private void warn(String warning) {
		System.out.println(warning);
	}

	private void error(String message) {
		System.out.println(message);
	}

	private void error(String message, Throwable t) {
		System.out.println(message);
		t.printStackTrace();
	}

	private void fatalError(String message) {
		error(message);
		System.exit(1);
	}

	private void fatalError(String message, Throwable t) {
		error(message, t);
		System.exit(1);
	}

	void loadFile(File file) throws SAXException, IOException, ParserConfigurationException {
		load(new FileInputStream(file));
	}

	void load(InputStream stream) throws SAXException, IOException, ParserConfigurationException {
		final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		final FractXmlLoader loader = new FractalClassReader();
		saxParser.parse(stream, loader);
		MainFrame.getInstance().setCurrentProvider(loader.getProvider());
		final FractXmlPaletteLoader colorPaletteLoader = new PaletteClassReader();
		saxParser.parse(stream, colorPaletteLoader);
		MainFrame.getInstance().setCurrentColorPalette(colorPaletteLoader.getPalette());
	}

	/**
	 * @return the currentProvider
	 */
	FractalProvider getCurrentProvider() {
		return currentProvider;
	}

	/**
	 * @param newProvider
	 *            the currentProvider to set
	 */
	void setCurrentProvider(final FractalProvider newProvider) {
		currentProvider = newProvider;
		if (showGui) {
			removeAll();
			add(newProvider.getCanvas(), BorderLayout.CENTER);
			add(statusBar, BorderLayout.SOUTH);
			initMenu();
			initContextMenu();
			pack();
		}
	}

	/**
	 * @return the currentColorPalette
	 */
	public ColorPalette getCurrentColorPalette() {
		return currentColorPalette;
	}

	void setCurrentColorPalette(final ColorPalette newPalette) {
		currentColorPalette = newPalette;
		initMenu();
		currentProvider.stopCalculation();
		currentProvider.setColorPalette(newPalette);
		currentProvider.startCalculation();
	}

	private void initMenu() {
		final MenuListener listener = new MenuListener();
		menuBar = new MenuBar();

		fileMenu = new Menu("File");
		final MenuItem saveSetup = new MenuItem("Save Setup", new MenuShortcut(KeyEvent.VK_S));
		saveSetup.addActionListener(listener);
		fileMenu.add(saveSetup);
		final MenuItem loadSetup = new MenuItem("Load Setup", new MenuShortcut(KeyEvent.VK_O));
		loadSetup.addActionListener(listener);
		fileMenu.add(loadSetup);
		final MenuItem saveImage = new MenuItem("Save Image", new MenuShortcut(KeyEvent.VK_P));
		saveImage.addActionListener(listener);
		fileMenu.add(saveImage);
		fileMenu.addSeparator();
		final MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(listener);
		fileMenu.add(exit);
		menuBar.add(fileMenu);

		fractalMenu = new Menu("Fractal");
		final MenuItem chooseFractal = new MenuItem("Choose Fractal...", new MenuShortcut(KeyEvent.VK_C));
		chooseFractal.addActionListener(listener);
		fractalMenu.add(chooseFractal);
		fractalMenu.addSeparator();
		menuBar.add(fractalMenu);

		colorPaletteMenu = new Menu("Color Palette");
		final MenuItem chooseColorPalette = new MenuItem("Choose Color Palette...", new MenuShortcut(KeyEvent.VK_C,
				true));
		chooseColorPalette.addActionListener(listener);
		colorPaletteMenu.add(chooseColorPalette);
		colorPaletteMenu.addSeparator();
		menuBar.add(colorPaletteMenu);

		currentProvider.initMenu(fractalMenu);
		currentColorPalette.initMenu(colorPaletteMenu, currentProvider, this);

		setMenuBar(menuBar);
	}

	private void initContextMenu() {
		final PopupMenu menu = new PopupMenu();

		final short[] zooms = new short[] {1, 2, 5, 10, 25, 50, 100 };

		final Menu zoomIn = new Menu(ZoomMenuListener.ZOOM_IN);
		final Menu zoomOut = new Menu(ZoomMenuListener.ZOOM_OUT);
		final Menu zoomInCurrentPos = new Menu(ZoomMenuListener.USE_COORDINATES);
		final Menu zoomInCenter = new Menu(ZoomMenuListener.USE_CENTER);
		final Menu zoomOutCurrentPos = new Menu(ZoomMenuListener.USE_COORDINATES);
		final Menu zoomOutCenter = new Menu(ZoomMenuListener.USE_CENTER);
		MenuItem m;
		final ZoomMenuListener listener = new ZoomMenuListener();
		for (final short s : zooms) {
			final String t = s + "%";
			m = new MenuItem(t);
			m.addActionListener(listener);
			zoomInCurrentPos.add(m);
			m = new MenuItem(t);
			m.addActionListener(listener);
			zoomInCenter.add(m);
			m = new MenuItem(t);
			m.addActionListener(listener);
			zoomOutCurrentPos.add(m);
			m = new MenuItem(t);
			m.addActionListener(listener);
			zoomOutCenter.add(m);
		}
		zoomIn.add(zoomInCurrentPos);
		zoomIn.add(zoomInCenter);
		zoomOut.add(zoomOutCurrentPos);
		zoomOut.add(zoomOutCenter);
		menu.add(zoomIn);
		menu.add(zoomOut);

		final MenuItem center = new MenuItem(ZoomMenuListener.CENTER_NO_ZOOM);
		center.addActionListener(listener);
		menu.add(center);

		final Canvas c = currentProvider.getCanvas();
		c.add(menu);
		c.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e.getX(), e.getY());
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (e.isPopupTrigger())
					showMenu(e.getX(), e.getY());
			}

			private void showMenu(final int x, final int y) {
				menu.show(c, x, y);
				zoomMenuX = x;
				zoomMenuY = y;
			}
		});

		menu.addSeparator();
		currentProvider.initContextMenu(menu);
		if (!menu.getItem(menu.getItemCount() - 2).equals(center)) // if the
			// last item
			// before the
			// separator
			// is the
			// "center"
			// MenuItem,
			// then the
			menu.addSeparator(); // FractalProvider didn't add any MenuItems,
		// and we don't need the second separator.
		menu.add("Cancel");
	}

	public static void main(final String[] args) {
		instance = new MainFrame(args);
	}

	static MainFrame getInstance() {
		return instance;
	}

	void setStatus(final String status, final Color color) {
		statusBar.setText(status);
		statusBar.setForeground(color);
		pack();
		repaint();
	}
}