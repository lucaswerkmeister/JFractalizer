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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.mandelbrot;


public class WaitForCalcThreads extends Thread
{
	MandelbrotImageMaker[]	threads;
	MandelbrotCanvas		canvas;

	public WaitForCalcThreads(final MandelbrotImageMaker[] threads, final MandelbrotCanvas canvas)
	{
		this.threads = threads;
		this.canvas = canvas;
	}

	@Override
	public void run()
	{
		for (final Thread t : threads)
			while (t.isAlive())
				try
				{
					Thread.sleep(25);
					canvas.repaint(25);
				}
				catch (final InterruptedException e)
				{
					// do nothing
				}
	}
}
