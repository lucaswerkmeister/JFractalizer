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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CifMouseListener extends MouseAdapter {
	private final CifCanvas<?>		canvas;
	private Point					clickStart;
	private static final boolean	keepRatio	= true; // TODO make this

	// configurable

	public CifMouseListener(final CifCanvas<?> canvas) {
		this.canvas = canvas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			clickStart = e.getPoint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (e.getX() != clickStart.x || e.getY() != clickStart.y) {
				canvas.setSelectedArea(makeArea(e.getX(), e.getY()));
				canvas.goToSelectedArea();
			}
			canvas.setSelectedArea(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseAdapter#mouseDragged(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseDragged(final MouseEvent e) {
		// if (e.getButton() == MouseEvent.BUTTON1) //for some odd reason
		// e.getButton() returns 0 and not 1
		canvas.setSelectedArea(makeArea(e.getX(), e.getY()));
	}

	private Rectangle makeArea(final int mouseX, final int mouseY) {
		final int dX = mouseX - clickStart.x;
		final int dY = mouseY - clickStart.y;
		if (keepRatio) {
			final int cWidth = canvas.getWidth();
			final int cHeight = canvas.getHeight();
			final double xRatio = (double) dX / cWidth;
			final double yRatio = (double) dY / cHeight;
			if (xRatio > yRatio)
				return positiveRectangle(new Rectangle(clickStart.x,
						clickStart.y, (int) (cWidth * yRatio), dY));
			else
				return positiveRectangle(new Rectangle(clickStart.x,
						clickStart.y, dX, (int) (cHeight * xRatio)));
		}
		else
			return new Rectangle(clickStart.x, clickStart.y, dX, dY);
	}

	private Rectangle positiveRectangle(final Rectangle r) {
		if (r.width > 0) {
			if (r.height > 0)
				return r;
			else
				return new Rectangle(r.x, r.y + r.height, r.width, -r.height);
		}
		else if (r.height > 0)
			return new Rectangle(r.x + r.width, r.y, -r.width, r.height);
		else
			return new Rectangle(r.x + r.width, r.y + r.height, -r.width,
					-r.height);
	}
}
