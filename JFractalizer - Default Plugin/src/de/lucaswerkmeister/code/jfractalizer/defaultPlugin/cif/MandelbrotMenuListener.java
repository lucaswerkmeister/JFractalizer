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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.cif;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.lucaswerkmeister.code.jfractalizer.Core;

public class MandelbrotMenuListener implements ActionListener {
    private final MandelbrotProvider provider;

    public MandelbrotMenuListener(MandelbrotProvider mandelbrotProvider) {
	this.provider = mandelbrotProvider;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	switch (e.getActionCommand()) {
	case "Switch to according Julia Set": {
	    try {
		Core.changeProvider(
			JuliaProvider.class,
			provider.canvas.getMinReal()
				+ (provider.canvas.getMaxReal() - provider.canvas
					.getMinReal())
				* provider.canvas.getMousePosition().x
				/ provider.canvas.getWidth(),
			provider.canvas.getMinImag()
				+ (provider.canvas.getMaxImag() - provider.canvas
					.getMinImag())
				* (provider.canvas.getHeight() - provider.canvas
					.getMousePosition().y)
				/ provider.canvas.getHeight());
	    } catch (HeadlessException | IllegalArgumentException
		    | ReflectiveOperationException e1) {
		e1.printStackTrace();
	    }
	}
	}
    }
}
