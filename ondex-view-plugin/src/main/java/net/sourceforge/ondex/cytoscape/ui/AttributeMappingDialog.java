/*
 * OndexView plug-in for Cytoscape
 * Copyright (C) 2010  University of Newcastle upon Tyne
 * 
 * This file is part of OndexView.
 * 
 * OndexView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OndexView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OndexView.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ondex.cytoscape.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.ondex.cytoscape.task.MapSecAttTask;
import net.sourceforge.ondex.cytoscape.task.OndexTaskConfig;
import net.sourceforge.ondex.cytoscape.task.ScanForKeysTask;
import cytoscape.Cytoscape;
import cytoscape.task.util.TaskManager;

/**
 * dialog for creating secondary attribute mappings.
 * @author jweile
 *
 */
public class AttributeMappingDialog extends JDialog {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * text fields for file and name
	 */
	private JTextField fileTF, nameTF;
	
	/**
	 * dropdown menu for key selection
	 */
	private JComboBox keybox;
	
	/**
	 * file chooser
	 */
	private JFileChooser fc;
	
	/**
	 * ok button
	 */
	private JButton okButton;
	
	/**
	 * checks whether key and file have been selected.
	 */
	private boolean keySelected = false, fileSelected = false;

	/**
	 * list of keys.
	 */
	private List<String> keys;

	/**
	 * creates an entry for this mapping in the cytoscape menu bar.
	 */
	@SuppressWarnings("serial")
	public static void registerInMenuBar() {
		JMenuBar mb = Cytoscape.getDesktop().getJMenuBar();
		for (int i = 0; i < mb.getMenuCount(); i++) {
			JMenu menu = mb.getMenu(i);
			if (menu.getText().equals("File")) {
				for (Component mc : menu.getMenuComponents()) {
					if (mc instanceof JMenu) {
						JMenu submenu = (JMenu) mc;
						if (submenu.getText().equals("Import")) {
							submenu.add(new AbstractAction("Secondary Attribute Map...") {
								public void actionPerformed(ActionEvent e) {
									ScanForKeysTask t = new ScanForKeysTask();
									TaskManager.executeTask(t, OndexTaskConfig.getInstance());
									List<String> keys = t.waitForKeys();
									if (keys.size() > 0) {
										new AttributeMappingDialog(keys);
									} else {
										JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
												"There are no unique attributes present in the current graph.", 
												"Unable to comply", 
												JOptionPane.INFORMATION_MESSAGE);
									}
								}
							});
							break;
						}
					}
				}
				break;
			}
		}
	}
	
	/**
	 * get file chooser.
	 * @return
	 */
	private JFileChooser getFileChooser() {
		if (fc == null) {
			fc = new JFileChooser();
			fc.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory() 
							|| f.getName().endsWith(".tsv") 
							|| f.getName().endsWith(".TSV")) {
						return true;
					} else {
						return false;
					}
				}
				@Override
				public String getDescription() {
					return "*.tsv Tab-separated values";
				}
			});
		}
		return fc;
	}
	
	/**
	 * constructor.
	 * @param keys
	 */
	public AttributeMappingDialog(List<String> keys) {
		super(Cytoscape.getDesktop(), "Secondary Attribute Map Dialog", true);
		this.keys = keys;

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
		getContentPane().add(createMainPanel(), BorderLayout.CENTER);
		
		pack();
		center();
		setResizable(false);
		setVisible(true);
	}

	/**
	 * centers dialog over screen.
	 */
	private void center() {
                int x_off = Cytoscape.getDesktop().getX();
                int y_off = Cytoscape.getDesktop().getY();
		int wo = Cytoscape.getDesktop().getWidth();
		int ho = Cytoscape.getDesktop().getHeight();
		int wi = getWidth();
		int hi = getHeight();
		setLocation(((wo - wi) / 2) + x_off, ((ho - hi) / 2) + y_off);
	}

	/**
	 * creates a button panel.
	 * @return
	 */
	private Component createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		okButton = createButton("OK",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MapSecAttTask t = new MapSecAttTask((String)keybox.getSelectedItem(), fileTF.getText(), nameTF.getText());
				TaskManager.executeTask(t, OndexTaskConfig.getInstance());
				dispose();
			}
		});
		okButton.setEnabled(false);
		panel.add(okButton);
		
		panel.add(createButton("Cancel",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}));
		
		return panel;
	}

	/**
	 * creates the main panel.
	 * @return
	 */
	private Component createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		JPanel keypanel = new JPanel();
		keypanel.setLayout(new BorderLayout());
		keypanel.setBorder(BorderFactory.createTitledBorder("Primary key:"));
		
		keybox = new JComboBox(keys.toArray());
		keybox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (keybox.getSelectedItem() != null) {
					keySelected = true;
					checkUnlockOK();
				}
			}
		});
		keypanel.add(keybox, BorderLayout.CENTER);
		panel.add(keypanel);
		
		JPanel namepanel = new JPanel();
		namepanel.setLayout(new BorderLayout());
		namepanel.setBorder(BorderFactory.createTitledBorder("Attribute name:"));
		nameTF = new JTextField("my_attribute");
		nameTF.setColumns(30);
		namepanel.add(nameTF, BorderLayout.CENTER);
		panel.add(namepanel);
		
		JPanel filepanel = new JPanel();
		filepanel.setLayout(new BorderLayout());
		filepanel.setBorder(BorderFactory.createTitledBorder("Mapping file:"));
		
		fileTF = new JTextField();
		filepanel.add(fileTF, BorderLayout.CENTER);
		
		final JDialog superthis = this;
		filepanel.add(createButton("Browse...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int opt = getFileChooser().showOpenDialog(superthis);
				if (opt == JFileChooser.APPROVE_OPTION) {
					fileTF.setText(getFileChooser().getSelectedFile().getAbsolutePath());
					if (fileTF.getText() != null && !fileTF.getText().trim().equals("")) {
						fileSelected = true;
						checkUnlockOK();
					}
				}
			}
		}), BorderLayout.EAST);
		
		panel.add(filepanel);
		
		return panel;
	}
	
	/**
	 * check whether ok button can be unlocked
	 */
	private void checkUnlockOK() {
		if (fileSelected && keySelected) {
			okButton.setEnabled(true);
		}
	}

	/**
	 * create a button with given title and action listener.
	 * @param title
	 * @param l
	 * @return
	 */
	private JButton createButton(String title, ActionListener l) {
		JButton b = new JButton(title);
		b.addActionListener(l);
		return b;
	}

}
