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

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;

public class CifMenuListener implements ActionListener {
	public static final int			LOG_CLASS_PREFIX				= DefaultPlugin.LOG_PLUGIN_PREFIX
																			+ (((0 << 5) + (4 << 0)) << 8);
	public static final int			LOG_EDIT_BOUNDARIES				= LOG_CLASS_PREFIX + 0;
	public static final int			LOG_EDIT_ADDITIONAL_PARAMETERS	= LOG_CLASS_PREFIX + 1;
	public static final int			LOG_RECALCULATE					= LOG_CLASS_PREFIX + 2;
	public static final int			LOG_UNDO						= LOG_CLASS_PREFIX + 3;
	public static final int			LOG_REDO						= LOG_CLASS_PREFIX + 4;

	private final CifFractal		fractal;
	private final CifCanvas<?>		canvas;
	private boolean					okClicked						= false;
	Dialog							editBoundariesDialog			= null, additionalParamsDialog = null;

	private final KeyListener		okCancelListener				= new KeyAdapter() {
																		@Override
																		public void keyPressed(KeyEvent e) {
																			switch (e.getKeyCode()) {
																				case KeyEvent.VK_ESCAPE:
																					actionPerformed(new ActionEvent(
																							this, -1, "Cancel"));
																					break;
																				case KeyEvent.VK_ENTER:
																					actionPerformed(new ActionEvent(
																							this, -1, "OK"));
																					break;
																			}
																		}
																	};
	private final WindowListener	closeCancelListener				= new WindowAdapter() {
																		@Override
																		public void windowClosing(WindowEvent e) {
																			actionPerformed(new ActionEvent(this, -1,
																					"Cancel"));
																		}
																	};

	CifMenuListener(final CifFractal fractal, final CifCanvas<?> canvas) {
		this.fractal = fractal;
		this.canvas = canvas;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Edit boundaries...":
				editBoundariesDialog = new Dialog((Frame) fractal.getCanvas().getParent(), "Edit boundaries", true);
				editBoundariesDialog.setLayout(new BorderLayout());
				final Panel interval = new Panel(new BorderLayout());
				final TextField maxImag = new TextField(((Double) fractal.getMaxImag()).toString());
				maxImag.addKeyListener(okCancelListener);
				Panel p = new Panel(new GridBagLayout());
				p.add(maxImag);
				interval.add(p, BorderLayout.NORTH);
				final TextField minReal = new TextField(((Double) fractal.getMinReal()).toString());
				p = new Panel(new GridBagLayout());
				minReal.addKeyListener(okCancelListener);
				p.add(minReal);
				interval.add(p, BorderLayout.WEST);
				final TextField maxReal = new TextField(((Double) fractal.getMaxReal()).toString());
				maxReal.addKeyListener(okCancelListener);
				p = new Panel(new GridBagLayout());
				p.add(maxReal);
				interval.add(p, BorderLayout.EAST);
				final TextField minImag = new TextField(((Double) fractal.getMinImag()).toString());
				minImag.addKeyListener(okCancelListener);
				p = new Panel(new GridBagLayout());
				p.add(minImag);
				interval.add(p, BorderLayout.SOUTH);
				final Panel centerText = new Panel(new GridLayout(3, 1));
				centerText.add(new Label("maxImag", Label.CENTER));
				centerText.add(new Label("minReal + maxReal", Label.CENTER));
				centerText.add(new Label("minImag", Label.CENTER));
				interval.add(centerText, BorderLayout.CENTER);
				editBoundariesDialog.add(interval, BorderLayout.NORTH);
				final Panel resolution = new Panel(new FlowLayout());
				final TextField width = new TextField(((Integer) canvas.getWidth()).toString());
				width.addKeyListener(okCancelListener);
				resolution.add(width);
				resolution.add(new Label("x"));
				final TextField height = new TextField(((Integer) canvas.getHeight()).toString());
				height.addKeyListener(okCancelListener);
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
				editBoundariesDialog.addKeyListener(okCancelListener);
				editBoundariesDialog.addWindowListener(closeCancelListener);
				editBoundariesDialog.pack();
				editBoundariesDialog.setVisible(true);
				if (okClicked) {
					CifParams params = new CifParams(Double.parseDouble(minReal.getText()), Double.parseDouble(maxReal
							.getText()), Double.parseDouble(minImag.getText()), Double.parseDouble(maxImag.getText()),
							fractal.getMaxPasses(), fractal.getSuperSamplingFactor());
					Dimension size = new Dimension(Integer.parseInt(width.getText()),
							Integer.parseInt(height.getText()));
					fractal.setParams(params);
					fractal.setImageSize(size);
					log(LOG_EDIT_BOUNDARIES, params, size);
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
				additionalParamsDialog = new Dialog((Frame) fractal.getCanvas().getParent(),
						"Edit additional parameters", true);
				additionalParamsDialog.setLayout(new GridLayout(3, 2));
				additionalParamsDialog.add(new Label("SuperSampling Factor", Label.RIGHT));
				final JSpinner ssf = new JSpinner(new SpinnerNumberModel(fractal.getSuperSamplingFactor(), 1,
						Byte.MAX_VALUE, 1));
				ssf.addKeyListener(okCancelListener);
				additionalParamsDialog.add(ssf);
				additionalParamsDialog.add(new Label("Calculation depth", Label.RIGHT));
				final JSpinner maxPasses = new JSpinner(new SpinnerNumberModel(fractal.getMaxPasses(), 1,
						Integer.MAX_VALUE, 1));
				maxPasses.addKeyListener(okCancelListener);
				additionalParamsDialog.add(maxPasses);
				ok = new Button("OK");
				ok.addActionListener(this);
				ok.addKeyListener(okCancelListener);
				additionalParamsDialog.add(ok);
				cancel = new Button("Cancel");
				cancel.addActionListener(this);
				cancel.addKeyListener(okCancelListener);
				additionalParamsDialog.add(cancel);
				additionalParamsDialog.pack();
				ssf.requestFocusInWindow();
				additionalParamsDialog.addWindowListener(closeCancelListener);
				additionalParamsDialog.setVisible(true);
				if (okClicked) {
					byte superSamplingFactor = (byte) (int) /* (Integer) */ssf.getValue();
					int mp = (int) maxPasses.getValue();
					log(LOG_EDIT_ADDITIONAL_PARAMETERS, superSamplingFactor, mp);
					fractal.setSuperSamplingFactor(superSamplingFactor);
					fractal.setMaxPasses(mp);
					fractal.stopCalculation();
					fractal.startCalculation();
				}
				break;
			case "Recalculate":
				log(LOG_RECALCULATE);
				fractal.stopCalculation();
				fractal.startCalculation();
				break;
			case "Undo":
				if (fractal.history.canUndo()) {
					log(LOG_UNDO);
					fractal.stopCalculation();
					fractal.setParams(fractal.history.undo(), false);
					fractal.startCalculation();
				}
				fractal.undoMenuItem.setEnabled(fractal.history.canUndo());
				fractal.redoMenuItem.setEnabled(fractal.history.canRedo());
				break;
			case "Redo":
				if (fractal.history.canRedo()) {
					log(LOG_REDO);
					fractal.stopCalculation();
					fractal.setParams(fractal.history.redo(), false);
					fractal.startCalculation();
				}
				fractal.undoMenuItem.setEnabled(fractal.history.canUndo());
				fractal.redoMenuItem.setEnabled(fractal.history.canRedo());
				break;
			default:
				System.out.println("Action \"" + e.getActionCommand() + "\" not yet implemented. "
						+ "If you see this in a published version, punch the developer in the face. "
						+ "(No, seriously, don't do that. Just write me an e-mail.)");
		}
	}
}
