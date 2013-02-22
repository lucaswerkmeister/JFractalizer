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

import javax.swing.JColorChooser;

public class MainFrame extends Frame {
	private static final long serialVersionUID = 8587484082717377870L;
	private static MainFrame instance;
	MenuBar menuBar;
	Menu fileMenu, fractalMenu, colorPaletteMenu;
	JColorChooser colorChooser;
	int zoomMenuX, zoomMenuY;
	private final Label statusBar;

	MainFrame(boolean askForFractalClass, boolean askForPaletteClass) {
		super("JFractalizer");
		instance = this;

		// Init GUI
		statusBar = new Label("Calculating...");
		setLayout(new BorderLayout());
		if (askForFractalClass) {
			// Let the user choose the fractal
			final ClassChooserDialog<Fractal> fractalChooserDialog = new ClassChooserDialog<>(
					this, "Choose Fractal", Fractal.class);
			fractalChooserDialog.setVisible(true);
			try {
				Core.setCurrentFractal(fractalChooserDialog
						.getSelectedService());
			} catch (final NullPointerException e) {
				// Do nothing, currentColorPalette wasn't set
			}
		}
		if (askForPaletteClass) {
			// Let the user choose the color palette
			final ClassChooserDialog<ColorPalette> colorPaletteDialog = new ClassChooserDialog<>(
					this, "Choose Color Palette", ColorPalette.class);
			colorPaletteDialog.setVisible(true);
			Core.setCurrentColorPalette(colorPaletteDialog.getSelectedService());
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		add(Core.getCurrentFractal().getCanvas(), BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		initMenu();
		initContextMenu();
		pack();
		setVisible(true);
		colorChooser = new JColorChooser();
	}

	void initMenu() {
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

		Core.getCurrentFractal().initMenu(fractalMenu);
		Core.getCurrentColorPalette().initMenu(colorPaletteMenu,
				Core.getCurrentFractal(), this);

		setMenuBar(menuBar);
	}

	private void initContextMenu() {
		final PopupMenu menu = new PopupMenu();

		final MenuItem center;
		if (Core.getCurrentFractal() instanceof ZoomableFractal) {
			final short[] zooms = new short[] { 1, 2, 5, 10, 25, 50, 100 };
			final Menu zoomIn = new Menu(ZoomMenuListener.ZOOM_IN);
			final Menu zoomOut = new Menu(ZoomMenuListener.ZOOM_OUT);
			final Menu zoomInCurrentPos = new Menu(
					ZoomMenuListener.USE_COORDINATES);
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
			center = new MenuItem(ZoomMenuListener.CENTER_NO_ZOOM);
			center.addActionListener(listener);
			menu.add(center);
			menu.addSeparator();
		} else
			center = null;
		final Canvas c = Core.getCurrentFractal().getCanvas();
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

		Core.getCurrentFractal().initContextMenu(menu);
		if (!menu.getItem(menu.getItemCount() - 2).equals(center)) // if the
																	// last item
																	// before
																	// the
																	// separator
																	// is the
																	// "center"
																	// MenuItem,
																	// then the
																	// Fractal
																	// didn't
																	// add any
																	// MenuItems,
																	// and we
																	// don't
																	// need the
																	// second
																	// separator.
			menu.addSeparator();
		menu.add("Cancel");
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

	public void reset() {
		removeAll();
		add(Core.getCurrentFractal().getCanvas(), BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		initMenu();
		initContextMenu();
		pack();
	}
}