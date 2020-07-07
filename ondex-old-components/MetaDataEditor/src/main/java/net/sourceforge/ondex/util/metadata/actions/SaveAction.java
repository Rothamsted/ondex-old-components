package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;

public class SaveAction extends MEAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6443858332033431565L;
	
	private boolean as;
	
	public SaveAction(boolean as) {
		super(as?"Save as...":"Save");
		this.as = as;
		if (!as) {
			KeyStroke key = KeyStroke.getKeyStroke("ctrl S");
			putValue(ACCELERATOR_KEY, key);
			
			putValue(SMALL_ICON, fetchIcon("img/document-save-as.png"));
		} else {
			KeyStroke key = KeyStroke.getKeyStroke("ctrl shift S");
			putValue(ACCELERATOR_KEY, key);
			
			putValue(SMALL_ICON, fetchIcon("img/document-save.png"));
		}
	}
	
	@Override
	public String[] getMenuPath() {
		if (as){
			return new String[]{"File","Save"};
		} else {
			return new String[]{"File","Save As..."};
		}
	}

	@Override
	public String getToolBarLabel() {
		return as ? null : "Save";
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (MetaDataEditor.getInstance().getCurrentMetaDataWindow() != null) {
			if (as) {
				MetaDataEditor.getInstance().getCurrentMetaDataWindow().saveAs();
			} else {
				MetaDataEditor.getInstance().getCurrentMetaDataWindow().save();
			}
		}
	}

}
