package de.lucaswerkmeister.jfractalizer.defaultPlugin;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam;
import de.lucaswerkmeister.jfractalizer.framework.Log;
import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;
import de.lucaswerkmeister.jfractalizer.framework.Plugin;

public class DefaultPlugin implements Plugin {

	@Override
	public void registerLogIDs() {
		Log.registerID(Steadicam.LOG_ADDED_OUTPUT, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_FILMING, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_FRAME, Level.INFO, this);
		Log.registerID(Steadicam.LOG_END_FRAME, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_WRITE, Level.INFO, this);
		Log.registerID(Steadicam.LOG_END_WRITE, Level.INFO, this);
	}

	@Override
	public String getVendorName() {
		return "Lucas Werkmeister";
	}

	@Override
	public String getPluginName() {
		return "Default Plugin";
	}

	@Override
	public String logToString(int id, Object... args) {
		switch (id) {
			case Steadicam.LOG_ADDED_OUTPUT:
				return "Steadicam: Added Output " + args[0].toString();
			case Steadicam.LOG_START_FILMING:
				return "Steadicam: Started filming of fractal " + args[0].toString();
			case Steadicam.LOG_START_FRAME:
				return "Steadicam: Started calculation of frame #" + args[0].toString();
			case Steadicam.LOG_END_FRAME:
				return "Steadicam: Finished calculation of frame #" + args[0].toString();
			case Steadicam.LOG_START_WRITE:
				return "Steadicam: Started writing frame #" + args[0].toString();
			case Steadicam.LOG_END_WRITE:
				return "Steadicam: Finished writing frame #" + args[0].toString();
		}
		throw new IllegalArgumentException("Unknown log ID!");
	}
}
