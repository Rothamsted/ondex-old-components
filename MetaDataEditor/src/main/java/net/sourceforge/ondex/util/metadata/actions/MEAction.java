package net.sourceforge.ondex.util.metadata.actions;

import java.io.File;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class MEAction extends AbstractAction {

	private static final long serialVersionUID = 685453528115658352L;

	public MEAction() {
		super();
	}

	public MEAction(String name) {
		super(name);
	}
	
	public MEAction(String name, Icon icon) {
		super(name, icon);
	}

	public abstract String[] getMenuPath();
	
	public abstract String getToolBarLabel();
	
	public abstract boolean hasSeparator();
	
	protected ImageIcon fetchIcon(String path) {
		ImageIcon icon = null;
		File imgFile = new File("src/main/resources/"+path);
		if (imgFile.exists()) {
			icon = new ImageIcon(imgFile.getAbsolutePath());
		} else {
			URL imgURL = getClass().getClassLoader().getResource(path);
			System.out.println(imgURL);
			icon = new ImageIcon(imgURL);
		}
		return icon;
	}
	
}
