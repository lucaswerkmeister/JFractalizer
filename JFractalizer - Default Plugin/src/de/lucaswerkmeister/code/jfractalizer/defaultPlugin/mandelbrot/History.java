/*
 * JFractalizer, a Java Fractal Program. Copyright (C) 2012 Lucas Werkmeister
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.lucaswerkmeister.code.jfractalizer.defaultPlugin.mandelbrot;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>History</code> class provides methods to store a specific state of anything and moving back and forth between different states of it.
 * After a specified number of states, old states are either discarded or stored to the hard disk.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public class History<T>
{
	private final List<T>	states;
	private final int		maxStates;
	private int				currentState;
	private int				lastState;

	/**
	 * Creates a <code>History</code> that will store 10 states. If more states are added, older states are discarded.
	 */
	public History()
	{
		this(10);
	}

	/**
	 * Creates a <code>History</code> that will store the specified amount of states.
	 * 
	 * @param capacity
	 *            Specifies how many states will be stored. If more states are added, older states are discarded.
	 */
	public History(int capacity)
	{
		states = new ArrayList<>(capacity);
		maxStates = capacity;
		currentState = lastState = -1;
	}

	/**
	 * Adds a new state to the <code>History</code>. If the current state of the <code>History</code> is not the last one (i. e. if the
	 * <code>History</code> can redo), all newer states will be discarded.
	 * 
	 * @param newState
	 */
	public void add(T newState)
	{
		if (currentState != lastState)
		{
			for (int i = currentState + 1; i <= lastState; i++)
				states.remove(i);
		}
		if (currentState == maxStates)
		{
			for (int i = 0; i < maxStates - 1;)
				states.set(i, states.get(++i));
			states.set(maxStates - 1, newState);
		}
		else
		{
			states.add(newState);
			currentState++;
			if (lastState < currentState)
				lastState = currentState;
		}
	}

	/**
	 * Gets the current state of the <code>History</code>, or <code>null</code> if no states have been added yet.
	 * 
	 * @return The current state of the <code>History</code> if it contains any states, <code>null</code> otherwise.
	 */
	public T getCurrentState()
	{
		if (currentState <= lastState)
			return states.get(currentState);
		return null;
	}

	/**
	 * If there are states to be undone, moves one state back and returns it.
	 * 
	 * @return The previous state if there is one, <code>null</code> otherwise.
	 */
	public T undo()
	{
		if (canUndo())
			return states.get(--currentState);
		return null;
	}

	/**
	 * If there are states to be redone, moves one state forth and returns it.
	 * 
	 * @return The next state if there is one, <code>null</code> otherwise.
	 */
	public T redo()
	{
		if (canRedo())
			return states.get(++currentState);
		return null;
	}

	/**
	 * Determines if there are states to be undone.
	 * 
	 * @return <code>true</code> if there are previous states, <code>false</code> otherwise.
	 */
	public boolean canUndo()
	{
		return currentState > 0;
	}

	/**
	 * Determines if there are states to be redone.
	 * 
	 * @return <code>true</code> if there are further states, <code>false</code> otherwise.
	 */
	public boolean canRedo()
	{
		return currentState <= lastState;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		for (T t : states)
			b.append(t.toString());
		return b.toString();
	}
}
