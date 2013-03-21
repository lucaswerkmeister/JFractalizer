package de.lucaswerkmeister.jfractalizer.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.lucaswerkmeister.jfractalizer.framework.CommandLineConfigurable;
import de.lucaswerkmeister.jfractalizer.framework.IllegalCommandLineException;
import de.lucaswerkmeister.jfractalizer.framework.Log;
import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;

public class FilteredOutputLog extends Log implements CommandLineConfigurable {
	private final Set<Integer>	loggedIDs;
	private final Set<Integer>	allIDs;

	private Writer				output;
	private DateFormat			dateFormat	= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private byte				tabWidth	= 8;
	private boolean				printTime	= true;
	private boolean				printID		= true;
	private boolean				printVendor	= false;
	private boolean				printPlugin	= true;
	private byte				textStart	= 48;

	public FilteredOutputLog() {
		allIDs = new HashSet<>(ids.keySet());
		loggedIDs = new HashSet<>(allIDs);
	}

	public void setOutput(OutputStream output) {
		this.output = new BufferedWriter(new OutputStreamWriter(output));
	}

	@Override
	public void handleCommandLineOption(String option, String optionName, String optionContent) {
		switch (optionName) {
			case "show":
				for (int id : parseIdRange(optionContent, allIDs))
					loggedIDs.add(id);
				break;
			case "hide":
				for (int id : parseIdRange(optionContent, loggedIDs))
					loggedIDs.remove(id);
				break;
			case "printTime":
				printTime = Boolean.parseBoolean(optionContent);
				break;
			case "printVendor":
				printVendor = Boolean.parseBoolean(optionContent);
				break;
			case "printPlugin":
				printPlugin = Boolean.parseBoolean(optionContent);
				break;
			default:
				throw new IllegalCommandLineException("Unknown option \"" + optionName + "\" for log!");
		}
	}

	private Iterable<Integer> parseIdRange(final String range, final Set<Integer> ids) {
		return new Iterable<Integer>() {
			@Override
			public Iterator<Integer> iterator() {
				if (range.equals("all"))
					return new HashSet<>(ids).iterator();
				boolean filtersLevel;
				Level level = Level.INFO;
				int min;
				int max;
				if (range.contains(":")) {
					String[] split = range.split(":");
					filtersLevel = true;
					level = Level.valueOf(split[0].toUpperCase());
					if (split[1].contains("-")) {
						String[] split2 = split[1].split("-");
						min = Integer.parseInt(split2[0]);
						max = Integer.parseInt(split2[1]);
					}
					else
						min = max = Integer.parseInt(split[1]);
				}
				else if (range.contains("-")) {
					filtersLevel = false;
					String[] split = range.split("-");
					min = Integer.parseInt(split[0]);
					max = Integer.parseInt(split[1]);
				}
				else
					try {
						min = max = Integer.parseInt(range);
						filtersLevel = false;
					}
					catch (NumberFormatException e) {
						min = Integer.MIN_VALUE;
						max = Integer.MAX_VALUE;
						level = Level.valueOf(range.toUpperCase());
						filtersLevel = true;
					}
				// <uglycode>
				final boolean finalFiltersLevel = filtersLevel;
				final Level finalLevel = level;
				final int finalMin = min;
				final int finalMax = max;
				// </uglycode>
				return new Iterator<Integer>() {
					final Iterator<Integer>	innerIterator	= ids.iterator();
					boolean					hasNext			= false;
					int						next			= 0;

					@Override
					public boolean hasNext() {
						if (hasNext)
							return true;
						while (innerIterator.hasNext()) {
							int id = innerIterator.next();
							if ((!finalFiltersLevel || finalLevel == levels.get(id)) && (id >= finalMin)
									&& (id <= finalMax)) {
								hasNext = true;
								next = id;
								return true;
							}
						}
						return false;
					}

					@Override
					public Integer next() {
						if (!hasNext)
							hasNext = hasNext(); // make next
						if (hasNext) {
							hasNext = false;
							return next;
						}
						throw new IllegalStateException();
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}

	@Override
	protected void log(Entry entry) {
		if (loggedIDs.contains(entry.id))
			try {
				String[] entryStringLines = entry.plugin.logToString(entry.id, entry.args).split("\\r?\\n");
				if (entryStringLines.length > 0) {
					StringBuilder initLine = new StringBuilder();
					if (printTime) {
						initLine.append("[");
						initLine.append(dateFormat.format(new Date(entry.time)));
						initLine.append("] ");
					}
					if (printID) {
						initLine.append("[");
						initLine.append(Integer.toString(entry.id, 16));
						initLine.append("h] ");
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
					output.write(System.getProperty("line.separator"));
					output.flush();
				}
			}
			catch (IOException e) {
				// TODO figure out what to do here
			}
	}

	@Override
	protected void close() {
		try {
			output.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
