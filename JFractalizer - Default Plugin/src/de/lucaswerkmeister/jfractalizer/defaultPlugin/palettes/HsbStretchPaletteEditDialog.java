package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.EditDialogPalette.PaletteEditDialog;

public class HsbStretchPaletteEditDialog extends PaletteEditDialog {
	private static final long	serialVersionUID	= 576336383691856587L;
	private JSpinner			hueBaseWidth;
	private JSlider				saturation;
	private JSlider				brightness;
	private JSlider				hueStart;
	private SelectableColor		coreColor;
	private boolean				okClicked			= false;

	private final KeyListener	okCancelListener	= new KeyAdapter() {
														@Override
														public void keyPressed(java.awt.event.KeyEvent e) {
															switch (e.getKeyCode()) {
																case KeyEvent.VK_ENTER:
																	okClicked = true;
																	setVisible(false);
																	break;
																case KeyEvent.VK_ESCAPE:
																	okClicked = false;
																	setVisible(false);
																	break;
															}
														};
													};

	public HsbStretchPaletteEditDialog(Frame owner, HsbStretchPalette palette) {
		super(owner, palette);
		setLayout(new GridLayout(5, 1));

		Panel hueBaseWidthPanel = new Panel(new FlowLayout());
		JLabel periodLabel = new JLabel("Base period");
		periodLabel.setToolTipText("Number of colors at the outermost level of the fractal (grows dynamically further in)");
		hueBaseWidthPanel.add(periodLabel);
		hueBaseWidth = new JSpinner(new SpinnerNumberModel(palette.getHueBaseWidth(), 1.0,
				Integer.MAX_VALUE, 0.01));
		hueBaseWidth.addKeyListener(okCancelListener);
		((JSpinner.DefaultEditor) hueBaseWidth.getEditor()).getTextField().addKeyListener(okCancelListener);
		hueBaseWidthPanel.add(hueBaseWidth);
		add(hueBaseWidthPanel);

		Panel saturationPanel = new Panel(new FlowLayout());
		saturationPanel.add(new Label("Saturation"));
		saturation = new JSlider(0, 100, Math.round(palette.getSaturation() * 100));
		Hashtable<Integer, JLabel> floatTable = new Hashtable<>();
		floatTable.put(0, new JLabel("0.0"));
		floatTable.put(50, new JLabel("0.5"));
		floatTable.put(100, new JLabel("1.0"));
		saturation.setLabelTable(floatTable);
		saturation.setPaintLabels(true);
		// saturation.setMajorTickSpacing(10);
		// saturation.setMinorTickSpacing(1);
		// saturation.setPaintTicks(true);
		saturation.addKeyListener(okCancelListener);
		saturationPanel.add(saturation);
		add(saturationPanel);

		Panel lightnessPanel = new Panel(new FlowLayout());
		lightnessPanel.add(new Label("Brightness"));
		brightness = new JSlider(0, 100, Math.round(palette.getBrightness() * 100));
		brightness.setLabelTable(floatTable);
		brightness.setPaintLabels(true);
		// brightness.setMajorTickSpacing(10);
		// brightness.setMinorTickSpacing(1);
		// brightness.setPaintTicks(true);
		brightness.addKeyListener(okCancelListener);
		lightnessPanel.add(brightness);
		add(lightnessPanel);

		Panel restPanel = new Panel(new FlowLayout());
		restPanel.add(new Label("Hue start"));
		hueStart = new JSlider(0, 100, Math.round(palette.getHueStart() * 100));
		hueStart.setLabelTable(floatTable);
		hueStart.setPaintLabels(true);
		hueStart.addKeyListener(okCancelListener);
		restPanel.add(hueStart);
		restPanel.add(new Label("Core color"));
		coreColor = new SelectableColor(palette.getCoreColor());
		restPanel.add(coreColor);
		add(restPanel);

		Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
		Button okButton = new Button("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				setVisible(false);
			}
		});
		buttonsPanel.add(okButton);
		Button cancelButton = new Button("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		buttonsPanel.add(cancelButton);
		add(buttonsPanel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		});

		pack();

		hueBaseWidth.requestFocusInWindow();
	}

	@Override
	public EditDialogPalette getPalette() {
		if (okClicked)
			return new HsbStretchPalette(hueStart.getValue() / 100f, (double) (Double) hueBaseWidth.getValue(),
					saturation.getValue() / 100f, brightness.getValue() / 100f, coreColor.getColor());
		return original;
	}
}
