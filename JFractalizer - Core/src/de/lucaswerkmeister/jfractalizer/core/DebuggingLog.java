package de.lucaswerkmeister.jfractalizer.core;

import java.util.LinkedList;
import java.util.List;

import de.lucaswerkmeister.jfractalizer.framework.Log;

/**
 * This log keeps all entries as objects so that a developer can inspect them when debugging.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class DebuggingLog extends Log {
	private final List<Entry>	entries	= new LinkedList<>();
	private final Thread		runner	= new Thread() {
											@Override
											public void run() {
												while (running) {
													try {
														sleep(1000);
													}
													catch (InterruptedException e) {
														// Do nothing
													}
													breakpoint();
												}
											};
										};

	public DebuggingLog() {
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	@Override
	protected void log(Entry entry) {
		entries.add(entry);
	}

	private void breakpoint() {
		// Set a breakpoint here to watch the content of the entries list. The method is called every second.
	}
}
