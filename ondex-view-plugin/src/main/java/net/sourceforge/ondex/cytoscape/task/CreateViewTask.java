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
package net.sourceforge.ondex.cytoscape.task;

import giny.model.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.cytoscape.ui.OndexViewStyle;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;

/**
 * Creates a view on a subnetwork according to the query parameters.
 * 
 * @author jweile
 * 
 */
public class CreateViewTask implements Task {

	/**
	 * the whitespace-separated identifiers for nodes from the selected
	 * namespace
	 */
	private String idString;

	/**
	 * the identifiers' namespaces (attribute names)
	 */
	private String namespace;

	/**
	 * size of neighbourhood to extract
	 */
	private int neighbourhoodSize;

        /**
         * Linking back to the dialog in order to report success.
         */
        private Runnable disposeAction;

	/**
	 * Creates a view on a subnetwork according to the query parameters.
	 * 
	 * @param ids
	 *            the whitespace-separated identifiers for nodes from the
	 *            selected namespace
	 * @param namespace
	 *            the identifiers' namespaces (attribute names)
	 * @param neighbourhoodSize
	 *            size of neighbourhood to extract
	 */
	public CreateViewTask(String ids, String namespace, int neighbourhoodSize, Runnable disposeAction) {
		this.idString = ids;
		this.namespace = namespace;
		this.neighbourhoodSize = neighbourhoodSize;
                this.disposeAction = disposeAction;
	}

	@Override
	public void run() {
			int[] nodes = extractNodes();

            if (nodes.length > 0) {
                int[] edges = Cytoscape.getCurrentNetwork()
                                .getConnectingEdgeIndicesArray(nodes);

                monitor.setPercentCompleted(90);

                CyNetwork parent = Cytoscape.getCurrentNetwork();

                CyNetwork net = Cytoscape.createNetwork(nodes, edges, parent.getTitle()
                                + " queried", parent, false);

                CyNetworkView view = Cytoscape.createNetworkView(net);

                OndexViewStyle style = new OndexViewStyle(view);

                monitor.setPercentCompleted(100);

                disposeAction.run();
            } else {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                            "No results. Try a different query.","Error",JOptionPane.ERROR_MESSAGE);
            }
	}

	/**
	 * finds all the nodes that belong to the subgraph specified by the query.
	 * 
	 * @param ids
	 *            the whitespace-separated identifiers for nodes from the
	 *            selected namespace
	 * @param namespace
	 *            the identifiers' namespaces (attribute names)
	 * @param neighbourhoodSize
	 *            size of neighbourhood to extract
	 * @return an array of integers identifying the nodes
	 */
	private int[] extractNodes() {

		// parse id string
		List<String> ids = new ArrayList<String>();
		for (String id : idString.split(" ")) {
			if (id != null && id.length() > 0) {
				ids.add(id.toLowerCase());
			}
		}

		// index all nodes according to their value in the given namespace. O(n)
		Map<String, Integer> index = indexNodes(namespace);

		Set<Integer> open = new HashSet<Integer>();// yet to be explored
		Set<Integer> closed = new HashSet<Integer>();// already explored

		// seed open set with ids from query
		for (String id : ids) {
			int nodeId = index.get(id);
			open.add(nodeId);
		}

		// find neighbours
		for (int i = 0; i < neighbourhoodSize; i++) {

			// N = neighbourhood(O)
			Set<Integer> neighbours = neighbourhood(open);

			// C = C ∪ O
			closed.addAll(open);

			// O = N \ C
			neighbours.removeAll(closed);
			open = neighbours;
			
			monitor.setPercentCompleted(50 * (i+1) / neighbourhoodSize);
		}

		// return C ∪ O
		closed.addAll(open);
		
		// TODO: think up better copying to array
		Integer[] temp = closed.toArray(new Integer[closed.size()]);
		int[] result = new int[temp.length];
		for (int i = 0; i < result.length; i ++) {
			result[i] = temp[i]; 
		}
		return result;
	}

	/**
	 * Returns a <code>Set<Integer></code> containing the root ids of all nodes
	 * belonging to the 1-neighbourhood of <code>nodes</code>
	 * 
	 * @param nodes
	 *            the nodes of which the neighbourhood will be found
	 */
	private Set<Integer> neighbourhood(Set<Integer> nodes) {

		Set<Integer> neighbours = new HashSet<Integer>();

		for (int node : nodes) {

			// find all adjacent edges
			int[] edges = Cytoscape.getCurrentNetwork()
					.getAdjacentEdgeIndicesArray(node, true, true, true);
			
			if (edges != null) {
				for (int edge : edges) {
	
					// get edge object
					Edge e = Cytoscape.getCurrentNetwork().getEdge(edge);
					// get get node opposite from current node on given edge
					if (e.getSource().getRootGraphIndex() == node) {
						neighbours.add(e.getTarget().getRootGraphIndex());
					} else {
						neighbours.add(e.getSource().getRootGraphIndex());
					}
	
				}
			}
		}

		return neighbours;
	}

	/**
	 * Creates an index over all nodes using the specified namespace.
	 * 
	 * @param namespace
	 *            the namespace
	 * @return a hashmap that stores the node integer id belonging to each
	 *         element of the namespace
	 */
	private Map<String, Integer> indexNodes(String namespace) {
		Map<String, Integer> index = new HashMap<String, Integer>();

		for (Object o : Cytoscape.getCyNodesList()) {
			CyNode node = (CyNode) o;
			Object att = Cytoscape.getNodeAttributes().getAttribute(
					node.getIdentifier(), namespace);
			if (att instanceof String) {
				String key = ((String) att).toLowerCase();
				index.put(key, node.getRootGraphIndex());
			}
		}

		return index;
	}

	private TaskMonitor monitor;

	@Override
	public void setTaskMonitor(TaskMonitor m)
			throws IllegalThreadStateException {
		this.monitor = m;
	}

	@Override
	public String getTitle() {
		return "Creating View";
	}

	@Override
	public void halt() {
		// TODO implement me
	}
}
