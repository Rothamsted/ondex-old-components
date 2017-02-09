package net.sourceforge.ondex.util.metadata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import net.sourceforge.ondex.ONDEXPluginArguments;
import net.sourceforge.ondex.args.FileArgumentDefinition;

import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.parser.oxl.Parser;
import net.sourceforge.ondex.tools.threading.monitoring.FailableThread;
import net.sourceforge.ondex.tools.threading.monitoring.SimpleMonitor;
import net.sourceforge.ondex.util.metadata.actions.CloseAction;
import net.sourceforge.ondex.util.metadata.actions.ExitAction;
import net.sourceforge.ondex.util.metadata.actions.MEAction;
import net.sourceforge.ondex.util.metadata.actions.NewAction;
import net.sourceforge.ondex.util.metadata.actions.OpenAction;
import net.sourceforge.ondex.util.metadata.actions.RedoAction;
import net.sourceforge.ondex.util.metadata.actions.SaveAction;
import net.sourceforge.ondex.util.metadata.actions.UndoAction;
import net.sourceforge.ondex.util.metadata.elements.MEProgressMonitor;

/**
 * Main class
 * 
 * Expects ondex.xsd file as first argument
 * 
 * 
 * @author jweile
 *
 */
public class MetaDataEditor extends JFrame {

	//singleton
	private static MetaDataEditor instance;
	
	//serial id
	private static final long serialVersionUID = -22313014280791064L;
	
	//the toolbar
	private JToolBar toolbar;
	
	//the desktop
	private JDesktopPane desktop;
	
	//the status bar
	private JLabel statusbar;
	
	//set of open editor windows (JInternalFrames) on the desktop
	private Set<MetaDataWindow> windows;
	
	//the Ondex OXL xml schema definition file
	private static File xsdFile;
	

	/**
	 * the main method.
	 * 
	 * @param args program arguments
	 */
	public static void main(final String[] args) {
		ensureConfigFilePresence("config.xml");
		System.setProperty("ondex.dir", new File("ondex_config").getAbsolutePath());
		ensureConfigFilePresence("ondex.xsd");
		xsdFile = new File("ondex_config"+File.separator+"ondex.xsd");
		
		SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        MetaDataEditor mde = getInstance();
                        if (args != null && args.length > 0) {
                                for (String arg : args) {
                                        File file = new File(arg);
                                        if (file.exists()) {
                                                mde.load(file);
                                        }
                                }
                        }
                    }
                });
	}
	
	private static void ensureConfigFilePresence(String filename) {
		File configTarget = new File("ondex_config"+File.separator+filename);
		if (!configTarget.exists()) {
			configTarget.getParentFile().mkdirs();

			try {
				InputStream in = MetaDataEditor.class.getClassLoader().getResourceAsStream(filename);
				OutputStream out = new FileOutputStream(configTarget);
				byte[] buf = new byte[128]; int len;
				while ((len = in.read(buf)) > -1) {
					out.write(buf, 0, len);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, 
						"Cannot access Ondex config file:\n"+configTarget.getAbsolutePath(), 
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
	
	/**
	 * constructor. sets up the gui for the main frame
	 */
	public MetaDataEditor() {
		super("Ondex Meta Data Editor");
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				boolean allClosed = true;
				for (MetaDataWindow mw : getAllMetaDataWindows()) {
					try {
						mw.setSelected(true);
					} catch (PropertyVetoException e1) {}
					allClosed = allClosed && mw.close();
					if (!allClosed) {
						break;
					}
				}
				if (allClosed) {
					System.exit(0);
				}
			}
		});
		
		initWindowSet();
		
		initLaF();
		setupContentPane();
		
		setJMenuBar(new JMenuBar());
		
		toolbar = new JToolBar("File",JToolBar.HORIZONTAL);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		populateActions();
		
		setupWindowMenu();
		
		displayCentrally();
	}
	
	/**
	 * inits the set of windows
	 */
	private void initWindowSet() {
		windows = new TreeSet<MetaDataWindow>(new Comparator<MetaDataWindow>() {

			@Override
			public int compare(MetaDataWindow o1, MetaDataWindow o2) {
				if (o1.id() < o2.id())
					return -1;
				else if (o1.id() == o2.id())
					return 0;
				else
					return 1;
			}
			
		});
	}
	
	/**
	 * sets up the look and feel
	 */
	private void initLaF() {
		try {
			UIManager.setLookAndFeel(UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
	}
	
	/**
	 * populates the menu and the toolbar
	 */
	private void populateActions() {
		addAction(new NewAction());
		addAction(new OpenAction());
		addAction(new SaveAction(false));
		addAction(new SaveAction(true));
		addAction(new CloseAction());
		addAction(new ExitAction());
		
		addAction(new UndoAction());
		addAction(new RedoAction());
		
		setActionEnabled(new String[]{"Edit","Undo"}, false);
		setActionEnabled(new String[]{"Edit","Redo"}, false);
		setActionEnabled(new String[]{"File","Close"}, false);
		setActionEnabled(new String[]{"File","Save"}, false);
		setActionEnabled(new String[]{"File","Save as..."}, false);
	}
	
	/**
	 * for enabling and disabling certain actions in the menus and toolbar.
	 * @param path menu path of the action (e.g. {"File","Open"}
	 * @param enabled whether to enable or disable the action
	 */
	public void setActionEnabled(String[] path, boolean enabled) {
		for (Component c : toolbar.getComponents()) {
			if (c instanceof JButton) {
				JButton button = (JButton) c;
				if (button.getAction().getValue(Action.NAME).equals(path[path.length-1])) {
					button.setEnabled(enabled);
				}
			}
		}
		for (int i = 0 ; i < getJMenuBar().getMenuCount(); i++) {
			JMenu menu = getJMenuBar().getMenu(i);
			if (menu.getText().equals(path[0])) {
				setActionEnabled(path, enabled, menu, 1);
				break;
			}
		}
	}
	
	/**
	 * helper method for the public equivalent. only for recursion purposes.
	 * @param path the menu path
	 * @param enabled enable or disable
	 * @param menu the currently searched menu
	 * @param depth the current path depth
	 */
	private void setActionEnabled(String[] path, boolean enabled, JMenu menu, int depth) {
		for (int i = 0; i < menu.getMenuComponentCount(); i++) {
			if (menu.getMenuComponent(i) instanceof JMenu && path.length -1 > depth) {
				JMenu submenu = (JMenu) menu.getMenuComponent(i);
				if (submenu.getText().equals(path[depth])) {
					setActionEnabled(path, enabled, submenu, depth+1);
				}
			} else if (menu.getMenuComponent(i) instanceof JMenuItem && path.length -1 == depth) {
				JMenuItem item = (JMenuItem) menu.getMenuComponent(i);
				if (item.getText().equals(path[depth])) {
					item.setEnabled(enabled);
				}
			}
		}
	}
	
	/**
	 * sets up the "Windows" menu.
	 * TODO: deprecate and replace with actions.
	 */
	@SuppressWarnings("serial")
	private void setupWindowMenu() {
		JMenu windowMenu = new JMenu("Windows");
		
		Action action = new AbstractAction("Display in parallel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int width = desktop.getWidth() / windows.size();
				int i = 0;
				for (MetaDataWindow w : windows) {
					try {
						w.setIcon(false);
						w.setMaximum(false);
					} catch (PropertyVetoException e1) {}
					w.setBounds(i * width, 0, width, desktop.getHeight());
					i++;
				}
			}
		};
		windowMenu.add(action);
		
		action = new AbstractAction("Iconify All") {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (MetaDataWindow w : windows) {
					try {
						w.setIcon(true);
					} catch (PropertyVetoException pve) {
						//do nothing
					}
				}
			}
		};
		windowMenu.add(action);
		windowMenu.addSeparator();
		getJMenuBar().add(windowMenu);
		
	}
	
	/**
	 * sets up the main window contents: The desktop and the status bar.
	 */
	private void setupContentPane() {
		getContentPane().setLayout(new BorderLayout());
		
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createLoweredBevelBorder());
		desktop = new JDesktopPane();
		p.add(desktop, BorderLayout.CENTER);
		getContentPane().add(p, BorderLayout.CENTER);
		
		statusbar = new JLabel("Ready");
		getContentPane().add(statusbar, BorderLayout.SOUTH);
	}
	
	/**
	 * registers an action with the menubar and the toolbar.
	 * @param action the action to add.
	 */
	public void addAction(MEAction action) {
		String[] menuPath = action.getMenuPath();
		if (action.getMenuPath() != null) {
			JMenu menu = null;
			for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
				JMenu m = getJMenuBar().getMenu(i);
				if (m.getText().equals(menuPath[0])){
					menu = m;
					break;
				}
			}
			if (menu == null) {
				menu = new JMenu(menuPath[0]);
				getJMenuBar().add(menu);
			}
			embedMenuAction(action,1,menu);
		}
		
		if (action.getToolBarLabel() != null) {
			toolbar.add(action);
		}
	}
	
	
	/**
	 * private helper method for addAction() 
	 * recursively searches the menus to find the correct insertion point.
	 * @param action the action to add
	 * @param d the current path depth
	 * @param menu the currently searched menu
	 */
	private void embedMenuAction(MEAction action, int d, JMenu menu) {
		String[] menuPath = action.getMenuPath();
		if (d == menuPath.length -1) {
			if (action.hasSeparator()) {
				menu.addSeparator();
			}
			menu.add(action);
		} else {
			JMenu submenu = null;
			for (int i = 0; i < menu.getMenuComponentCount(); i++) {
				Component c = menu.getMenuComponent(i);
				if (c instanceof JMenu) {
					JMenu potentialSubmenu = (JMenu) c;
					if (potentialSubmenu.getText().equals(menuPath[d])) {
						submenu = potentialSubmenu;
						break;
					}
				}
			}
			if (submenu == null) {
				submenu = new JMenu(menuPath[d]);
				menu.add(submenu);
			}
			embedMenuAction(action,d+1,submenu);
		}
	}

	/**
	 * displays the frame centrally on the screen
	 */
	private void displayCentrally() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if (screen.width < 1024 || screen.height < 768) {
			setSize(screen);
		} else {
			setSize(new Dimension(1024,768));
		}
		setLocation((screen.width - getWidth())/2, (screen.height - getHeight())/2);
		setVisible(true);
	}

	/**
	 * returns the current instance of the MetaDataEditor.
	 */
	public static MetaDataEditor getInstance() {
		if (instance == null) {
			instance = new MetaDataEditor();
		}
		return instance;
	}
	
	/**
	 * returns all currently existing internal editor frames.
	 */
	public MetaDataWindow[] getAllMetaDataWindows() {
		return windows.toArray(new MetaDataWindow[windows.size()]);
	}
	
	/**
	 * returns the currently active internal editor frame.
	 */
	public MetaDataWindow getCurrentMetaDataWindow() {
		for (JInternalFrame f : getAllMetaDataWindows()) {
			MetaDataWindow mw = (MetaDataWindow) f;
			if (mw.isSelected()) {
				return mw;
			}
		}
		return null;
	}
	
	/**
	 * registers a new window with the main frame.
	 * @param w
	 */
	private void registerWindow(final MetaDataWindow w) {
		windows.add(w);
		desktop.add(w);
		JMenu windowMenu = getJMenuBar().getMenu(getJMenuBar().getMenuCount()-1);
		JMenuItem item = new JMenuItem(w.getTitle());
		item.setName(w.id()+"");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					w.setIcon(false);
					w.setSelected(true);
				} catch (PropertyVetoException pve) {
					JOptionPane.showMessageDialog(MetaDataEditor.getInstance(), 
							"Window could not be selected!",
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		windowMenu.add(item);
		setActionEnabled(new String[]{"File","Close"}, true);
		setActionEnabled(new String[]{"File","Save"}, true);
		setActionEnabled(new String[]{"File","Save as..."}, true);
	}
	
	/**
	 * deregisters a closed window with the main frame
	 * @param w
	 */
	private void deregisterWindow(MetaDataWindow w) {
		windows.remove(w);
		JMenu windowMenu = getJMenuBar().getMenu(getJMenuBar().getMenuCount()-1);
		for (Component c : windowMenu.getMenuComponents()) {
			if (c instanceof JMenuItem) {
				JMenuItem item = (JMenuItem) c;
				if (item.getName() != null && item.getName().equals(w.id()+"")) {
					windowMenu.remove(item);
					break;
				}
			}
		}
		if (windows.size() == 0) {
			setActionEnabled(new String[]{"File","Close"}, false);
			setActionEnabled(new String[]{"File","Save"}, false);
			setActionEnabled(new String[]{"File","Save as..."}, false);
		}
	}

	/**
	 * creates a new internal editor frame
	 * @param md the metadata object to use
	 * @param file the file to use
	 */
	public MetaDataWindow createMetaDataWindow(ONDEXGraphMetaData md, File file) {
		final MetaDataWindow w = new MetaDataWindow(md, file);
		registerWindow(w);
		w.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				deregisterWindow(w);
			}
		});
		try {
			w.setSelected(true);
			w.setMaximum(true);
		} catch (PropertyVetoException e) {}
		return w;
	}
	
	/**
	 * loads a new file
	 * @param file the file to load.
	 */
	public void load(final File file) {
		if (file.exists()) {
			final SimpleMonitor mo = new SimpleMonitor("Validating XML file...",2);
			FailableThread th = new FailableThread("File reader thread"){
			
				@Override
				public void failableRun() throws Throwable {

//					Initialisation init = new Initialisation(file, xsdFile);
					mo.next("Reading XML file...");
					MemoryONDEXGraph og = new MemoryONDEXGraph("dummygraph");
//					init.initMetaData(og);

                                        Parser oxlParser = new Parser();
                                        oxlParser.setONDEXGraph(og);
                                        ONDEXPluginArguments args = new ONDEXPluginArguments(oxlParser.getArgumentDefinitions());
                                        args.addOption(FileArgumentDefinition.INPUT_FILE, file.getAbsolutePath());
                                        oxlParser.setArguments(args);
                                        oxlParser.start();

					ONDEXGraphMetaData md = og.getMetaData();
					mo.next("Initializing UI tree structure...");
					createMetaDataWindow(md, file);
					mo.complete();
				}
			};
			th.start();
			MEProgressMonitor.start("Opening file", mo);
		} else {
			JOptionPane.showMessageDialog(this, "File does not exist", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
