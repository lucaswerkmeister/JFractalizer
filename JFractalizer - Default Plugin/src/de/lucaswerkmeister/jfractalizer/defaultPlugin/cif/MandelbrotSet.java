package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.geom.Rectangle2D;

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
		if (!contextMenu.getItem(contextMenu.getItemCount() - 1).getLabel().equals("-"))
			contextMenu.addSeparator();
		MenuItem gotoJulia = new MenuItem("Switch to according Julia Set");
		gotoJulia.addActionListener(listener);
		contextMenu.add(gotoJulia);
	}

	@Override
	public void onFractalChange(Object... params) {
		// Don't do anything
	}

	@Override
	protected Rectangle2D.Double getStartArea() {
		return new Rectangle2D.Double(-2.25, -1.25, 3.25, 2.5);
	}
}
