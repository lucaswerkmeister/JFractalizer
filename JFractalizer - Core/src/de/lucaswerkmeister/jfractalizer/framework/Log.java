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
	protected static final Map<Integer, Level>	levels	= new HashMap<>();
	private static final BlockingQueue<Entry>	entries	= new LinkedBlockingQueue<>();
	protected static boolean					running	= true;
	private static final Thread					handler	= new Thread() {
															public void run() {
																while (running) {
																	try {
																		Entry nextEntry = entries.poll(1,
																				TimeUnit.SECONDS);
																		if (nextEntry != null) {
																			nextEntry.prepare();
																			for (Log log : logs)
																				log.log(nextEntry);
																		}
																	}
																	catch (InterruptedException e) {
																		// This should never happen
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
	 * Registers that one id has a specified level and belongs to a specified plugin.
	 * <p>
	 * The general contract for the IDs is that the bits should be distributed as following:
	 * <tt>vvvvvvvv&nbsp;pppppppp&nbsp;cccccccc&nbsp;llllllll</tt> , where <tt>v</tt> stands for the plugin vendor,
	 * <tt>p</tt> for the plugin, <code>c</code> for the class in which the logging event occured, and <tt>l</tt> for
	 * the indivitual logging event.
	 * 
	 * @param id
	 *            The ID.
	 * @param level
	 *            The level: {@link Level#INFO Information}, {@link Level#WARNING Warning}, {@link Level#ERROR Error} or
	 *            {@link Level#CRASH Crash}.
	 * @param plugin
	 *            The plugin.
	 */
	public static final void registerID(int id, Level level, Plugin plugin) {
		ids.put(id, plugin);
		levels.put(id, level);
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
	 * Sends a logging event to all registered logs.
	 * <p>
	 * The level of the event (info, warning, error, crash) has been set when {@link #registerID(int, Level, Plugin)
	 * registering} the logging event.
	 * 
	 * @param id
	 *            The ID of the event.
	 * @param args
	 *            Arguments to the event.
	 */
	public static final void log(int id, Object... args) {
		entries.offer(new Entry(id, args));
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
		public Level			level;
		public Plugin			plugin;

		public Entry(int id, Object[] args) {
			this.time = System.currentTimeMillis();
			this.id = id;
			this.args = args;
		}

		public void prepare() {
			this.level = levels.get(id);
			this.plugin = ids.get(id);
		}
	}
}
