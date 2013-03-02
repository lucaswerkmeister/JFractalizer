package de.lucaswerkmeister.jfractalizer.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.lucaswerkmeister.jfractalizer.core.Log.Entry.Level;
import de.lucaswerkmeister.jfractalizer.framework.Plugin;

public abstract class Log {
	private static final Set<Log>				logs	= new HashSet<>();
	protected static final Map<Integer, Plugin>	ids		= new HashMap<>();
	private static final BlockingQueue<Entry>	entries	= new LinkedBlockingQueue<>();
	private static boolean						running	= true;
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

	public static final void registerID(int id, Plugin plugin) {
		ids.put(id, plugin);
	}

	public static final void registerLog(Log log) {
		logs.add(log);
	}

	public static final void info(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.INFO, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	public static final void warning(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.WARNING, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

	public static final void error(int id, Object... args) {
		try {
			entries.put(new Entry(id, Level.ERROR, args));
		}
		catch (InterruptedException e) {
			// Do nothing, will never happen
		}
	}

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

	protected abstract void log(Entry entry);

	public static class Entry {
		public static enum Level {
			INFO, WARNING, ERROR, CRASH
		}

		public final long		time;
		public final int		id;
		public final Object[]	args;
		public final Level		level;

		public Entry(int id, Level level, Object[] args) {
			this.time = System.currentTimeMillis();
			this.id = id;
			this.args = args;
			this.level = level;
		}
	}
}
