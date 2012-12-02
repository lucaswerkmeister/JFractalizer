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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class ClassChooserDialog<S extends SelectableService> extends Dialog
		implements ActionListener, WindowListener {
	private static final long serialVersionUID = -4629788833444189716L;
	private final ServiceLoader<S> serviceLoader;
	private final Map<String, S> services;
	private final List implList;
	private S selectedService;

	public ClassChooserDialog(final Frame owner, final String title,
			final Class<S> baseInterface) {
		super(owner, title, true);
		addWindowListener(this);
		final Label topText = new Label("Searching for implementations...");
		add(topText, BorderLayout.NORTH);
		implList = new List(10);
		add(implList, BorderLayout.CENTER);
		final Button ok = new Button("OK");
		ok.addActionListener(this);
		add(ok, BorderLayout.SOUTH);
		pack();
		services = new HashMap<>();
		serviceLoader = ServiceLoader.load(baseInterface);
		boolean hasService = false;
		for (final S service : serviceLoader) {
			final String name = service.getName();
			if (services.containsKey(name))
				System.out
						.println("WARNING: Several services with same name found!");
			services.put(name, service);
			implList.add(name);
			hasService = true;
		}
		if (!hasService) {
			System.out.println("No implementations for "
					+ baseInterface.getCanonicalName() + " found, exiting.");
			System.exit(1);
		}
		topText.setText("Choose an implementation.");
		selectedService = services.get(implList.getItem(0));
		pack();
		invalidate();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("OK")) {
			selectedService = services.get(implList.getSelectedItem());
			if (selectedService == null)
				selectedService = services.get(implList.getItem(0)); // in case
			// the user
			// didn't
			// select
			// any
			// service,
			// the
			// first in
			// the list
			// will be
			// used
			dispose();
		}
	}

	public S getSelectedService() {
		return selectedService;
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		dispose();
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}
}
