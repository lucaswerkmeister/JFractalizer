package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.geom.Rectangle2D;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlLoader;

public class MandelbrotSet extends CifFractal {
	public static final int					LOG_CLASS_PREFIX		= DefaultPlugin.LOG_PLUGIN_PREFIX
																			+ (((0 << 5) + (9 << 0)) << 8);
	public static final int					LOG_INIT_CONTEXT_MENU	= LOG_CLASS_PREFIX + 0;
	private final MandelbrotMenuListener	listener				= new MandelbrotMenuListener(this);
	private CifMenuListener					menuListener			= null;

	public MandelbrotSet() {
		super(MandelbrotImageMaker_NoHoles.class);
	}

	@Override
	public CifMenuListener getMenuListener() {
		if (menuListener == null)
			menuListener = new CifMenuListener(this, canvas);
		return menuListener;
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
		log(LOG_INIT_CONTEXT_MENU, contextMenu);
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
