package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.MenuItem;
import java.awt.PopupMenu;

import de.lucaswerkmeister.jfractalizer.FractXmlLoader;

public class MandelbrotSet extends CifFractal {
	private final MandelbrotMenuListener	listener	= new MandelbrotMenuListener(this);

	public MandelbrotSet() {
		canvas = new CifCanvas<>(this, MandelbrotImageMaker_NoHoles.class);
		menuListener = new CifMenuListener(this, canvas);
	}

	@Override
	public FractXmlLoader getFractXmlLoader() {
		return new CifFractXmlLoader(MandelbrotSet.class);
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
	public void onFractalChange(Object... params) {
		// Don't do anything
	}
}
