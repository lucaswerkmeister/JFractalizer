package de.lucaswerkmeister.jfractalizer;

public interface CommandLineConfigurable {
	/**
	 * If the JFractalizer was started with command line arguments, some of them are passed to the
	 * {@link CommandLineConfigurable} (one by one) via this method.
	 * 
	 * @param args
	 *            A string that contains a single option.
	 * @param optionName
	 *            The name of the option. Given purely for convenience.
	 * @param optionContent
	 *            The content of the option. Given purely for convenience.
	 */
	public abstract void handleCommandLineOption(String option, String optionName, String optionContent);
}
