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
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.cytoscape.OndexPlugin;
import net.sourceforge.ondex.cytoscape.io.IOHandler;
import net.sourceforge.ondex.cytoscape.mapping.EdgeMappingDescriptor;
import net.sourceforge.ondex.cytoscape.mapping.MalformedPathException;
import net.sourceforge.ondex.cytoscape.task.CreateGraphTask;
import net.sourceforge.ondex.cytoscape.task.OndexTaskConfig;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.util.TaskManager;

/**
 * The panel is inserted as a tab in the lefthand CytoPanel.
 * 
 *    
 * @author jweile
 *
 */
public class OndexMappingPanel extends JPanel {
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * currently used mapping file
	 */
	private File currentFile = null;
	
	/**
	 * reusable tree selection listener.
	 */
	private TreeSelectionListener tsl;
	
	private JTree currentTree;
	
	/**
	 * split pane. located in the center.
	 * reference required for inserting new trees
	 * after loading new files.
	 */
	private JSplitPane splitPane;
	
	/**
	 * list of mapping descriptor. This is the
	 * most important component of the GUI and needs to be
	 * initialized first! as everything else references this.
	 */
	private ProfileList profileList;
	
	/**
	 * constructor sets up the GUI
	 */
	public OndexMappingPanel()  {
		setupUI();
	}
	
	/**
	 * sets up the GUI
	 */
	private void setupUI() {
		setLayout(new BorderLayout());
		add(makeSplitPanel(), BorderLayout.CENTER);
		add(makeFileField(), BorderLayout.NORTH);
		add(makeButtonPanel(), BorderLayout.SOUTH);
		//sorry... hack
		splitPane.setDividerLocation(0.5);
	}
	
	/**
	 * creates the file selection field on the top.<br/>
	 * <code>|current file       |load|save    v |</code><br/>
	 * <code>                         |save as...|</code><br/>
	 * <code>                         |save      |</code><br/>
	 * @return
	 */
	private JPanel makeFileField() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("Data set"));
		
		final JTextField field = new JTextField();
		field.setEditable(false);
		panel.add(field, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2,5,5));
		buttonPanel.add(makeButton("Load...",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String filename = load();
				if (filename != null) {
					field.setText(filename);
					splitPane.setDividerLocation(0.75);
				}
			}
		}));
		
		PopupButton saveButton = new PopupButton("<html>Save &nabla;</html>");
		saveButton.addAction("Save", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newFile = save();
				if (newFile != null) {
					field.setText(newFile);
				}
			}
		});
		saveButton.addAction("Save as...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String newFile = saveAs();
				if (newFile != null) {
					field.setText(newFile);
				}
			}
		});
		
		buttonPanel.add(saveButton);
		
		panel.add(buttonPanel, BorderLayout.EAST);
		
		return panel;
	}
	
	/**
	 * opens filechooser. then loads the selected file, which is either a graph
	 * or a view (graph+mapping).
	 * @return
	 */
	private String load() {
		JFileChooser fc = IOHandler.getInstance().getFileChooser();
		int retVal = fc.showOpenDialog(Cytoscape.getDesktop());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				
				Collection<EdgeMappingDescriptor> newDescriptors = 
					IOHandler.getInstance().load(file);
				
				CCTreeFactory factory = new CCTreeFactory(OndexPlugin.getInstance().getOndexGraph());
				setTree(factory.makeTree());
				
				if (newDescriptors != null) {
					profileList.init(newDescriptors);
					currentFile = file;
				}
				
				return file.getName();
			} catch (IOException e1) {
				fail("Could not read file: "+file.getName());
			} catch (ParseException e1) {
				fail("Selected file "+file.getName()+
						" is no valid mapping file.\n"+e1.getMessage());
			} catch (MalformedPathException e1) {
				fail("Selected file "+file.getName()+
						" is incompatible with the loaded data set.\n"+e1.getMessage());
			} catch (Exception e1) {
				fail("An error occured while trying to read the file "+file.getName()+
						"\n\n"+e1.getMessage());
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * saves the current graph+mapping to a file of the users choice.
	 * opens fileChooser dialog.
	 */
	private String saveAs() {
		JFileChooser fc = IOHandler.getInstance().getFileChooser();
		int retVal = fc.showSaveDialog(Cytoscape.getDesktop());
		if (retVal == JFileChooser.APPROVE_OPTION) {
			currentFile = fc.getSelectedFile();
			return save();
		}
		return null;
	}
	
	/**
	 * saves the current graph+mapping to the current file.
	 */
	private String save() {
		if (currentFile == null) {
			saveAs();
		} else {
			try {
				File file = currentFile;
				if (!file.getName().endsWith(".ov1")) {
					file = new File(file.getAbsolutePath()+".ov1");
				}
				IOHandler.getInstance().save(file, profileList.getDescriptors());
				currentFile = file;
				return currentFile.getName();
			} catch (IOException e1) {
				fail("Could not write to file: "+currentFile.getName());
			} catch (Exception e) {
				fail("An error occurred while trying to write to the file "+currentFile.getName()+
						"\n\n"+e.getMessage());
			}
		}
		return null;
	}
	

	/**
	 * creates the split panel in the centre.
	 * @return
	 */
	private JSplitPane makeSplitPanel() {
		JPanel dummy = new JPanel();
		dummy.setBackground(Color.white);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dummy, makeProfilePanel());
		return splitPane;
	}

	/**
	 * creates the profile panel. must be the first component to be initialized!
	 * otherwise nullpointer exceptions!!!
	 * @return
	 */
	private Component makeProfilePanel() {
		profileList = new ProfileList();
		return new JScrollPane(profileList);
	}

	/**
	 * sets a new tree on the left side of the split pane.
	 * @param tree
	 */
	private void setTree(JTree tree) {
		splitPane.setLeftComponent(new JScrollPane(tree));
		tree.addTreeSelectionListener(getTreeListener());
		currentTree = tree;
		repaint();
	}
	
	/**
	 * singleton treeselection listener for newly loaded trees.
	 * @return
	 */
	private TreeSelectionListener getTreeListener() {
		if (tsl == null) {
			tsl = new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					JTree tree = (JTree)e.getSource();
					TreePath path = tree.getSelectionPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					ConceptClass cc = (ConceptClass) node.getUserObject();
					profileList.setFocus(cc);
				}
			};
		}
		return tsl;
	}
	
	private ConceptClass getCurrentTreeSelection() {
		TreePath p = currentTree.getSelectionPath();
		if (p == null) {
			return null;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) p.getLastPathComponent();
		return (ConceptClass) node.getUserObject();
	}
	
	/**
	 * creates the button panel at the bottom
	 * |Customize...|Apply|
	 * @return
	 */
	private JPanel makeButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1,2,5,5));
		buttonPanel.add(makeButton("Customize...", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MappingDialog dialog = new MappingDialog(profileList.getDescriptors());
				dialog.setVisible(true);
				if (dialog.approveOption()) {
					profileList.init(dialog.getDescriptors());
				}
			}
		}));
		buttonPanel.add(makeButton("Apply", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task task = new CreateGraphTask(profileList.getSelection()); 
				TaskManager.executeTask(task, OndexTaskConfig.getInstance());
			}
		}));
		return buttonPanel;
	}
	

	/**
	 * creates a new button with title and actionlistener
	 * @param title
	 * @param l
	 * @return
	 */
	private JButton makeButton(String title, ActionListener l) {
		JButton b = new JButton(title);
		b.addActionListener(l);
		return b;
	}
	
	/**
	 * show error message
	 * @param message
	 */
	private void fail(String message) {
		JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
				message, 
				"Error", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.add(new JPanel(), BorderLayout.CENTER);
		f.add(new OndexMappingPanel(), BorderLayout.WEST);
		f.pack();
		f.setVisible(true);
	}
	
	/**
	 * Panel holding a list of checkboxes that represent the different mapping descriptors.
	 * 
	 * @author jweile
	 *
	 */
	private class ProfileList extends JPanel {
		
		/**
		 * serial id.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * descriptor lists for ConceptClass ids.
		 */
		private HashMap<String, ArrayList<EdgeMappingDescriptor>> cc2descriptorList;
		
		/**
		 * descriptor for descriptorID.
		 */
		private HashMap<String, EdgeMappingDescriptor> id2descriptor;
		
		/**
		 * constructor.
		 * inits the hashmaps
		 * sets contents up.
		 */
		public ProfileList() {
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			cc2descriptorList = new HashMap<String, ArrayList<EdgeMappingDescriptor>>();
			id2descriptor = new HashMap<String, EdgeMappingDescriptor>();

			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(Color.white);
		}
		
		/**
		 * returns the list of currently selected descriptors.
		 * @return a collection of EdgeMappingDescriptors representing the currently selected entries.
		 */
		public Collection<EdgeMappingDescriptor> getSelection() {
			Collection<EdgeMappingDescriptor> activeList = new ArrayList<EdgeMappingDescriptor>();
			for (Component c : getComponents()) {
				if (c instanceof JCheckBox) {
					JCheckBox box = (JCheckBox) c;
					if (box.isSelected()) {
						String id = box.getName();
						EdgeMappingDescriptor descriptor = id2descriptor.get(id);
						activeList.add(descriptor);
					}
				}
			}
			return activeList;
		}

		/**
		 * Returns the list of all known descriptors.
		 */
		public Collection<EdgeMappingDescriptor> getDescriptors() {
			return id2descriptor.values();
		}

		/**
		 * selects a concept class focus creates the list of
		 * descriptor checkboxes accordingly.
		 * @param cc filter statement. value null results in empty list.
		 */
		public void setFocus(ConceptClass cc) {
			removeAll();
			if (cc != null) {
				ArrayList<EdgeMappingDescriptor> ds = cc2descriptorList.get(cc.getId());
				if (ds != null) {
					for (EdgeMappingDescriptor d : ds) {
						JCheckBox box = new JCheckBox(d.getName());
						box.setName(d.getId());
						box.setBackground(Color.white);
						add(box);
					}
				}
			}
			revalidate();
			getParent().repaint();
		}
		
		/**
		 * initializes the hashtables with a new set of descriptors.
		 * @param descriptors
		 */
		public void init(Collection<EdgeMappingDescriptor> descriptors) {
			id2descriptor.clear();
			cc2descriptorList.clear();
			for (EdgeMappingDescriptor d: descriptors) {
				String id = d.getId();
				id2descriptor.put(id,d);
				
				String ccId = d.getConceptClasses()[0];
				ArrayList<EdgeMappingDescriptor> list = cc2descriptorList.get(ccId);
				if (list == null) {
					list = new ArrayList<EdgeMappingDescriptor>();
					cc2descriptorList.put(ccId, list);
				}
				list.add(d);
			}
			setFocus(getCurrentTreeSelection());
		}

	}
	
}
