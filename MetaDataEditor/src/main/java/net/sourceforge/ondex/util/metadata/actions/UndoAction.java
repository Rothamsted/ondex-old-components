package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.MetaDataWindow;

public class UndoAction extends MEAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4645775869848402372L;

	public UndoAction() {
		super("Undo");
		KeyStroke key = KeyStroke.getKeyStroke("ctrl Z");
		putValue(ACCELERATOR_KEY, key);
		putValue(SMALL_ICON, fetchIcon("img/edit-undo.png"));
	}
	
	@Override
	public String[] getMenuPath() {
		return new String[]{"Edit","Undo"};
	}

	@Override
	public String getToolBarLabel() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataWindow w = MetaDataEditor.getInstance().getCurrentMetaDataWindow();
		if (w != null){
			w.undo();
		}
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

}
