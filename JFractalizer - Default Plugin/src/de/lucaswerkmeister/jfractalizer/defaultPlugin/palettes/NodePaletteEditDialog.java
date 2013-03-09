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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.EditDialogPalette.PaletteEditDialog;

public class NodePaletteEditDialog extends PaletteEditDialog implements ActionListener, ChangeListener {
	private static final long		serialVersionUID	= -4994844391708814135L;
	private final Panel				nodesPanel;
	private final List<ColorNode>	nodes;
	private final Panel				otherPaletteStuff;
	private final JSpinner			nodesCount;
	private final SelectableColor	coreColor;
	private final Panel				buttonsPanel;
	private boolean					okClicked;

	public NodePaletteEditDialog(final Frame owner, final NodePalette original) {
		super(owner, original);
		setLayout(new BorderLayout());
		final ScrollPane nodesPanelParent = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		nodesPanel = new Panel(new FlowLayout());
		nodesPanelParent.add(nodesPanel);
		nodes = new ArrayList<>(original.nodes.size());
		ColorNode lastNode = null;
		for (final ColorNode n : original.nodes) {
			final ColorNode newNode = n.copy();
			nodes.add(newNode);
			if (lastNode != null)
				lastNode.link(newNode);
			nodesPanel.add(newNode);
			lastNode = newNode;
		}
		if (nodes.size() > 1)
			lastNode.link(nodes.get(0));
		coreColor = new SelectableColor(original.coreColor);
		nodesPanel.add(coreColor);
		otherPaletteStuff = new Panel();
		nodesCount = new JSpinner(new SpinnerNumberModel(original.nodes.size(), 1, null, 1));
		nodesCount.addChangeListener(this);
		otherPaletteStuff.add(new Label("Number of color nodes"));
		otherPaletteStuff.add(nodesCount);
		otherPaletteStuff.add(new Label("Core color"));
		otherPaletteStuff.add(coreColor);
		final Panel centerPanel = new Panel(new GridLayout(2, 1));
		centerPanel.add(otherPaletteStuff);
		centerPanel.add(nodesPanelParent);
		add(centerPanel, BorderLayout.CENTER);
		buttonsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
		final Button ok = new Button("OK");
		ok.addActionListener(this);
		buttonsPanel.add(ok);
		final Button cancel = new Button("Cancel");
		cancel.addActionListener(this);
		buttonsPanel.add(cancel);
		add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		switch (e.getActionCommand()) {
			case "OK":
				okClicked = true;
				break;
			case "Cancel":
				okClicked = false;
				break;
		}
		for (final ColorNode n : nodes)
			n.prepare();
		setVisible(false);
	}

	public EditDialogPalette getPalette() {
		if (okClicked)
			return new NodePalette(nodes, coreColor.getColor());
		else
			return original;
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		final int newSize = (int) nodesCount.getValue();
		while (nodes.size() > newSize)
			nodes.remove(nodes.size() - 1);
		while (nodes.size() < newSize) {
			final ColorNode newNode = new ColorNode(nodes.get(nodes.size() - 1).getEndColor(), nodes.get(0)
					.getStartColor(), nodes.get(nodes.size() - 1).getLength());
			nodes.get(nodes.size() - 1).unlink();
			nodes.get(nodes.size() - 1).link(newNode);
			nodes.add(newNode);
		}
		if (nodes.size() > 1)
			nodes.get(nodes.size() - 1).link(nodes.get(0));
		nodesPanel.removeAll();
		for (final ColorNode n : nodes)
			nodesPanel.add(n);
		validate();
	}
}
