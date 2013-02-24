package de.lucaswerkmeister.jfractalizer;

/**
 * A {@link Camera} films a movie of a fractal. It does so by repeatedly zooming on the fractal and sending the
 * resulting image to one or several {@link Output Outputs}.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface Camera extends SelectableService, CommandLineConfigurable {

	/**
	 * Adds an output to this {@link Camera}.
	 * <p>
	 * All images calculated after this call will be sent to this output.
	 * <p>
	 * Behavior when adding an output after {@link #startFilming(Fractal) filming has started} is unspecified.
	 * 
	 * @param output
	 */
	public void addOutput(Output output);

	/**
	 * Starts the filming. This method returns immediately.
	 * 
	 * @param fractal
	 *            The fractal which to film.
	 */
	public void startFilming(Fractal fractal);
}
