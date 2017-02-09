package net.sourceforge.ondex.util.metadata.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.ondex.util.metadata.MetaDataEditor;

public class OpenAction extends MEAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1317487623200351551L;
	public static JFileChooser fc;
	
	static {
		fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory())
					return true;
				else
					return f.getName().endsWith(".xml");
			}

			@Override
			public String getDescription() {
				return "XML File";
			}
			
		});
	}
	
	public OpenAction() {
		super("Open");
		
		KeyStroke key = KeyStroke.getKeyStroke("ctrl O");
		putValue(ACCELERATOR_KEY, key);
		
		putValue(SMALL_ICON, fetchIcon("img/document-open.png"));
	}

	@Override
	public String[] getMenuPath() {
		return new String[]{"File","Open..."};
	}

	@Override
	public String getToolBarLabel() {
		return (String)getValue(NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MetaDataEditor mde = MetaDataEditor.getInstance();
		int retval = fc.showOpenDialog(mde);
		switch (retval) {
		case JFileChooser.APPROVE_OPTION:
			File file = fc.getSelectedFile();
			mde.load(file);
			break;
		case JFileChooser.CANCEL_OPTION:
			break;
		case JFileChooser.ERROR_OPTION:
			break;
		}
	}

	@Override
	public boolean hasSeparator() {
		return false;
	}

}
