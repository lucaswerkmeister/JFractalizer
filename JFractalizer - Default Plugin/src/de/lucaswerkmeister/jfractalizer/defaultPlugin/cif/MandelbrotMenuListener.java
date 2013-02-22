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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.lucaswerkmeister.jfractalizer.Core;

public class MandelbrotMenuListener implements ActionListener {
	private final CifFractal	fractal;

	public MandelbrotMenuListener(CifFractal mandelbrotFractal) {
		this.fractal = mandelbrotFractal;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Switch to according Julia Set": {
				try {
					Core.changeFractal(JuliaSet.class,
							fractal.canvas.getMinReal() + (fractal.canvas.getMaxReal() - fractal.canvas.getMinReal())
									* fractal.canvas.getMousePosition().x / fractal.canvas.getWidth(),
							fractal.canvas.getMinImag() + (fractal.canvas.getMaxImag() - fractal.canvas.getMinImag())
									* (fractal.canvas.getHeight() - fractal.canvas.getMousePosition().y)
									/ fractal.canvas.getHeight());
				}
				catch (HeadlessException | IllegalArgumentException | ReflectiveOperationException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
