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

import de.lucaswerkmeister.code.jfractalizer.FractXmlLoader;

public class JuliaProvider extends CifProvider {
    private double cReal, cImag;

    public JuliaProvider() {
	this(0.0, 0.0);
    }

    public JuliaProvider(final double cReal, final double cImag) {
	canvas = new CifCanvas<>(this, JuliaImageMaker_CalcAll.class);
	menuListener = new CifMenuListener(this, canvas);
	this.cReal = cReal;
	this.cImag = cImag;
    }

    @Override
    public FractXmlLoader getFractXmlLoader() {
	return new CifFractXmlLoader(JuliaProvider.class);
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
    public void onProviderChange(Object... params) {
	if (params.length == 2 && params[0] instanceof Double
		&& params[1] instanceof Double) {
	    cReal = (double) params[0];
	    cImag = (double) params[1];
	}
    }

    @Override
    public void handleCommandLineArgs(String arg0) {
	// TODO implement args
    }
}
