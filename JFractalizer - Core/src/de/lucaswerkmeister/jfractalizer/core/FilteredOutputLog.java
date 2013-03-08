package de.lucaswerkmeister.jfractalizer.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.lucaswerkmeister.jfractalizer.framework.Log;

public class FilteredOutputLog extends Log {
	private final Writer	output;
	private DateFormat		dateFormat	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private byte			tabWidth	= 8;
	private boolean			printTime	= true;
	private boolean			printVendor	= false;
	private boolean			printPlugin	= true;
	private byte			textStart	= 64;

	public FilteredOutputLog(OutputStream output) {
		this.output = new BufferedWriter(new OutputStreamWriter(output));
	}

	@Override
	protected void log(Entry entry) {
		try {
			String[] entryStringLines = entry.plugin.logToString(entry.id, entry.args).split("\\r?\\n");
			if (entryStringLines.length > 0) {
				StringBuilder initLine = new StringBuilder();
				if (printTime) {
					initLine.append("[");
					initLine.append(dateFormat.format(new Date(entry.time)));
					initLine.append("] ");
				}
				if (printVendor || printPlugin) {
					initLine.append("[");
					if (printVendor) {
						initLine.append(entry.plugin.getVendorName());
						if (printPlugin)
							initLine.append(" - ");
					}
					if (printPlugin)
						initLine.append(entry.plugin.getPluginName());
					initLine.append("]");
				}
				int tabCount = (int) Math.ceil(((double) (textStart - initLine.length())) / tabWidth);
				for (int i = 0; i < tabCount; i++)
					initLine.append('\t');
				output.write(initLine.toString());
				output.write(entryStringLines[0]);
				if (entryStringLines.length > 1) {
					StringBuilder tabsBuilder = new StringBuilder();
					int tabCount2 = textStart / tabWidth;
					for (int i = 0; i < tabCount2; i++)
						tabsBuilder.append('\t');
					String tabs = tabsBuilder.toString();
					for (int i = 1; i < entryStringLines.length; i++) {
						output.write(tabs);
						output.write(entryStringLines[i]);
					}
				}
			}
		}
		catch (IOException e) {
			// TODO figure out what to do here
		}
	}
}
