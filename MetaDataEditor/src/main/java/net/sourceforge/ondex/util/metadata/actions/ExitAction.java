package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;

import javax.swing.KeyStroke;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.MetaDataWindow;

public class ExitAction extends MEAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5876241637789887028L;

	public ExitAction() {
		super("Exit");
		KeyStroke key = KeyStroke.getKeyStroke("alt F4");
		putValue(ACCELERATOR_KEY, key);
	}
	
	@Override
	public String[] getMenuPath() {
		return new String[]{"File","Exit"};
	}

	@Override
	public String getToolBarLabel() {
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataEditor me = MetaDataEditor.getInstance();
		for (MetaDataWindow mw : me.getAllMetaDataWindows()) {
			try {
				mw.setSelected(true);
			} catch (PropertyVetoException e1) {}
			mw.close();
		}
		System.exit(0);
	}

	@Override
	public boolean hasSeparator() {
		return true;
	}

}
