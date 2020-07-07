package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;

public class NewAction extends MEAction {
	
	
	private static final long serialVersionUID = 202914355304666437L;

	public NewAction() {
		super("New");
		KeyStroke key = KeyStroke.getKeyStroke("ctrl N");
		putValue(ACCELERATOR_KEY, key);
		putValue(SMALL_ICON, fetchIcon("img/document-new.png"));
	}
	
	@Override
	public String[] getMenuPath() {
		return new String[]{"File","New"};
	}

	@Override
	public String getToolBarLabel() {
		return (String) getValue(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataEditor me = MetaDataEditor.getInstance();
		me.createMetaDataWindow(null, null);
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

}
