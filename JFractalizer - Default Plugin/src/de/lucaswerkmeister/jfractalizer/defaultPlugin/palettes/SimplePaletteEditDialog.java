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

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.EditDialogPalette.PaletteEditDialog;

public class SimplePaletteEditDialog extends PaletteEditDialog implements ActionListener {
	private static final long	serialVersionUID	= -4693221922749333802L;
	private final SelectableColor	coreColor, startColor, endColor;
	private final JSpinner			colorSteps;
	private boolean					userCanceled	= false;

	public SimplePaletteEditDialog(final Frame owner, final SimplePalette original) {
		super(owner, original);
		coreColor = new SelectableColor(original.coreColor);
		startColor = new SelectableColor(original.startColor);
		endColor = new SelectableColor(original.endColor);
		colorSteps = new JSpinner(new SpinnerNumberModel(original.colorSteps, 2, null, 1));
		setLayout(new GridLayout(5, 2));
		add(new Label("Core Color", Label.RIGHT));
		add(coreColor);
		add(new Label("Start Color", Label.RIGHT));
		add(startColor);
		add(new Label("End Color", Label.RIGHT));
		add(endColor);
		add(new Label("Color steps", Label.RIGHT));
		add(colorSteps);
		final Button ok = new Button("OK");
		ok.addActionListener(this);
		add(ok);
		final Button cancel = new Button("Cancel");
		cancel.addActionListener(this);
		add(cancel);
		pack();
	}

	@Override
	public EditDialogPalette getPalette() {
		if (userCanceled)
			return original;
		return new SimplePalette(coreColor.getColor(), startColor.getColor(), endColor.getColor(),
				((Integer) colorSteps.getValue()));
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("Cancel"))
			userCanceled = true;
		setVisible(false);
	}
}
