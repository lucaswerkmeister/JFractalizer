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
package de.lucaswerkmeister.jfractalizer.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;
import de.lucaswerkmeister.jfractalizer.framework.Fractal;

public class MenuListener implements ActionListener {
	JFileChooser	fileChooser;

	public MenuListener() {
		initFileChooser();
	}

	private void initFileChooser() {
		fileChooser = new JFileChooser(); // TODO the default directory should
		// probably be read from a config file
	}

	@Override
	public void actionPerformed(final ActionEvent event) {
		switch (event.getActionCommand()) {
			case "Save Image":
				final FileNameExtensionFilter jpg = new FileNameExtensionFilter("JPEG image", "jpg", "jpeg");
				final FileNameExtensionFilter png = new FileNameExtensionFilter("PNG image", "png");
				final FileNameExtensionFilter gif = new FileNameExtensionFilter("GIF image", "gif");
				fileChooser.removeChoosableFileFilter(fileChooser.getFileFilter()); // remove
				// the
				// "all files"
				// filter
				fileChooser.addChoosableFileFilter(jpg);
				fileChooser.addChoosableFileFilter(png);
				fileChooser.addChoosableFileFilter(gif);
				int result = fileChooser.showSaveDialog(MainFrame.getInstance());
				if (result == JFileChooser.APPROVE_OPTION)
					try {
						String fileFormat;
						String fileString = fileChooser.getSelectedFile().getAbsolutePath();
						final String[] fileStringParts = fileString.split("[.]");
						switch (fileStringParts[fileStringParts.length - 1]) {
							case "jpeg":
							case "jpg":
								fileFormat = "jpg";
								break;
							case "png":
								fileFormat = "png";
								break;
							case "gif":
								fileFormat = "gif";
								break;
							default:
								final FileFilter filter = fileChooser.getFileFilter();
								if (filter == jpg)
									fileFormat = "jpg";
								else if (filter == png)
									fileFormat = "png";
								else if (filter == gif)
									fileFormat = "gif";
								else
									throw new IllegalArgumentException(
											"Unknown image type! Allowed image types: jpg, png, gif");
								fileString = fileString + "." + fileFormat;
						}
						ImageIO.write(Core.getImage(), fileFormat, new File(fileString));
					}
					catch (final IOException e) {
						System.out.println("An error occured while writing image file. Exception:");
						e.printStackTrace();
					}
				break;
			case "Save Setup":
				FileNameExtensionFilter fractXml = new FileNameExtensionFilter("Fractal XML", "fractXml", "xml");
				for (final FileFilter f : fileChooser.getChoosableFileFilters())
					fileChooser.removeChoosableFileFilter(f);
				fileChooser.addChoosableFileFilter(fractXml);
				result = fileChooser.showSaveDialog(MainFrame.getInstance());
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					if (!(selectedFile.getName().endsWith("xml") || selectedFile.getName().endsWith("Xml")))
						selectedFile = new File(selectedFile.getAbsolutePath() + ".fractXml");
					try (final OutputStream out = new BufferedOutputStream(new FileOutputStream(selectedFile))) {
						final StreamResult streamResult = new StreamResult(out);
						final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
						TransformerHandler hd;
						hd = tf.newTransformerHandler();
						final Transformer serializer = hd.getTransformer();
						serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
						serializer.setOutputProperty(OutputKeys.INDENT, "yes");
						hd.setResult(streamResult);
						hd.startDocument();
						AttributesImpl atts = new AttributesImpl();
						atts.addAttribute("", "", "version", "CDATA", "2");
						hd.startElement("", "", "fractXML", atts);
						atts.clear();
						atts.addAttribute("", "", "canonicalName", "CDATA", Core.getCurrentFractal().getClass()
								.getCanonicalName());
						hd.startElement("", "", "fractal", atts);
						Core.getCurrentFractal().saveFractXml(hd);
						hd.endElement("", "", "fractal");
						atts.clear();
						atts.addAttribute("", "", "canonicalName", "CDATA", Core.getCurrentColorPalette().getClass()
								.getCanonicalName());
						hd.startElement("", "", "palette", atts);
						Core.getCurrentColorPalette().saveFractXml(hd);
						hd.endElement("", "", "palette");
						hd.endElement("", "", "fractXML");
						hd.endDocument();
					}
					catch (TransformerConfigurationException | SAXException | IOException e) {
						e.printStackTrace();
					}
				}
				break;
			case "Load Setup":
				fractXml = new FileNameExtensionFilter("Fractal XML", "fractXml", "xml");
				for (final FileFilter f : fileChooser.getChoosableFileFilters())
					fileChooser.removeChoosableFileFilter(f);
				fileChooser.addChoosableFileFilter(fractXml);
				result = fileChooser.showOpenDialog(MainFrame.getInstance());
				if (result == JFileChooser.APPROVE_OPTION)
					try {
						Core.loadFile(fileChooser.getSelectedFile());
					}
					catch (SAXException | IOException | ParserConfigurationException e) {
						e.printStackTrace();
					}
				break;
			case "Choose Fractal...":
				final ClassChooserDialog<Fractal> fractalChooserDialog = new ClassChooserDialog<>(
						MainFrame.getInstance(), "Choose Fractal", Fractal.class);
				fractalChooserDialog.setVisible(true);
				Core.setCurrentFractal(fractalChooserDialog.getSelectedService());
				break;
			case "Choose Color Palette...":
				final ClassChooserDialog<ColorPalette> colorPaletteDialog = new ClassChooserDialog<>(
						MainFrame.getInstance(), "Choose Color Palette", ColorPalette.class);
				colorPaletteDialog.setVisible(true);
				Core.setCurrentColorPalette(colorPaletteDialog.getSelectedService());
				break;
			case "Exit":
				System.exit(0);
				break;
			default:
				System.out
						.println("Action \""
								+ event.getActionCommand()
								+ "\" not yet implemented. If you see this in a published version, punch the developer in the face. (No, seriously, don't do that. Just write me an e-mail.)");
		}
	}
}
