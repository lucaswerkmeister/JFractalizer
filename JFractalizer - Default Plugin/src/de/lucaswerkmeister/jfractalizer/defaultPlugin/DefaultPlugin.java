package de.lucaswerkmeister.jfractalizer.defaultPlugin;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.EditDialogPalette;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.SelectableColor;
import de.lucaswerkmeister.jfractalizer.framework.Log;
import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;
import de.lucaswerkmeister.jfractalizer.framework.Plugin;

public class DefaultPlugin implements Plugin {
	public static final int	LOG_PLUGIN_PREFIX	= (0 << 24) + (1 << 16);

	@Override
	public void registerLogIDs() {
		Log.registerID(Steadicam.LOG_ADDED_OUTPUT, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_FILMING, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_FRAME, Level.INFO, this);
		Log.registerID(Steadicam.LOG_END_FRAME, Level.INFO, this);
		Log.registerID(Steadicam.LOG_START_WRITE, Level.INFO, this);
		Log.registerID(Steadicam.LOG_END_WRITE, Level.INFO, this);
		Log.registerID(Steadicam.LOG_GC, Level.INFO, this);
		Log.registerID(EditDialogPalette.LOG_INIT_MENU, Level.INFO, this);
		Log.registerID(EditDialogPalette.LOG_SHOW_EDIT_DIALOG, Level.INFO, this);
		Log.registerID(EditDialogPalette.LOG_EDITED_PALETTE, Level.INFO, this);
		Log.registerID(SelectableColor.LOG_SHOW_EDIT_DIALOG, Level.INFO, this);
		Log.registerID(SelectableColor.LOG_EDITED_COLOR, Level.INFO, this);
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
			case Steadicam.LOG_GC:
				return "Steadicam: Triggered a Garbage Collection.";
			case EditDialogPalette.LOG_INIT_MENU:
				return args[0].getClass().getName() + ": Initialized menu.";
			case EditDialogPalette.LOG_SHOW_EDIT_DIALOG:
				return args[0].getClass().getName() + ": Showing edit dialog.";
			case EditDialogPalette.LOG_EDITED_PALETTE:
				return args[0].getClass().getName() + ": Changed color palette from " + args[0] + " to " + args[1]
						+ ".";
			case SelectableColor.LOG_SHOW_EDIT_DIALOG:
				return "SelectableColor: Showing edit dialog.";
			case SelectableColor.LOG_EDITED_COLOR:
				return "SelectableColor: Changed color from " + args[1] + " to " + args[2] + ".";
		}
		throw new IllegalArgumentException("Unknown log ID!");
	}
}
