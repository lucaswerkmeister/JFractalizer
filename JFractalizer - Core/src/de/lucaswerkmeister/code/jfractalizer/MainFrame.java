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
import java.io.IOException;

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

    MenuBar menuBar;
    Menu fileMenu, fractalMenu, colorPaletteMenu;
    JColorChooser colorChooser;
    int zoomMenuX, zoomMenuY;
    private final Label statusBar;

    private MainFrame(String[] args) {
	super("JFractalizer");

	boolean useGUI = true;
	boolean askForClasses = true;

	// Read command line args
	if (args.length > 0) {
	    askForClasses = false;
	    File file = new File(args[0]);
	    boolean isFile = file.exists();
	    if (!isFile) {
		// Check whether the file CAN exist
		try {
		    isFile = file.createNewFile();
		} catch (IOException e) {
		    isFile = false;
		}
	    }
	    if (isFile) {
		// Specifies input file
		if (file.exists()) {
		    try {
			loadFile(file);
		    } catch (IOException e) {
			System.out
				.println("Something went wrong while loading the file - perhaps the JFractalizer does not have the necessary permissions to read it?");
			e.printStackTrace();
			System.exit(1);
		    } catch (SAXException e) {
			System.out
				.println("Something went wrong while parsing the file - perhaps it contains invalid XML? (If you want to test that, most modern browsers can check XML for validity.)");
			e.printStackTrace();
			System.exit(1);
		    } catch (ParserConfigurationException e) {
			System.out
				.println("Something went wrong while initializing the file loader. If you see this, please contact the developer.");
			e.printStackTrace();
			System.exit(1);
		    }
		} else {
		    System.out.println("The file you specified for loading ("
			    + args[0] + ") does not exist!");
		    System.exit(1);
		}
	    } else {
		// Specifies FractalProvider and ColorPalette class
		String[] fpCpArgs = args[0].split(":");
		if (fpCpArgs.length < 2) {
		    System.out
			    .println("The FractalProvider and ColorPalette could not be parsed as there were not enough names specified!");
		    System.exit(1);
		}
		try {
		    setCurrentProvider((FractalProvider) Class.forName(
			    fpCpArgs[0]).newInstance());
		} catch (InstantiationException | IllegalAccessException
			| ClassNotFoundException e) {
		    System.out
			    .println("Something went wrong while loading the specified FractalProvider class!");
		    e.printStackTrace();
		    System.exit(1);
		} catch (ClassCastException e) {
		    System.out
			    .println("The class you specified is not a FractalProvider!");
		    e.printStackTrace();
		    System.exit(1);
		}
		try {
		    setCurrentColorPalette((ColorPalette) Class.forName(
			    fpCpArgs[1]).newInstance());
		} catch (InstantiationException | IllegalAccessException
			| ClassNotFoundException e) {
		    System.out
			    .println("Something went wrong while loading the specified ColorPalette class!");
		    e.printStackTrace();
		    System.exit(1);
		} catch (ClassCastException e) {
		    System.out
			    .println("The class you specified is not a ColorPalette!");
		    e.printStackTrace();
		    System.exit(1);
		}
		if (fpCpArgs.length > 2) {
		    // Pass additional arguments to provider and palette
		    currentProvider.handleCommandLineArgs(fpCpArgs[2]);
		    if (fpCpArgs.length > 3) {
			currentColorPalette.handleCommandLineArgs(fpCpArgs[3]);
			if (fpCpArgs.length > 4)
			    System.out
				    .println("Unnecessary information detected: The JFractalizer only processes at most four parts in the \"provider:palette:args:args\" parts, but you specified more. Incorrect command line?");
		    }
		}
	    }
	}

	if (useGUI) {
	    // Init GUI
	    statusBar = new Label("Calculating...");
	    setLayout(new BorderLayout());
	    if (askForClasses) {
		// Let the user choose the fractal
		final ClassChooserDialog<FractalProvider> fractalChooserDialog = new ClassChooserDialog<>(
			this, "Choose Fractal", FractalProvider.class);
		fractalChooserDialog.setVisible(true);
		try {
		    setCurrentProvider(fractalChooserDialog
			    .getSelectedService());
		} catch (final NullPointerException e) {
		    // Do nothing, currentColorPalette wasn't set
		}
		// Let the user choose the color palette
		final ClassChooserDialog<ColorPalette> colorPaletteDialog = new ClassChooserDialog<>(
			this, "Choose Color Palette", ColorPalette.class);
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

    void loadFile(File file) throws SAXException, IOException,
	    ParserConfigurationException {
	final SAXParser saxParser = SAXParserFactory.newInstance()
		.newSAXParser();
	final FractXmlLoader loader = new FractalClassReader();
	saxParser.parse(file, loader);
	MainFrame.getInstance().setCurrentProvider(loader.getProvider());
	final FractXmlPaletteLoader colorPaletteLoader = new PaletteClassReader();
	saxParser.parse(file, colorPaletteLoader);
	MainFrame.getInstance().setCurrentColorPalette(
		colorPaletteLoader.getPalette());
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
	removeAll();
	add(newProvider.getCanvas(), BorderLayout.CENTER);
	add(statusBar, BorderLayout.SOUTH);
	initMenu();
	initContextMenu();
	pack();
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
	final MenuItem saveSetup = new MenuItem("Save Setup", new MenuShortcut(
		KeyEvent.VK_S));
	saveSetup.addActionListener(listener);
	fileMenu.add(saveSetup);
	final MenuItem loadSetup = new MenuItem("Load Setup", new MenuShortcut(
		KeyEvent.VK_O));
	loadSetup.addActionListener(listener);
	fileMenu.add(loadSetup);
	final MenuItem saveImage = new MenuItem("Save Image", new MenuShortcut(
		KeyEvent.VK_P));
	saveImage.addActionListener(listener);
	fileMenu.add(saveImage);
	fileMenu.addSeparator();
	final MenuItem exit = new MenuItem("Exit");
	exit.addActionListener(listener);
	fileMenu.add(exit);
	menuBar.add(fileMenu);

	fractalMenu = new Menu("Fractal");
	final MenuItem chooseFractal = new MenuItem("Choose Fractal...",
		new MenuShortcut(KeyEvent.VK_C));
	chooseFractal.addActionListener(listener);
	fractalMenu.add(chooseFractal);
	fractalMenu.addSeparator();
	menuBar.add(fractalMenu);

	colorPaletteMenu = new Menu("Color Palette");
	final MenuItem chooseColorPalette = new MenuItem(
		"Choose Color Palette...",
		new MenuShortcut(KeyEvent.VK_C, true));
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
	final Menu zoomOutCurrentPos = new Menu(
		ZoomMenuListener.USE_COORDINATES);
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