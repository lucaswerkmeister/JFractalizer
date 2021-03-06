package de.lucaswerkmeister.jfractalizer.defaultPlugin.palettes;

import static de.lucaswerkmeister.jfractalizer.framework.Log.log;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import de.lucaswerkmeister.jfractalizer.core.Core;
import de.lucaswerkmeister.jfractalizer.defaultPlugin.DefaultPlugin;
import de.lucaswerkmeister.jfractalizer.framework.ColorPalette;

public abstract class EditDialogPalette implements ActionListener, ColorPalette {
	private Frame			owner;
	public static final int	LOG_CLASS_PREFIX		= DefaultPlugin.LOG_PLUGIN_PREFIX + (((4 << 5) + (0 << 0)) << 8);
	public static final int	LOG_INIT_MENU			= LOG_CLASS_PREFIX + 0;
	public static final int	LOG_SHOW_EDIT_DIALOG	= LOG_CLASS_PREFIX + 1;
	public static final int	LOG_EDITED_PALETTE		= LOG_CLASS_PREFIX + 2;

	@Override
	public void initMenu(Menu colorPaletteMenu, Frame owner) {
		this.owner = owner;
		final MenuItem edit = new MenuItem("Edit Color Palette...", new MenuShortcut(KeyEvent.VK_E, true));
		edit.addActionListener(this);
		colorPaletteMenu.add(edit);
		log(LOG_INIT_MENU, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Edit Color Palette...":
				PaletteEditDialog d = makeEditDialog(owner);
				log(LOG_SHOW_EDIT_DIALOG, this, d);
				d.setVisible(true);
				final EditDialogPalette newPalette = d.getPalette();
				if (!this.equals(newPalette)) {
					log(LOG_EDITED_PALETTE, this, newPalette);
					Core.setCurrentColorPalette(newPalette);
				}
				break;
		}
	}

	protected abstract PaletteEditDialog makeEditDialog(Frame owner);

	public static abstract class PaletteEditDialog extends Dialog {
		private static final long			serialVersionUID	= 4600711463618687779L;
		protected final EditDialogPalette	original;

		public PaletteEditDialog(Frame owner, EditDialogPalette original) {
			super(owner, "Edit Color Palette", true);
			this.original = original;
		}

		public abstract EditDialogPalette getPalette();
	}
}
