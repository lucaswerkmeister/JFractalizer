package de.lucaswerkmeister.jfractalizer.framework;

/**
 * The {@link Plugin} class manages everything that concerns plugins.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface Plugin {
	/**
	 * Registers log IDs.
	 */
	public void registerLogIDs();

	/**
	 * Gets the plugin vendor's name.
	 * 
	 * @return The vendor name.
	 */
	public String getVendorName();

	/**
	 * Gets the plugin's name.
	 * 
	 * @return The plugin name.
	 */
	public String getPluginName();

	/**
	 * Decodes a logging entry into a string.
	 * 
	 * @param id
	 *            The log ID.
	 * @param args
	 *            The arguments of the logging entry.
	 * @return A string representing the logging entry.
	 */
	public String logToString(int id, Object... args);
}
