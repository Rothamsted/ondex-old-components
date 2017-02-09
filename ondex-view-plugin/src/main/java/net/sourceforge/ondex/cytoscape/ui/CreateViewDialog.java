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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import net.sourceforge.ondex.cytoscape.task.CreateViewTask;
import net.sourceforge.ondex.cytoscape.task.OndexTaskConfig;
import net.sourceforge.ondex.cytoscape.task.ScanForKeysTask;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.util.TaskManager;

/**
 * dialog for creating new views on background graphs
 * @author jweile
 *
 */
@SuppressWarnings("serial")
public class CreateViewDialog extends JDialog {

	/**
	 * textarea for queries
	 */
	private JTextArea queryIDs;

	/**
	 * dropdown box for query namespace
	 */
	private JComboBox namespace;

	/**
	 * checkbox for neighbourhood.
	 */
	private JCheckBox useNeighbourhood;

	/**
	 * spinner for neighbourhood size
	 */
	private JSpinner neighbourhood;

	/**
	 * group for radio buttons all or query
	 */
	private ButtonGroup allOrQuery;

	private static final String QUERY = "Query";
	private static final String ALL = "All";

//        /**
//         * Used by query task to report success;
//         */
//        private boolean success = false;

	/**
	 * constructor
	 */
	public CreateViewDialog() {

		super(Cytoscape.getDesktop(), "Create View", true);

		makeUI();
		pack();
		center();

		setVisible(true);
	}

	/**
	 * fill out user interface
	 */
	private void makeUI() {
		this.setLayout(new BorderLayout());
		this.add(makeMainPanel(), BorderLayout.CENTER);
		this.add(makeButtonPanel(), BorderLayout.SOUTH);
	}

	/**
	 * creates the button panel
	 * @return
	 */
	private Component makeButtonPanel() {

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		final CreateViewDialog thisDialog = this;

		panel.add(makeButton("Cancel", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				thisDialog.dispose();
			}
		}));

		panel.add(makeButton("OK", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String selected = allOrQuery.getSelection().getActionCommand();
                        CyNetwork net = null;
                        if (selected.equals(QUERY)) {

                            int neighbourhoodSize = useNeighbourhood.isSelected() ?
                                            (Integer) neighbourhood.getValue()
                                            : 0;

                            String ns = (String) namespace.getSelectedItem();

                            Runnable disposeAction = new Runnable() {
                                @Override
                                public void run() {
                                    thisDialog.dispose();
                                    System.out.println("dispose");
                                }
                            };

                            Task task = new CreateViewTask(queryIDs.getText(), ns,
                                            neighbourhoodSize,disposeAction);
                            TaskManager.executeTask(task, OndexTaskConfig.getInstance());

//                            //success flag is set through reference given to the task
//                            if (success) {
//                                thisDialog.dispose();
//                            } else {
//                                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
//                                        "No results. Try a different query.","Error",JOptionPane.ERROR_MESSAGE);
//                            }
                        } else {

                            net = Cytoscape.getCurrentNetwork();
                            Cytoscape.createNetworkView(net);
                            thisDialog.dispose();
                        }
                    }
		}));

		return panel;
	}

	// private int[] extractNodes(int neighbourhoodSize) {
	// IntSet nodes = new IntAVLTreeSet();
	//		
	// String query = queryIDs.getText();
	// //FIXME implement better parsing
	// List<String> ids = new ArrayList<String>();
	// for (String id : query.split(" ")) {
	// if (id != null && id.length() > 0) {
	// ids.add(id.toLowerCase());
	// }
	// }
	//		
	// Object2IntOpenHashMap<String> index =
	// indexNodes((String)namespace.getSelectedItem());
	// for (String id : ids) {
	// int nodeId = index.getInt(id);
	// nodes.add(nodeId);
	// }
	//		
	// //find neighbours
	// for (int i = 0; i < neighbourhoodSize; i++) {
	// IntArrayList neighbours = new IntArrayList();
	// for (int node : nodes) {
	// int[] edges =
	// Cytoscape.getCurrentNetwork().getAdjacentEdgeIndicesArray(node, true,
	// true, true);
	// for (int edge: edges) {
	// Edge e = Cytoscape.getCurrentNetwork().getEdge(edge);
	// if (e.getSource().getRootGraphIndex() == node) {
	// neighbours.add(e.getTarget().getRootGraphIndex());
	// } else {
	// neighbours.add(e.getSource().getRootGraphIndex());
	// }
	// }
	// }
	// nodes.addAll(neighbours);
	// }
	//		
	// return nodes.toIntArray();
	// }
	//	
	// private Object2IntOpenHashMap<String> indexNodes(String namespace) {
	// Object2IntOpenHashMap<String> index = new
	// Object2IntOpenHashMap<String>();
	//		
	// for (Object o : Cytoscape.getCyNodesList()) {
	// CyNode node = (CyNode) o;
	// Object att =
	// Cytoscape.getNodeAttributes().getAttribute(node.getIdentifier(),
	// namespace);
	// if (att instanceof String) {
	// String key = ((String)att).toLowerCase();
	// index.put(key, node.getRootGraphIndex());
	// }
	// }
	//		
	// return index;
	// }


	/**
	 * creates the main panel
	 */
	private Component makeMainPanel() {

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		allOrQuery = new ButtonGroup();

		panel.add(makeSelectablePanel(QUERY, new InnerPanel() {
			public void fill() {
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				// add(new JLabel("Only show the following elements:"));
				add(makeQueryIDField());
				add(makeNamespaceField());
				add(makeNeighboursField());
			}
		}));

		panel.add(makeSelectablePanel(ALL, new InnerPanel() {
			public void fill() {
				add(new JLabel("Show everything"));
			}
		}));

		return panel;
	}

	/**
	 * creates text field for queries
	 * @return
	 */
	private Component makeQueryIDField() {
		final String startMessage = "Enter IDs to query";
		queryIDs = new JTextArea(startMessage);
		queryIDs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (queryIDs.getText().equals(startMessage)) {
					queryIDs.setText("");
				}
			}
		});
		return new JScrollPane(queryIDs);
	}

	/**
	 * creates the namespace dropdown box
	 * @return
	 */
	private Component makeNamespaceField() {

		ScanForKeysTask t = new ScanForKeysTask();
		TaskManager.executeTask(t, OndexTaskConfig.getInstance());
		List<String> keys = t.waitForKeys();

		if (keys.size() <= 0) {
			// FIXME no idea... Panic!!
		}

		List<String> elements = new ArrayList<String>();
		elements.add("Namespace");
		elements.addAll(keys);

		namespace = new JComboBox(elements.toArray());

		return namespace;
	}

	/**
	 * create field for neighbourhood selection
	 * @return
	 */
	private Component makeNeighboursField() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		useNeighbourhood = new JCheckBox("and neighbourhood: ");
		panel.add(useNeighbourhood);

		neighbourhood = new JSpinner();
		neighbourhood.setModel(new SpinnerNumberModel(1, 1, 4, 1));
		neighbourhood.setEnabled(false);
		panel.add(neighbourhood);

		useNeighbourhood.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				neighbourhood.setEnabled(useNeighbourhood.isSelected());
			}
		});

		return panel;
	}

	/**
	 * creates a panel selectable by a radio button
	 * @param title
	 * @param innerPanel
	 * @return
	 */
	private Component makeSelectablePanel(String title, InnerPanel innerPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JRadioButton radio = new JRadioButton(" ");
		radio.setActionCommand(title);
		radio.addActionListener(getRadioListener());
		allOrQuery.add(radio);
		radio.setSelected(title.equals(QUERY));

		JPanel dummypanel = new JPanel(new BorderLayout());
		dummypanel.add(radio, BorderLayout.NORTH);
		dummypanel.add(new JPanel(), BorderLayout.CENTER);
		panel.add(dummypanel, BorderLayout.WEST);

		innerPanel.setBorder(BorderFactory.createTitledBorder(title));
		innerPanel.fill();
		panel.add(innerPanel);

		return panel;
	}

	/**
	 * radiobutton listener singleton
	 */
	private ActionListener radioListener;

	/**
	 * radiobutton listener singleton method
	 * @return
	 */
	private ActionListener getRadioListener() {
		if (radioListener == null) {
			radioListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JRadioButton b = (JRadioButton) e.getSource();
					if (b.isSelected()) {
						if (b.getActionCommand().equals(ALL)) {
							queryIDs.setEnabled(false);
							namespace.setEnabled(false);
							useNeighbourhood.setEnabled(false);
							neighbourhood.setEnabled(false);
						} else {
							queryIDs.setEnabled(true);
							namespace.setEnabled(true);
							useNeighbourhood.setEnabled(true);
							neighbourhood.setEnabled(useNeighbourhood
									.isSelected());
						}
					}
				}
			};
		}
		return radioListener;
	}

	/**
	 * create button with given label and listener
	 * @param string
	 * @param actionListener
	 * @return
	 */
	private Component makeButton(String string, ActionListener actionListener) {
		JButton button = new JButton(string);
		button.addActionListener(actionListener);
		return button;
	}

	/**
	 * center dialog on screen
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

//        /**
//         * Can be used by the CreateGraphTask to report a successful query processing.
//         */
//        public void reportSuccess() {
//            success = true;
//        }

	/**
	 * helper class to make selecable panels.
	 * @author jweile
	 *
	 */
	private abstract class InnerPanel extends JPanel {
		public abstract void fill();
	}
}
