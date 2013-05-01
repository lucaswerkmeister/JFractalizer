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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.lucaswerkmeister.jfractalizer.core.Core;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;

public class MandelbrotMenuListener implements ActionListener {
	public static final int		LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((0 << 5) + (0xA << 0)) << 8);
	public static final int		LOG_SWITCH_JULIA	= LOG_CLASS_PREFIX + 0;

	private final CifFractal	fractal;

	public MandelbrotMenuListener(CifFractal mandelbrotFractal) {
		this.fractal = mandelbrotFractal;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Switch to according Julia Set": {
				log(LOG_SWITCH_JULIA);
				try {
					Core.changeFractal(
							JuliaSet.class,
							fractal.getMinReal() + (fractal.getMaxReal() - fractal.getMinReal())
									* fractal.canvas.getMousePosition().x / fractal.canvas.getWidth(),
							fractal.getMinImag() + (fractal.getMaxImag() - fractal.getMinImag())
									* (fractal.canvas.getHeight() - fractal.canvas.getMousePosition().y)
									/ fractal.canvas.getHeight());
				}
				catch (IllegalArgumentException | ReflectiveOperationException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
