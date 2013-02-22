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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class CifMenuListener implements ActionListener {
	private final CifFractal	fractal;
	private final CifCanvas<?>	canvas;
	private boolean				okClicked	= false;
	Dialog						editBoundariesDialog	= null, additionalParamsDialog = null;	// initialize

	// variable so the compiler doesn't complain

	CifMenuListener(final CifFractal fractal, final CifCanvas<?> canvas) {
		this.fractal = fractal;
		this.canvas = canvas;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Edit boundaries...":
				editBoundariesDialog = new Dialog((Frame) fractal.getCanvas().getParent(), true);
				editBoundariesDialog.setLayout(new BorderLayout());
				final Panel interval = new Panel(new BorderLayout());
				final TextField maxImag = new TextField(((Double) canvas.getMaxImag()).toString());
				Panel p = new Panel(new GridBagLayout());
				p.add(maxImag);
				interval.add(p, BorderLayout.NORTH);
				final TextField minImag = new TextField(((Double) canvas.getMinImag()).toString());
				p = new Panel(new GridBagLayout());
				p.add(minImag);
				interval.add(p, BorderLayout.SOUTH);
				final TextField maxReal = new TextField(((Double) canvas.getMaxReal()).toString());
				p = new Panel(new GridBagLayout());
				p.add(maxReal);
				interval.add(p, BorderLayout.EAST);
				final TextField minReal = new TextField(((Double) canvas.getMinReal()).toString());
				p = new Panel(new GridBagLayout());
				p.add(minReal);
				interval.add(p, BorderLayout.WEST);
				final Panel centerText = new Panel(new GridLayout(3, 1));
				centerText.add(new Label("maxImag", Label.CENTER));
				centerText.add(new Label("minReal + maxReal", Label.CENTER));
				centerText.add(new Label("minImag", Label.CENTER));
				interval.add(centerText, BorderLayout.CENTER);
				editBoundariesDialog.add(interval, BorderLayout.NORTH);
				final Panel resolution = new Panel(new FlowLayout());
				final TextField width = new TextField(((Integer) canvas.getWidth()).toString());
				resolution.add(width);
				resolution.add(new Label("x"));
				final TextField height = new TextField(((Integer) canvas.getHeight()).toString());
				resolution.add(height);
				resolution.add(new Label("pixels"));
				editBoundariesDialog.add(resolution, BorderLayout.CENTER);
				final Panel buttons = new Panel(new FlowLayout());
				Button ok = new Button("OK");
				ok.addActionListener(this);
				buttons.add(ok);
				Button cancel = new Button("Cancel");
				cancel.addActionListener(this);
				buttons.add(cancel);
				editBoundariesDialog.add(buttons, BorderLayout.SOUTH);
				editBoundariesDialog.pack();
				editBoundariesDialog.setVisible(true);
				if (okClicked) {
					canvas.setMinImag(Double.parseDouble(minImag.getText()));
					canvas.setMaxImag(Double.parseDouble(maxImag.getText()));
					canvas.setMinReal(Double.parseDouble(minReal.getText()));
					canvas.setMaxReal(Double.parseDouble(maxReal.getText()));
					Dimension d = new Dimension(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
					canvas.setImageSize(d);
					canvas.setPreferredSize(d);
					canvas.setSize(d);
					((Frame) canvas.getParent()).pack();
					fractal.stopCalculation();
					fractal.startCalculation();
				}
				editBoundariesDialog = null;
				break;
			case "OK":
				if (editBoundariesDialog != null) {
					okClicked = true;
					editBoundariesDialog.dispose();
				}
				else if (additionalParamsDialog != null) {
					okClicked = true;
					additionalParamsDialog.dispose();
				}
				break;
			case "Cancel":
				if (editBoundariesDialog != null) {
					okClicked = false;
					editBoundariesDialog.dispose();
				}
				else if (additionalParamsDialog != null) {
					okClicked = false;
					additionalParamsDialog.dispose();
				}
				break;
			case "Edit additional parameters...":
				additionalParamsDialog = new Dialog((Frame) fractal.getCanvas().getParent(), true);
				additionalParamsDialog.setLayout(new GridLayout(3, 2));
				additionalParamsDialog.add(new Label("SuperSampling Factor", Label.RIGHT));
				final JSpinner ssf = new JSpinner(new SpinnerNumberModel(canvas.getSuperSamplingFactor(), 1,
						Byte.MAX_VALUE, 1));
				additionalParamsDialog.add(ssf);
				additionalParamsDialog.add(new Label("Calculation depth", Label.RIGHT));
				final JSpinner maxPasses = new JSpinner(new SpinnerNumberModel(canvas.getMaxPasses(), 1,
						Integer.MAX_VALUE,
						1));
				additionalParamsDialog.add(maxPasses);
				ok = new Button("OK");
				ok.addActionListener(this);
				additionalParamsDialog.add(ok);
				cancel = new Button("Cancel");
				cancel.addActionListener(this);
				additionalParamsDialog.add(cancel);
				additionalParamsDialog.pack();
				additionalParamsDialog.setVisible(true);
				if (okClicked) {
					canvas.setSuperSamplingFactor((byte) (int) ssf.getValue()); // the
					// additional (int) cast is necessary because casting from Integer to byte raises a
					// ClassCastException
					canvas.setMaxPasses((int) maxPasses.getValue());
					fractal.stopCalculation();
					fractal.startCalculation();
				}
				break;
			case "Recalculate":
				fractal.stopCalculation();
				fractal.startCalculation();
				break;
			case "Undo":
				if (canvas.history.canUndo()) {
					fractal.stopCalculation();
					canvas.setParams((CifParams) canvas.history.undo(), false);
					fractal.startCalculation();
				}
				fractal.undoMenuItem.setEnabled(canvas.history.canUndo());
				fractal.redoMenuItem.setEnabled(canvas.history.canRedo());
				break;
			case "Redo":
				if (canvas.history.canRedo()) {
					fractal.stopCalculation();
					canvas.setParams((CifParams) canvas.history.redo(), false);
					fractal.startCalculation();
				}
				fractal.undoMenuItem.setEnabled(canvas.history.canUndo());
				fractal.redoMenuItem.setEnabled(canvas.history.canRedo());
				break;
			default:
				System.out
						.println("Action \""
								+ e.getActionCommand()
								+ "\" not yet implemented. If you see this in a published version, punch the developer in the face. (No, seriously, don't do that. Just write me an e-mail.)");
		}
	}
}
