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
package de.lucaswerkmeister.code.jfractalizer;

import java.awt.Canvas;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ZoomMenuListener implements ActionListener
{
	public static final String	ZOOM_IN			= "Zoom in";
	public static final String	ZOOM_OUT		= "Zoom out";
	public static final String	USE_COORDINATES	= "On mouse location";
	public static final String	USE_CENTER		= "On center";
	public static final String	CENTER_NO_ZOOM	= "Center on mouse location";

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String actionCommand = e.getActionCommand();
		MainFrame i = MainFrame.getInstance();
		FractalProvider p = i.getCurrentProvider();
		Canvas c = p.getCanvas();
		p.stopCalculation();
		if (actionCommand.equals(CENTER_NO_ZOOM))
			p.zoom(i.zoomMenuX, i.zoomMenuY, 1.0);
		else
		{
			short factorPercent = Short.parseShort(actionCommand.substring(0, actionCommand.length() - 1));
			boolean useCoordinates = i.zoomMenuX >= 0 && i.zoomMenuY >= 0
					&& ((MenuItem) ((MenuItem) e.getSource()).getParent()).getLabel().equals(USE_COORDINATES);
			boolean zoomIn = ((MenuItem) ((MenuItem) ((MenuItem) e.getSource()).getParent()).getParent()).getLabel().equals(ZOOM_IN);
			double factor = zoomIn ? 1 - factorPercent / 200.0 : 1 + factorPercent / 100.0;
			int x = useCoordinates ? i.zoomMenuX : c.getWidth() / 2;
			int y = useCoordinates ? i.zoomMenuY : c.getHeight() / 2;
			p.zoom(x, y, factor);
		}
		p.startCalculation();
		i.zoomMenuX = i.zoomMenuY = -1;
	}
}