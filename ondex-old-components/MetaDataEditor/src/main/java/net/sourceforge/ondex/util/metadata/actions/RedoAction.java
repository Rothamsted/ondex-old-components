package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.MetaDataWindow;

public class RedoAction extends MEAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -509844661949704829L;

	public RedoAction() {
		super("Redo");
		KeyStroke key = KeyStroke.getKeyStroke("ctrl Y");
		putValue(ACCELERATOR_KEY, key);
		putValue(SMALL_ICON, fetchIcon("img/edit-redo.png"));
	}

	@Override
	public String[] getMenuPath() {
		return new String[]{"Edit","Redo"};
	}

	@Override
	public String getToolBarLabel() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataWindow w = MetaDataEditor.getInstance().getCurrentMetaDataWindow();
		if (w != null){
			w.redo();
		}
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

}
