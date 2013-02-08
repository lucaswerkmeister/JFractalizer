package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.MenuItem;
import java.awt.PopupMenu;

import de.lucaswerkmeister.jfractalizer.FractXmlLoader;

public class MandelbrotProvider extends CifProvider {
	private final MandelbrotMenuListener listener = new MandelbrotMenuListener(this);

	public MandelbrotProvider() {
		canvas = new CifCanvas<>(this, MandelbrotImageMaker_NoHoles.class);
		menuListener = new CifMenuListener(this, canvas);
	}

	@Override
	public FractXmlLoader getFractXmlLoader() {
		return new CifFractXmlLoader(MandelbrotProvider.class);
	}

	@Override
	public String getName() {
		return "Mandelbrot Set";
	}

	@Override
	public void initContextMenu(PopupMenu contextMenu) {
		super.initContextMenu(contextMenu);
		contextMenu.addSeparator();
		MenuItem gotoJulia = new MenuItem("Switch to according Julia Set");
		gotoJulia.addActionListener(listener);
		contextMenu.add(gotoJulia);
	}

	@Override
	public void onProviderChange(Object... params) {
		// Don't do anything
	}

	@Override
	public void handleCommandLineOption(String option) {
		// TODO implement
	}
}
