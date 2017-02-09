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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.sourceforge.ondex.cytoscape.mapping.EdgeMappingDescriptor;
import net.sourceforge.ondex.cytoscape.mapping.MalformedPathException;
import cytoscape.Cytoscape;

/**
 * dialog for creating mappings
 * 
 * @author jweile
 *
 */
public class MappingDialog extends JDialog {

	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * collection of current mapping descriptors
	 */
	private Collection<EdgeMappingDescriptor> descriptors;
		
	/**
	 * listpanel
	 */
	private ListPanel listPanel;
	
	/**
	 * whether no problems exist with the current descriptors.
	 */
	private boolean ok = false;
	
	/**
	 * constructor.
	 */
	public MappingDialog(Collection<EdgeMappingDescriptor> descriptors) {
		super(Cytoscape.getDesktop(), true);
		setTitle("Customize mappings");
		
		if (descriptors != null) {
			this.descriptors = descriptors;
		} else {
			this.descriptors = new ArrayList<EdgeMappingDescriptor>();
		}
		
		listPanel = new ListPanel();
		
		setLayout(new BorderLayout());
		add(makeMainPanel(), BorderLayout.CENTER);
		add(makeButtonPanel(), BorderLayout.SOUTH);
		pack();
		setSize(getWidth(),getHeight()+200);
		center();
	}
	
	/**
	 * center dialog over window.
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
	 * creates the main panel
	 * @return
	 */
	private Component makeMainPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Mapping definitions:"));
		panel.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(listPanel);
		scrollPane.setBackground(Color.white);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * creates the button panel (cancel,ok) at the bottom of the frame.
	 * @return
	 */
	private Component makeButtonPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		panel.add(makeButton("Cancel",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}));
		panel.add(makeButton("OK",new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		}));
		return panel;
	}
	
	/**
	 * activated by OK button.
	 */
	private void ok() {
		try {
			Collection<EdgeMappingDescriptor> newDescriptors = listPanel.extractDescriptors();
			descriptors = newDescriptors;
			ok = true;
			dispose();
		} catch (MalformedPathException e) {
			fail("Please resolve existing problems first!");
		}
	}
	
	/**
	 * returns whether descriptors are ok.
	 * @return
	 */
	public boolean approveOption() {
		return ok;
	}
	
	/**
	 * safely destroys the dialog window
	 */
	public void dispose() {
		listPanel.shutDown();
		super.dispose();
	}
	
	/**
	 * show error message
	 * @param message
	 */
	private void fail(String message) {
		JOptionPane.showMessageDialog(this, 
				message, 
				"Error", 
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * create button with given title and listener
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
	 * get current descriptors
	 * @return
	 */
	public Collection<EdgeMappingDescriptor> getDescriptors() {
		return descriptors;
	}
	
	/**
	 * panel containing list of descriptor fields.
	 * @author jweile
	 *
	 */
	private class ListPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * listeners for adding and removing elements.
		 */
		private ActionListener plusListener, minusListener, clashListener;
		
		/**
		 * list of items
		 */
		private ArrayList<ListItem> items = new ArrayList<ListItem>();
		
		/**
		 * constructor.
		 */
		public ListPanel() {
			initListeners();
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			setBackground(Color.white);
			
			//populate list
			for (EdgeMappingDescriptor d : descriptors) {
				ListItem item = new ListItem(d.getName(), d.getPathString(), plusListener, minusListener, clashListener);
				item.check();
				items.add(item);
			}
			if (items.size() == 0) {
				items.add(new ListItem("Name","Path", plusListener, minusListener, clashListener));
			}
			
			refreshContents();
		}

		/**
		 * refreshes the list's contents
		 */
		private void refreshContents() {
			removeAll();
			for (ListItem item : items) {
				add(item);
			}
			add(Box.createGlue());
			revalidate();
		}

		/**
		 * initializes the listeners for adding and removing list items.
		 */
		private void initListeners() {
			plusListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					items.add(new ListItem("Name","Path", plusListener, minusListener, clashListener));
					refreshContents();
				}
			};
			
			minusListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton minusButton = (JButton) e.getSource();
					ListItem item = (ListItem) minusButton.getParent();
					items.remove(item);
					item.destroy();
					refreshContents();
				}
			};
			
			clashListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					HashMap<String, ListItem> control = new HashMap<String, ListItem>();
					ListItem clashFrom = null, 
							 clashTo = null;
					for (ListItem item : items) {
						String curr_id = item.getId();
						clashTo = control.get(curr_id);
						if (clashTo != null) {
							clashFrom = item;
							break;
						} else {
							control.put(curr_id, item);
						}
					}
					
					if (clashFrom != null) {
						clashFrom.markClash(true);
						clashTo.markClash(true);
					} else {
						for (ListItem item : items) {
							item.markClash(false);
						}
					}
				}
			};
		}
		
		/**
		 * extracts all mapping descriptors from the list items.
		 * @return the list of mapping descriptors.
		 * @throws MalformedPathException if one of them isn't ready.
		 */
		private Collection<EdgeMappingDescriptor> extractDescriptors() throws MalformedPathException {
			ArrayList<EdgeMappingDescriptor> newDescriptors 
				= new ArrayList<EdgeMappingDescriptor>();
			for (ListItem item : items) {
				if (item.allOK()) {
					newDescriptors.add(item.getDescriptor());
				} else {
					throw new MalformedPathException();
				}
			}
			return newDescriptors;
		}
		
		/**
		 * shuts all listener threads on the list items down.
		 */
		public void shutDown() {
			for (ListItem item : items) {
				item.destroy();
			}
		}
		
	}
	
	/**
	 * list item with two textboxes for name and path,
	 * a delaytrigger for automatic syntax checking,
	 * and two buttons for adding and removing list items.
	 * @author jweile
	 *
	 */
	private class ListItem extends JPanel {
		/**
		 * serial id.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * textfields for name and path
		 */
		private JTextField name, path;
		
		/**
		 * the represented descriptor.
		 */
		private EdgeMappingDescriptor descriptor;
		
		/**
		 * current item status.
		 */
		private ItemStatus pathStatus = ItemStatus.UNCHECKED,
		                   nameStatus = ItemStatus.UNCHECKED;
		
		/**
		 * delay trigger for syntax checking. fires 700ms after typing stops.
		 */
		private DelayTrigger pathTrigger, nameTrigger;
		
		/**
		 * constructor.
		 * @param nameS
		 * @param pathS
		 * @param plusListener
		 * @param minusListener
		 */
		public ListItem(String nameS, String pathS, 
				ActionListener plusListener,
				ActionListener minusListener,
				ActionListener clashListener) {
			this.name = new JTextField(nameS,8);
			this.path = new JTextField(pathS,20);
			
			pathTrigger = new DelayTrigger();
			path.addKeyListener(pathTrigger);
			pathTrigger.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					check();
				}
			});
			pathTrigger.start();
			
			nameTrigger = new DelayTrigger();
			name.addKeyListener(nameTrigger);
			path.addKeyListener(nameTrigger);
			nameTrigger.addActionListener(clashListener);
			nameTrigger.start();
			
			setBackground(Color.white);
			setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			add(name);
			add(path);
			JButton plusButton = makeButton("+",plusListener);
			plusButton.setMaximumSize(new Dimension(50, 20));
			add(plusButton);
			JButton minusButton = makeButton("-",minusListener);
			minusButton.setMaximumSize(new Dimension(50, 20));
			add(minusButton);
			
			setMaximumSize(new Dimension(500,40));
		}
		
		/**
		 * returns whether the descriptor is valid
		 * @return
		 */
		public boolean allOK() {
			return nameStatus == ItemStatus.OK && pathStatus == ItemStatus.OK;
		}

		/**
		 * colors the textfield according to error state
		 * @param clash whether or not there is a name clash
		 */
		public void markClash(boolean clash) {
			name.setBackground(clash ? Color.red : Color.white);
			name.setToolTipText(clash ? "Name clash for this Concept class!" : null);
			nameStatus = clash ? ItemStatus.BROKEN : ItemStatus.OK;
		}

		/**
		 * gets id of path
		 * @return
		 */
		public String getId() {
			StringBuilder b = new StringBuilder("");
			if (path.getText() != null && !path.getText().equals("")) {
				b.append(path.getText().split(" ")[0]+"#");
			}
			if (name.getText() != null && !name.getText().equals("")) {
				b.append(name.getText());
			}
			return b.toString();
		}

		/**
		 * checks whether the current path statement is syntactically correct.
		 * If so it updates the descriptor and sets the status accordingly.
		 * If not it sets the status to broken.
		 */
		public void check() {
			if (path.getText() == null || path.getText().equals("")) {
				return;
			}
			EdgeMappingDescriptor dummy = 
				new EdgeMappingDescriptor(name.getText(), path.getText());
			String message = null;
			try {
				dummy.validate();
			} catch (MalformedPathException e) {
				message = e.getMessage();
			}
			if (message == null) {
				//all good
				path.setBackground(Color.green);
				path.setToolTipText(null);
				pathStatus = ItemStatus.OK;
				descriptor = dummy;
			} else {
				path.setBackground(Color.red);
				path.setToolTipText(message);
				pathStatus = ItemStatus.BROKEN;
			}
		}
				
		/**
		 * gets the contained descriptor
		 * @return
		 */
		public EdgeMappingDescriptor getDescriptor() {
			return descriptor;
		}
		
		/**
		 * destroys the field and safely shuts down daemons
		 */
		public void destroy() {
			pathTrigger.terminate();
			nameTrigger.terminate();
		}
		
	}
	
	/**
	 * helper enum for tracking error states.
	 * @author jweile
	 *
	 */
	private enum ItemStatus {
		BROKEN, UNCHECKED, OK;
	}
	
}
