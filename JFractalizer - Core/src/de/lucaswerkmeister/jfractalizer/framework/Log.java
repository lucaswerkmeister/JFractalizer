package de.lucaswerkmeister.jfractalizer.framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;

public abstract class Log {
	private static final Set<Log>				logs	= new HashSet<>();
	protected static final Map<Integer, Plugin>	ids		= new HashMap<>();
	private static final BlockingQueue<Entry>	entries	= new LinkedBlockingQueue<>();
	protected static boolean					running	= true;
	private static final Thread					handler	= new Thread() {
															public void run() {
																while (running) {
																	try {
																		Entry nextEntry = entries.poll(1,
																				TimeUnit.SECONDS);
																		if (nextEntry != null)
																			for (Log log : logs)
																				log.log(nextEntry);
																	}
																	catch (InterruptedException e) {
																		continue;
																	}
																}
															}
														};
	static {
		handler.setPriority(Thread.MIN_PRIORITY);
		handler.start();
	}

	/**
	 * Registers that one id belongs to a specified plugin.
	 * <p>
	 * The general contract for the IDs is that the bits should be distributed as following:
	 * <tt>vvvvvvvv&nbsp;pppppppp&nbsp;cccccccc&nbsp;llllllll</tt> , where <tt>v</tt> stands for the plugin vendor,
	 * <tt>p</tt> for the plugin, <code>c</code> for the class in which the logging event occured, and <tt>l</tt> for
	 * the indivitual logging event.
	 * 
	 * @param id
	 *            The ID.
	 * @param plugin
	 *            The plugin.
	 */
	public static final void registerID(int id, Plugin plugin) {
		ids.put(id, plugin);
	}

	/**
	 * Adds a log.
	 * 
	 * @param log
	 *            The log.
	 */
	public static final void registerLog(Log log) {
		logs.add(log);
	}

	/**
	 * Send an information to all registered logs.
	 * 
	 * @param id
	 *            The ID of the information.
	 * @param args
	 *            Arguments to the information.
	 */
	public static final void info(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.INFO, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	/**
	 * Send a warning to all registered logs.
	 * 
	 * @param id
	 *            The ID of the warning.
	 * @param args
	 *            Arguments to the warning.
	 */
	public static final void warning(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.WARNING, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	/**
	 * Send an error to all registered logs.
	 * 
	 * @param id
	 *            The ID of the error.
	 * @param args
	 *            Arguments to the error.
	 */
	public static final void error(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.ERROR, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	/**
	 * Send a crash to all registered logs.
	 * 
	 * @param id
	 *            The ID of the crash.
	 * @param args
	 *            Arguments to the crash.
	 */
	static final void crash(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.CRASH, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	static final void shutdown() {
		running = false;
	}

	/**
	 * Logs an entry. This method is called asynchronously and may therefore block.
	 * 
	 * @param entry
	 *            The log entry.
	 */
	protected abstract void log(Entry entry);

	public static class Entry {
		public static enum Level {
			INFO, WARNING, ERROR, CRASH
		}

		public final long		time;
		public final int		id;
		public final Object[]	args;
		public final Level		level;
		public final Plugin		plugin;

		public Entry(int id, Level level, Object[] args) {
			this.time = System.currentTimeMillis();
			this.id = id;
			this.args = args;
			this.level = level;
			this.plugin = ids.get(id);
		}
	}
}
