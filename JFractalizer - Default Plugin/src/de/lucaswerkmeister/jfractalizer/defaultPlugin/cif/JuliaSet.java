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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlLoader;

public class JuliaSet extends CifFractal {
	public static final int	LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((0 << 5) + (0xD << 0)) << 8);
	public static final int	LOG_SWITCHED		= LOG_CLASS_PREFIX + 0;

	private double			cReal, cImag;
	private CifMenuListener	menuListener;

	public JuliaSet() {
		this(-0.6299693606626819, 0.6864195472186531);
	}

	public JuliaSet(final double cReal, final double cImag) {
		super(JuliaImageMaker_CalcAll.class);
		this.cReal = cReal;
		this.cImag = cImag;
	}

	@Override
	protected CifMenuListener getMenuListener() {
		if (menuListener == null)
			menuListener = new CifMenuListener(this, canvas);
		return menuListener;
	}

	@Override
	public FractXmlLoader getFractXmlLoader() {
		return new CifFractXmlLoader(JuliaSet.class);
	}

	@Override
	public String getName() {
		return "Julia Set";
	}

	public double getCReal() {
		return cReal;
	}

	public double getCImag() {
		return cImag;
	}

	@Override
	public void onFractalChange(Object... params) {
		if (params.length == 2 && params[0] instanceof Double && params[1] instanceof Double) {
			log(LOG_SWITCHED, params);
			cReal = (double) params[0];
			cImag = (double) params[1];
		}
	}

	@Override
	protected java.awt.geom.Rectangle2D.Double getStartArea() {
		return new java.awt.geom.Rectangle2D.Double(-1.1, -1.1, 2.2, 2.2);
	}
}