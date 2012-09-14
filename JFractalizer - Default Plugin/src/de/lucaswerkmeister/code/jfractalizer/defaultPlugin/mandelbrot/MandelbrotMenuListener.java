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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
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

public class MandelbrotMenuListener implements ActionListener
{
	private MandelbrotProvider	provider;
	private boolean				okClicked	= false;
	Dialog						editBoundariesDialog	= null, additionalParamsDialog = null;	// initialize variable so the compiler doesn't
																								// complain

	MandelbrotMenuListener(MandelbrotProvider provider)
	{
		this.provider = provider;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		switch (e.getActionCommand())
		{
			case "Edit boundaries...":
				editBoundariesDialog = new Dialog((Frame) provider.getCanvas().getParent(), true);
				editBoundariesDialog.setLayout(new BorderLayout());
				Panel interval = new Panel(new BorderLayout());
				TextField maxImag = new TextField(((Double) ((MandelbrotCanvas) provider.getCanvas()).getMaxImag()).toString());
				Panel p = new Panel(new GridBagLayout());
				p.add(maxImag);
				interval.add(p, BorderLayout.NORTH);
				TextField minImag = new TextField(((Double) ((MandelbrotCanvas) provider.getCanvas()).getMinImag()).toString());
				p = new Panel(new GridBagLayout());
				p.add(minImag);
				interval.add(p, BorderLayout.SOUTH);
				TextField maxReal = new TextField(((Double) ((MandelbrotCanvas) provider.getCanvas()).getMaxReal()).toString());
				p = new Panel(new GridBagLayout());
				p.add(maxReal);
				interval.add(p, BorderLayout.EAST);
				TextField minReal = new TextField(((Double) ((MandelbrotCanvas) provider.getCanvas()).getMinReal()).toString());
				p = new Panel(new GridBagLayout());
				p.add(minReal);
				interval.add(p, BorderLayout.WEST);
				Panel centerText = new Panel(new GridLayout(3, 1));
				centerText.add(new Label("maxImag", Label.CENTER));
				centerText.add(new Label("minReal + maxReal", Label.CENTER));
				centerText.add(new Label("minImag", Label.CENTER));
				interval.add(centerText, BorderLayout.CENTER);
				editBoundariesDialog.add(interval, BorderLayout.NORTH);
				Panel resolution = new Panel(new FlowLayout());
				TextField width = new TextField(((Integer) ((MandelbrotCanvas) provider.getCanvas()).getWidth()).toString());
				resolution.add(width);
				resolution.add(new Label("x"));
				TextField height = new TextField(((Integer) ((MandelbrotCanvas) provider.getCanvas()).getHeight()).toString());
				resolution.add(height);
				resolution.add(new Label("pixels"));
				editBoundariesDialog.add(resolution, BorderLayout.CENTER);
				Panel buttons = new Panel(new FlowLayout());
				Button ok = new Button("OK");
				ok.addActionListener(this);
				buttons.add(ok);
				Button cancel = new Button("Cancel");
				cancel.addActionListener(this);
				buttons.add(cancel);
				editBoundariesDialog.add(buttons, BorderLayout.SOUTH);
				editBoundariesDialog.pack();
				editBoundariesDialog.setVisible(true);
				if (okClicked)
				{
					((MandelbrotCanvas) provider.getCanvas()).setMinImag(Double.parseDouble(minImag.getText()));
					((MandelbrotCanvas) provider.getCanvas()).setMaxImag(Double.parseDouble(maxImag.getText()));
					((MandelbrotCanvas) provider.getCanvas()).setMinReal(Double.parseDouble(minReal.getText()));
					((MandelbrotCanvas) provider.getCanvas()).setMaxReal(Double.parseDouble(maxReal.getText()));
					((MandelbrotCanvas) provider.getCanvas()).setSize(Integer.parseInt(width.getText()), Integer.parseInt(height.getText()));
					((Frame) provider.getCanvas().getParent()).pack();
					provider.cancelCalculation();
					provider.startCalculation();
				}
				editBoundariesDialog = null;
				break;
			case "OK":
				if (editBoundariesDialog != null)
				{
					okClicked = true;
					editBoundariesDialog.dispose();
				}
				else if (additionalParamsDialog != null)
				{
					okClicked = true;
					additionalParamsDialog.dispose();
				}
				break;
			case "Cancel":
				if (editBoundariesDialog != null)
				{
					okClicked = false;
					editBoundariesDialog.dispose();
				}
				else if (additionalParamsDialog != null)
				{
					okClicked = false;
					additionalParamsDialog.dispose();
				}
				break;
			case "Edit additional parameters...":
				additionalParamsDialog = new Dialog((Frame) provider.getCanvas().getParent(), true);
				additionalParamsDialog.setLayout(new GridLayout(3, 2));
				additionalParamsDialog.add(new Label("SuperSampling Factor", Label.RIGHT));
				JSpinner ssf = new JSpinner(new SpinnerNumberModel(((MandelbrotCanvas) provider.getCanvas()).getSuperSamplingFactor(), 1,
						Byte.MAX_VALUE, 1));
				additionalParamsDialog.add(ssf);
				additionalParamsDialog.add(new Label("Calculation depth", Label.RIGHT));
				JSpinner maxPasses = new JSpinner(new SpinnerNumberModel(((MandelbrotCanvas) provider.getCanvas()).getMaxPasses(), 1,
						Integer.MAX_VALUE, 1));
				additionalParamsDialog.add(maxPasses);
				ok = new Button("OK");
				ok.addActionListener(this);
				additionalParamsDialog.add(ok);
				cancel = new Button("Cancel");
				cancel.addActionListener(this);
				additionalParamsDialog.add(cancel);
				additionalParamsDialog.pack();
				additionalParamsDialog.setVisible(true);
				if (okClicked)
				{
					((MandelbrotCanvas) provider.getCanvas()).setSuperSamplingFactor((byte) (int) ssf.getValue()); // the additional (int) cast is
																													// necessary because casting from
																													// Integer to byte raises a
																													// ClassCastException
					((MandelbrotCanvas) provider.getCanvas()).setMaxPasses((int) maxPasses.getValue());
					provider.cancelCalculation();
					provider.startCalculation();
				}
				break;
			case "Recalculate":
				provider.cancelCalculation();
				provider.startCalculation();
				break;
		}
	}
}
