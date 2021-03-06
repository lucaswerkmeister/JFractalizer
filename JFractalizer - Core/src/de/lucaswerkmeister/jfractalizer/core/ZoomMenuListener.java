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
package de.lucaswerkmeister.jfractalizer.core;

import java.awt.Canvas;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.lucaswerkmeister.jfractalizer.framework.Fractal;
import de.lucaswerkmeister.jfractalizer.framework.ZoomableFractal;

public class ZoomMenuListener implements ActionListener {
	public static final String	ZOOM_IN			= "Zoom in";
	public static final String	ZOOM_OUT		= "Zoom out";
	public static final String	USE_COORDINATES	= "On mouse location";
	public static final String	USE_CENTER		= "On center";
	public static final String	CENTER_NO_ZOOM	= "Center on mouse location";
	public static final String	START_IMAGE		= "Show start image";

	@Override
	public void actionPerformed(final ActionEvent e) {
		final String actionCommand = e.getActionCommand();
		final MainFrame i = MainFrame.getInstance();
		final Fractal fract = Core.getCurrentFractal();
		if (!(fract instanceof ZoomableFractal))
			throw new UnsupportedOperationException("Can't zoom on current fractal!");
		final ZoomableFractal p = (ZoomableFractal) fract;
		final Canvas c = p.getCanvas();
		p.stopCalculation();
		if (actionCommand.equals(CENTER_NO_ZOOM))
			p.zoom(i.zoomMenuX, i.zoomMenuY, 1.0);
		else if (actionCommand.equals(START_IMAGE))
			p.zoomToStart(c.getWidth() / 2, c.getHeight() / 2, p.getZoomFactor());
		else {
			final short factorPercent = Short.parseShort(actionCommand.substring(0, actionCommand.length() - 1));
			final boolean useCoordinates = i.zoomMenuX >= 0 && i.zoomMenuY >= 0
					&& ((MenuItem) ((MenuItem) e.getSource()).getParent()).getLabel().equals(USE_COORDINATES);
			final boolean zoomIn = ((MenuItem) ((MenuItem) ((MenuItem) e.getSource()).getParent()).getParent())
					.getLabel().equals(ZOOM_IN);
			final double factor = zoomIn ? 1 - factorPercent / 200.0 : 1 + factorPercent / 100.0;
			final int x = useCoordinates ? i.zoomMenuX : c.getWidth() / 2;
			final int y = useCoordinates ? i.zoomMenuY : c.getHeight() / 2;
			p.zoom(x, y, factor);
		}
		p.startCalculation();
		i.zoomMenuX = i.zoomMenuY = -1;
	}
}
