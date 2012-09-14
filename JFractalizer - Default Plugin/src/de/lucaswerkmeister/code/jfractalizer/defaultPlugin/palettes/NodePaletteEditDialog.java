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
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.palettes;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
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

public class NodePaletteEditDialog extends Dialog implements ActionListener, ChangeListener
{
	private static final long	serialVersionUID	= -4994844391708814135L;
	private final NodePalette	original;
	private Panel				nodesPanel;
	private List<ColorNode>		nodes;
	private Panel				otherPaletteStuff;
	private JSpinner			nodesCount;
	private SelectableColor		coreColor;
	private Panel				buttonsPanel;
	private boolean				okClicked;

	public NodePaletteEditDialog(Frame owner, NodePalette start)
	{
		super(owner, "Edit Color Palette", true);
		original = start;
		setLayout(new BorderLayout());
		ScrollPane nodesPanelParent = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		nodesPanel = new Panel(new FlowLayout());
		nodesPanelParent.add(nodesPanel);
		nodes = new ArrayList<>(start.nodes.size());
		ColorNode lastNode = null;
		for (ColorNode n : start.nodes)
		{
			ColorNode newNode = n.copy();
			nodes.add(newNode);
			if (lastNode != null)
				lastNode.link(newNode);
			nodesPanel.add(newNode);
			lastNode = newNode;
		}
		if (nodes.size() > 1)
			lastNode.link(nodes.get(0));
		coreColor = new SelectableColor(start.coreColor);
		nodesPanel.add(coreColor);
		otherPaletteStuff = new Panel();
		nodesCount = new JSpinner(new SpinnerNumberModel(start.nodes.size(), 1, null, 1));
		nodesCount.addChangeListener(this);
		otherPaletteStuff.add(new Label("Number of color nodes"));
		otherPaletteStuff.add(nodesCount);
		otherPaletteStuff.add(new Label("Core color"));
		otherPaletteStuff.add(coreColor);
		Panel centerPanel = new Panel(new GridLayout(2, 1));
		centerPanel.add(otherPaletteStuff);
		centerPanel.add(nodesPanelParent);
		add(centerPanel, BorderLayout.CENTER);
		buttonsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT));
		Button ok = new Button("OK");
		ok.addActionListener(this);
		buttonsPanel.add(ok);
		Button cancel = new Button("Cancel");
		cancel.addActionListener(this);
		buttonsPanel.add(cancel);
		add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		switch (e.getActionCommand())
		{
			case "OK":
				okClicked = true;
				break;
			case "Cancel":
				okClicked = false;
				break;
		}
		for (ColorNode n : nodes)
			n.prepare();
		setVisible(false);
	}

	public NodePalette getPalette()
	{
		if (okClicked)
			return new NodePalette(nodes, coreColor.getColor());
		else
			return original;
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		int newSize = (int) nodesCount.getValue();
		while (nodes.size() > newSize)
			nodes.remove(nodes.size() - 1);
		while (nodes.size() < newSize)
		{
			ColorNode newNode = new ColorNode(nodes.get(nodes.size() - 1).getEndColor(), nodes.get(0).getStartColor(), nodes.get(nodes.size() - 1)
					.getLength());
			nodes.get(nodes.size() - 1).unlink();
			nodes.get(nodes.size() - 1).link(newNode);
			nodes.add(newNode);
		}
		if (nodes.size() > 1)
			nodes.get(nodes.size() - 1).link(nodes.get(0));
		nodesPanel.removeAll();
		for (ColorNode n : nodes)
			nodesPanel.add(n);
		validate();
	}
}
