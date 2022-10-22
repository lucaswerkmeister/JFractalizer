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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.FractXmlLoader;

public class JuliaSet extends CifFractal {
	public static final int	LOG_CLASS_PREFIX	= DefaultPlugin.LOG_PLUGIN_PREFIX + (((0 << 5) + (0xD << 0)) << 8);
	public static final int	LOG_SWITCHED		= LOG_CLASS_PREFIX + 0;

	private double			cReal, cImag;
	private CifMenuListener	menuListener;

	public JuliaSet() {
		this(-0.6299693606626819, 0.6864195472186531);
	}

	public JuliaSet(final double cReal, final double cImag) {
		super(JuliaImageMaker_CalcAll.class);
		this.cReal = cReal;
		this.cImag = cImag;
	}

	@Override
	public void initMenu(Menu fractalMenu) {
		super.initMenu(fractalMenu);
		fractalMenu.addSeparator();
		MenuItem editJuliaParams = new MenuItem("Change start value", new MenuShortcut(KeyEvent.VK_V, false));
		editJuliaParams.addActionListener(new ActionListener() {
			private boolean	okClicked	= false;
			private boolean	closing		= false;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final Dialog dialog = new Dialog((Frame) getCanvas().getParent(), "Change start value", true);
				dialog.setLayout(new BorderLayout());
				Panel content = new Panel(new FlowLayout(FlowLayout.LEFT));
				TextField cReal = new TextField(Double.toString(getCReal()));
				content.add(cReal);
				content.add(new Label("+"));
				TextField cImag = new TextField(Double.toString(getCImag()));
				content.add(cImag);
				content.add(new Label("i"));
				dialog.add(content, BorderLayout.CENTER);
				Panel buttons = new Panel(new FlowLayout(FlowLayout.CENTER));
				Button ok = new Button("OK");
				final ActionListener okListener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okClicked = true;
						closing = true;
						dialog.setVisible(false);
					}
				};
				ok.addActionListener(okListener);
				buttons.add(ok);
				Button cancel = new Button("Cancel");
				final ActionListener cancelListener = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						closing = true;
						dialog.setVisible(false);
					}
				};
				cancel.addActionListener(cancelListener);
				buttons.add(cancel);
				dialog.add(buttons, BorderLayout.SOUTH);
				dialog.pack();
				KeyListener keyListener = new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.getKeyCode()) {
							case KeyEvent.VK_ENTER:
								okListener.actionPerformed(new ActionEvent(this, 0, "OK"));
								break;
							case KeyEvent.VK_ESCAPE:
								cancelListener.actionPerformed(new ActionEvent(this, 1, "Cancel"));
								break;
						}
					}
				};
				cReal.addKeyListener(keyListener);
				cImag.addKeyListener(keyListener);
				ok.addKeyListener(keyListener);
				cancel.addKeyListener(keyListener);
				dialog.addKeyListener(keyListener);
				dialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						if (!closing)
							cancelListener.actionPerformed(new ActionEvent(this, 2, "Cancel"));
					}
				});
				dialog.setVisible(true);
				if (okClicked) {
					double newCReal;
					double newCImag;
					try {
						newCReal = Double.parseDouble(cReal.getText());
						newCImag = Double.parseDouble(cImag.getText());
					}
					catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(getCanvas().getParent(), "Invalid number format", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (newCReal != getCReal() || newCImag != getCImag()) {
						stopCalculation();
						setCReal(newCReal);
						setCImag(newCImag);
						startCalculation();
					}
				}
			}
		});
		fractalMenu.add(editJuliaParams);
	}

	@Override
	protected CifMenuListener getMenuListener() {
		if (menuListener == null)
			menuListener = new CifMenuListener(this, canvas);
		return menuListener;
	}

	@Override
	public void saveFractXml(TransformerHandler handler) throws SAXException {
		super.saveFractXml(handler);

		final Attributes noAtts = new AttributesImpl();

		handler.startElement("", "", "cReal", noAtts);
		final char[] cReal = Double.toString(this.getCReal()).toCharArray();
		handler.characters(cReal, 0, cReal.length);
		handler.endElement("", "", "cReal");

		handler.startElement("", "", "cImag", noAtts);
		final char[] cImag = Double.toString(this.getCImag()).toCharArray();
		handler.characters(cImag, 0, cImag.length);
		handler.endElement("", "", "cImag");
	}

	@Override
	public FractXmlLoader getFractXmlLoader() {
		return new CifFractXmlLoader(JuliaSet.class);
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

	public void setCReal(double cReal) {
		this.cReal = cReal;
	}

	public void setCImag(double cImag) {
		this.cImag = cImag;
	}

	@Override
	public void onFractalChange(Object... params) {
		if (params.length == 2 && params[0] instanceof Double && params[1] instanceof Double) {
			log(LOG_SWITCHED, params);
			cReal = (double) params[0];
			cImag = (double) params[1];
		}
	}

	@Override
	protected java.awt.geom.Rectangle2D.Double getStartArea() {
		return new java.awt.geom.Rectangle2D.Double(-1.1, -1.1, 2.2, 2.2);
	}
}