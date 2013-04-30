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
package de.lucaswerkmeister.jfractalizer.defaultPlugin.cif;

/**
 * The <code>History</code> class provides methods to store a specific state of anything and moving back and forth
 * between different states of it. After a specified number of states, old states are discarded.
 * <p>
 * Does not support null elements.
 * 
 * @author Lucas Werkmeister
 * @version 2.0
 */
public class History<T> {
	private final int	maxLength;

	private Entry		dummy	= new Entry(null);
	private Entry		current	= dummy;
	private int			length	= 0;

	/**
	 * Creates a <code>History</code> that will store 16 states. If more states are added, older states are discarded.
	 */
	public History() {
		this(16);
	}

	/**
	 * Creates a <code>History</code> that will store the specified amount of states.
	 * 
	 * @param capacity
	 *            Specifies how many states will be stored. If more states are added, older states are discarded. A
	 *            value of -1 means "do not discard old states".
	 */
	public History(final int capacity) {
		maxLength = capacity;
		dummy.next = dummy.prev = dummy;
	}

	/**
	 * Adds a new state to the <code>History</code>. If the current state of the <code>History</code> is not the last
	 * one (i. e. if the <code>History</code> can redo), all newer states will be discarded.
	 * 
	 * @param newState
	 *            The new state.
	 * @throws IllegalArgumentException
	 *             When newState is null.
	 */
	public void add(final T newState) throws IllegalArgumentException {
		if (newState == null)
			throw new IllegalArgumentException("Can't store null value in the history!");
		boolean needToRecount = current.next != dummy && maxLength != -1;
		current.append(newState);
		current = current.next;
		if (needToRecount) {
			int length = 0; // shadow the instance variable and set it once below for better performance
			Entry counter = dummy;
			while (counter.next != dummy) {
				counter = counter.next;
				length++; // this only operates on the method frame stack instead of a lot of getfield/putfield action
			}
			this.length = length;
		}
		while (maxLength != -1 && length > maxLength) {
			dummy.link(dummy.next.next);
			length--;
		}
	}

	/**
	 * Gets the current state of the <code>History</code>, or <code>null</code> if no states have been added yet.
	 * 
	 * @return The current state of the <code>History</code> if it contains any states, <code>null</code> otherwise.
	 */
	public T getCurrentState() {
		return current.content;
	}

	/**
	 * If there are states to be undone, moves one state back and returns it.
	 * 
	 * @return The previous state if there is one, <code>null</code> otherwise.
	 */
	public T undo() {
		if (canUndo())
			return (current = current.prev).content;
		return null;
	}

	/**
	 * If there are states to be redone, moves one state forth and returns it.
	 * 
	 * @return The next state if there is one, <code>null</code> otherwise.
	 */
	public T redo() {
		if (canRedo())
			return (current = current.next).content;
		return null;
	}

	/**
	 * Determines if there are states to be undone.
	 * 
	 * @return <code>true</code> if there are previous states, <code>false</code> otherwise.
	 */
	public boolean canUndo() {
		return current.prev != dummy;
	}

	/**
	 * Determines if there are states to be redone.
	 * 
	 * @return <code>true</code> if there are further states, <code>false</code> otherwise.
	 */
	public boolean canRedo() {
		return current.next != dummy;
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		Entry pointer = dummy;
		while (pointer.next != dummy) {
			pointer = pointer.next;
			b.append(pointer.content);
		}
		return b.toString();
	}

	/**
	 * An entry in this little Doubly Linked List implementation.
	 * 
	 * @author Lucas Werkmeister
	 * @version 1.0
	 */
	private class Entry {
		Entry	prev;
		Entry	next;
		T		content;

		/**
		 * Creates a new {@link Entry} with the specified content.
		 * 
		 * @param content
		 *            The content of the entry.
		 */
		Entry(T content) {
			this.content = content;
			next = prev = dummy;
		}

		/**
		 * Appends a new Entry to this entry, with the specified content.
		 * 
		 * @param content
		 *            The content for the new, following, entry.
		 */
		void append(T content) {
			Entry newEntry = new Entry(content);
			newEntry.prev = this;
			newEntry.next = dummy;
			next = newEntry;
		}

		/**
		 * Links that Entry after this one, such that <code>other.prev = this</code> and <code>this.next = other</code>.
		 * 
		 * @param other
		 *            The other Entry.
		 */
		void link(Entry other) {
			next = other;
			other.prev = this;
		}
	}
}
