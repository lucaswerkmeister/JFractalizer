package de.lucaswerkmeister.jfractalizer.defaultPlugin;

import static de.lucaswerkmeister.jfractalizer.framework.Log.registerID;

import java.awt.Rectangle;

import de.lucaswerkmeister.jfractalizer.defaultPlugin.cameras.Steadicam;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.CifCanvas;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.CifFractal;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.CifMenuListener;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.JuliaSet;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.MandelbrotMenuListener;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.cif.MandelbrotSet;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.ColorNode;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.EditDialogPalette;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.HsbRotatePalette;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.NodePalette;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.SelectableColor;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes.SimplePalette;
import de.lucaswerkmeister.jfractalizer.framework.Log.Entry.Level;
import de.lucaswerkmeister.jfractalizer.framework.Plugin;

public class DefaultPlugin implements Plugin {
	public static final int	LOG_PLUGIN_PREFIX	= (0 << 24) + (1 << 16);

	@Override
	public void registerLogIDs() {
		registerID(CifFractal.LOG_CHANGED_IMAGE_TYPE, Level.INFO, this);
		registerID(CifFractal.LOG_SAVING, Level.INFO, this);
		registerID(CifFractal.LOG_SET_PALETTE, Level.INFO, this);
		registerID(CifFractal.LOG_START_CALCULATION, Level.INFO, this);
		registerID(CifFractal.LOG_STOP_CALCULATION, Level.INFO, this);
		registerID(CifFractal.LOG_INIT_MENU, Level.INFO, this);
		registerID(CifFractal.LOG_ZOOM, Level.INFO, this);
		registerID(CifFractal.LOG_ZOOM_TO_START, Level.INFO, this);
		registerID(CifFractal.LOG_SHUTDOWN, Level.INFO, this);
		registerID(CifCanvas.LOG_GO_TO_SELECTED_AREA, Level.INFO, this);
		registerID(CifMenuListener.LOG_EDIT_BOUNDARIES, Level.INFO, this);
		registerID(CifMenuListener.LOG_EDIT_ADDITIONAL_PARAMETERS, Level.INFO, this);
		registerID(CifMenuListener.LOG_RECALCULATE, Level.INFO, this);
		registerID(CifMenuListener.LOG_UNDO, Level.INFO, this);
		registerID(CifMenuListener.LOG_REDO, Level.INFO, this);
		registerID(MandelbrotSet.LOG_INIT_CONTEXT_MENU, Level.INFO, this);
		registerID(MandelbrotMenuListener.LOG_SWITCH_JULIA, Level.INFO, this);
		registerID(JuliaSet.LOG_SWITCHED, Level.INFO, this);
		registerID(EditDialogPalette.LOG_INIT_MENU, Level.INFO, this);
		registerID(EditDialogPalette.LOG_SHOW_EDIT_DIALOG, Level.INFO, this);
		registerID(EditDialogPalette.LOG_EDITED_PALETTE, Level.INFO, this);
		registerID(SelectableColor.LOG_SHOW_EDIT_DIALOG, Level.INFO, this);
		registerID(SelectableColor.LOG_EDITED_COLOR, Level.INFO, this);
		registerID(SimplePalette.LOG_SAVING, Level.INFO, this);
		registerID(NodePalette.LOG_SAVING, Level.INFO, this);
		registerID(ColorNode.LOG_UPDATE, Level.INFO, this);
		registerID(HsbRotatePalette.LOG_SAVING, Level.INFO, this);
		registerID(Steadicam.LOG_ADDED_OUTPUT, Level.INFO, this);
		registerID(Steadicam.LOG_START_FILMING, Level.INFO, this);
		registerID(Steadicam.LOG_START_FRAME, Level.INFO, this);
		registerID(Steadicam.LOG_END_FRAME, Level.INFO, this);
		registerID(Steadicam.LOG_START_WRITE, Level.INFO, this);
		registerID(Steadicam.LOG_END_WRITE, Level.INFO, this);
		registerID(Steadicam.LOG_GC, Level.INFO, this);
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
			case CifFractal.LOG_CHANGED_IMAGE_TYPE:
				return args[0].getClass().getName() + ": Changed image type to " + args[1];
			case CifFractal.LOG_SAVING:
				return args[0].getClass().getName() + ": Saving";
			case CifFractal.LOG_SET_PALETTE:
				return args[0].getClass().getName() + ": Set color palette to " + args[1];
			case CifFractal.LOG_START_CALCULATION:
				return args[0].getClass().getName() + ": Starting calculation";
			case CifFractal.LOG_STOP_CALCULATION:
				return args[0].getClass().getName() + ": Stopping calculation";
			case CifFractal.LOG_INIT_MENU:
				return args[0].getClass().getName() + ": Initialized menu";
			case CifFractal.LOG_ZOOM:
				return args[0].getClass().getName() + ": Zooming on " + args[1] + "|" + args[2] + " by " + args[3];
			case CifFractal.LOG_ZOOM_TO_START:
				return args[0].getClass().getName() + ": Zooming to start on " + args[1] + "|" + args[2] + " by "
						+ args[3];
			case CifFractal.LOG_SHUTDOWN:
				return args[0].getClass().getName() + ": Shutting down";
			case CifCanvas.LOG_GO_TO_SELECTED_AREA:
				Rectangle rect = (Rectangle) args[1];
				return "CifCanvas: Go to selected area " + rect.toString();
			case CifMenuListener.LOG_EDIT_BOUNDARIES:
				return "CifMenuListener: Edit boundaries (new boundaries: " + args[0] + ", " + args[1] + ")";
			case CifMenuListener.LOG_EDIT_ADDITIONAL_PARAMETERS:
				return "CifMenuListener: Edit additional parameters (supersampling " + args[0] + ", max passes "
						+ args[1] + ")";
			case CifMenuListener.LOG_RECALCULATE:
				return "CifMenuListener: Recalculate";
			case CifMenuListener.LOG_UNDO:
				return "CifMenuListener: Undo";
			case CifMenuListener.LOG_REDO:
				return "CifMenuListener: Redo";
			case MandelbrotSet.LOG_INIT_CONTEXT_MENU:
				return "MandelbrotSet: Initialized context menu";
			case MandelbrotMenuListener.LOG_SWITCH_JULIA:
				return "MandelbrotMenuListener: Switch to Julia set";
			case JuliaSet.LOG_SWITCHED:
				return "JuliaSet: Switched to Julia set, adopting coordinate " + args[0] + "+" + args[1] + "i";
			case EditDialogPalette.LOG_INIT_MENU:
				return args[0].getClass().getName() + ": Initialized menu";
			case EditDialogPalette.LOG_SHOW_EDIT_DIALOG:
				return args[0].getClass().getName() + ": Showing edit dialog";
			case EditDialogPalette.LOG_EDITED_PALETTE:
				return args[0].getClass().getName() + ": Changed color palette from " + args[0] + " to " + args[1];
			case SelectableColor.LOG_SHOW_EDIT_DIALOG:
				return "SelectableColor: Showing edit dialog.";
			case SelectableColor.LOG_EDITED_COLOR:
				return "SelectableColor: Changed color from " + args[1] + " to " + args[2];
			case SimplePalette.LOG_SAVING:
				return "SimplePalette: Saving";
			case NodePalette.LOG_SAVING:
				return "NodePalette: Saving";
			case ColorNode.LOG_UPDATE:
				return "ColorNode: Updated node " + args[0] + " from " + args[1] + " to " + args[2];
			case HsbRotatePalette.LOG_SAVING:
				return "HsbRotatePalette: Saving";
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
		}
		throw new IllegalArgumentException("Unknown log ID!");
	}
}
