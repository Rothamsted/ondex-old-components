package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.MetaDataWindow;

public class CloseAction extends MEAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6071522113414591176L;

	public CloseAction() {
		super("Close");
		KeyStroke key = KeyStroke.getKeyStroke("ctrl W");
		putValue(ACCELERATOR_KEY, key);

		putValue(SMALL_ICON, fetchIcon("img/mail-mark-not-junk.png"));
	}
	
	@Override
	public String[] getMenuPath() {
		return new String[]{"File","Close"};
	}

	@Override
	public String getToolBarLabel() {
		return (String)getValue(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataEditor me = MetaDataEditor.getInstance();
		MetaDataWindow mw = me.getCurrentMetaDataWindow();
		if (mw != null) {
			mw.close();
		}
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

}
