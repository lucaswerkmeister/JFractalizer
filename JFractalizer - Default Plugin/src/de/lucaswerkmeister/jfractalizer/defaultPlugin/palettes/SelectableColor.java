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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JColorChooser;

import de.lucaswerkmeister.jfractalizer.core.Core;

public class SelectableColor extends Component implements MouseListener {
	private static final long			serialVersionUID	= -464835931994412419L;
	private static final Dimension		size				= new Dimension(25, 25);
	private Color						color				= Color.black;
	private final List<ActionListener>	listeners			= new LinkedList<>();

	public SelectableColor(final Color c) {
		color = c;
		addMouseListener(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
	}

	@Override
	public void paint(final Graphics g) {
		g.setColor(Color.black);
		g.drawRect(0, 0, size.width, size.height);
		g.setColor(color);
		g.fillRect(1, 1, size.width - 1, size.height - 1);
	}

	public void setColor(final Color c) {
		color = c;
		repaint();
	}

	public Color getColor() {
		return color;
	}

	public void addActionListener(final ActionListener listener) {
		listeners.add(listener);
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		Container parent = getParent();
		while (!(parent instanceof Frame))
			parent = parent.getParent();
		final ColorDialog d = new ColorDialog((Frame) parent, color);
		d.setVisible(true);
		color = d.getColor();
		repaint();
		if (!listeners.isEmpty()) {
			final ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Color changed");
			for (final ActionListener l : listeners)
				l.actionPerformed(ae);
		}
	}

	@Override
	public void mousePressed(final MouseEvent e) {

	}

	@Override
	public void mouseReleased(final MouseEvent e) {

	}

	@Override
	public void mouseEntered(final MouseEvent e) {

	}

	@Override
	public void mouseExited(final MouseEvent e) {

	}
}

class ColorDialog extends Dialog implements ActionListener {
	private static final long	serialVersionUID	= 4347923773976010884L;
	private boolean				okClicked			= false;
	private final Color			startColor;
	private final JColorChooser	chooser;

	public ColorDialog(final Frame owner, final Color c) {
		super(owner, "Choose color", true);
		startColor = c;
		setLayout(new BorderLayout());
		chooser = Core.getGlobalColorChooser();
		chooser.setColor(c);
		add(chooser, BorderLayout.CENTER);
		final Panel south = new Panel();
		final Button ok = new Button("OK");
		ok.addActionListener(this);
		south.add(ok);
		final Button cancel = new Button("Cancel");
		cancel.addActionListener(this);
		south.add(cancel);
		add(south, BorderLayout.SOUTH);
		pack();
		invalidate();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		okClicked = e.getActionCommand().equals("OK");
		dispose();
	}

	public Color getColor() {
		if (okClicked)
			return chooser.getColor();
		else
			return startColor;
	}
}
