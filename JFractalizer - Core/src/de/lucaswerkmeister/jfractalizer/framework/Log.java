package de.lucaswerkmeister.jfractalizer.framework;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.lucaswerkmeister.jfractalizer.core.TimeSpan;
import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;

public abstract class Log {
	private static final Set<Log>						logs	= new HashSet<>();
	protected static final Map<Integer, Plugin>			ids		= new HashMap<>();
	protected static final Map<Integer, Level>			levels	= new HashMap<>();
	private static final BlockingQueue<Entry>			entries	= new LinkedBlockingQueue<>();
	private static final Map<Thread, LinkedList<Long>>	timers	= new HashMap<>();
	protected static boolean							running	= true;
	private static final Thread							handler	= new Thread() {
																	@Override
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

	/**
	 * Adds one timer to the stack of timers associated with the current thread.
	 * <p>
	 * This will also be logged with the specified ID; the arguments to the logging event will be one {@link Integer}
	 * representing the current stack size (which can be used for e.&nbsp;g. indenting the textual representation),
	 * followed by the arguments given to this method.
	 * 
	 * @param id
	 *            The ID of the resulting logging event. (Not associated with the timer.)
	 * @param args
	 *            The arguments to the resulting logging event. (Not associated with the timer.)
	 */
	public static final void pushTimer(int id, Object... args) {
		Thread currentThread = Thread.currentThread();
		LinkedList<Long> stack;
		if (!timers.containsKey(currentThread)) {
			stack = new LinkedList<>();
			timers.put(currentThread, stack);
		}
		else
			stack = timers.get(currentThread);
		log(id, stack.size(), args);
		stack.push(System.nanoTime());
	}

	/**
	 * Stops and removes one timer from the stack of timers associated with the current thread.
	 * <p>
	 * This will also be logged with the specified ID; the the arguments to the logging event will be one
	 * {@link TimeSpan} representing the elapsed time since the timer was started, one {@link Integer} representing the
	 * stack size (including the removed timer) (which can be used for e.&nbsp;g. indenting the textual representation),
	 * followed by the arguments given to this method.
	 * 
	 * @param id
	 *            The ID of the resulting logging event. (Not associated with the timer.)
	 * @param args
	 *            The arguments to the resulting logging event. (Not associated with the timer.)
	 */
	public static final void popTimer(int id, Object... args) {
		LinkedList<Long> stack = timers.get(Thread.currentThread());
		log(id, new TimeSpan(System.nanoTime() - stack.pop()), stack.size() + 1, args);
	}

	public static final void shutdown() {
		running = false;
		for (Log log : logs)
			log.close();
	}

	/**
	 * Logs an entry. This method is called asynchronously and may therefore block.
	 * 
	 * @param entry
	 *            The log entry.
	 */
	protected abstract void log(Entry entry);

	protected void close() {

	}

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
