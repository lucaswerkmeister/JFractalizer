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

import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JColorChooser;

public class MainFrame extends Frame
{
	private static final long		serialVersionUID	= 8587484082717377870L;
	private static final MainFrame	instance			= new MainFrame();
	private FractalProvider			currentProvider;
	private ColorPalette			currentColorPalette;
	MenuBar							menuBar;
	Menu							fileMenu, fractalMenu, colorPaletteMenu;
	JColorChooser					colorChooser;

	private MainFrame()
	{
		super("JFractalizer");
		// Let the user choose the fractal
		final ClassChooserDialog<FractalProvider> fractalChooserDialog = new ClassChooserDialog<>(this, "Choose Fractal", FractalProvider.class);
		fractalChooserDialog.setVisible(true);
		try
		{
			setCurrentProvider(fractalChooserDialog.getSelectedService());
		}
		catch (NullPointerException e)
		{
			// Do nothing, currentColorPalette wasn't set
		}
		// Let the user choose the color palette
		final ClassChooserDialog<ColorPalette> colorPaletteDialog = new ClassChooserDialog<>(this, "Choose Color Palette", ColorPalette.class);
		colorPaletteDialog.setVisible(true);
		setCurrentColorPalette(colorPaletteDialog.getSelectedService());
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				System.exit(0);
			}
		});
		initMenu();
		pack();
		setVisible(true);
		colorChooser = new JColorChooser();
	}

	/**
	 * @return the currentProvider
	 */
	FractalProvider getCurrentProvider()
	{
		return currentProvider;
	}

	/**
	 * @param newProvider
	 *            the currentProvider to set
	 */
	void setCurrentProvider(final FractalProvider newProvider)
	{
		currentProvider = newProvider;
		removeAll();
		add(newProvider.getCanvas());
		initMenu();
		pack();
	}

	void setCurrentColorPalette(final ColorPalette newPalette)
	{
		currentColorPalette = newPalette;
		initMenu();
		currentProvider.cancelCalculation();
		currentProvider.setColorPalette(newPalette);
		currentProvider.startCalculation();
	}

	private void initMenu()
	{
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
		menuBar.add(fileMenu);

		fractalMenu = new Menu("Fractal");
		final MenuItem chooseFractal = new MenuItem("Choose Fractal...", new MenuShortcut(KeyEvent.VK_F));
		chooseFractal.addActionListener(listener);
		fractalMenu.add(chooseFractal);
		menuBar.add(fractalMenu);

		colorPaletteMenu = new Menu("Color Palette");
		final MenuItem chooseColorPalette = new MenuItem("Choose Color Palette...", new MenuShortcut(KeyEvent.VK_C));
		chooseColorPalette.addActionListener(listener);
		colorPaletteMenu.add(chooseColorPalette);
		menuBar.add(colorPaletteMenu);

		currentProvider.initMenu(fractalMenu);
		currentColorPalette.initMenu(colorPaletteMenu, currentProvider, this);

		setMenuBar(menuBar);
	}

	public static void main(final String[] args)
	{
		MainFrame.getInstance();
	}

	static MainFrame getInstance()
	{
		return instance;
	}
}
