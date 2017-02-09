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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JOptionPane;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * Task for mapping secondary attributes to the graph
 * @author jweile
 *
 */
public class MapSecAttTask implements Task {
	
	/**
	 * monitor
	 */
	private TaskMonitor monitor;
	
	/**
	 * key attribute name, filename and new attribute name
	 */
	private String keyAN, filename, newAN;
	
	//####CONSTRUCTOR####
	/**
	 * constructor.
	 * @param key
	 * @param filename
	 * @param newAN
	 */
	public MapSecAttTask(String key, String filename, String newAN) {
		this.keyAN = key;
		this.filename = filename;
		this.newAN = newAN;
	}

	//####METHODS####
	
	/**
	 * gets the title.
	 */
	public String getTitle() {
		return "Mapping secondary attributes";
	}

	/**
	 * not implemented
	 */
	public void halt() {

	}

	/**
	 * starts the task.
	 */
	public void run() {
		HashMap<String, String> key2id = new HashMap<String, String>();
		
		CyNetwork network = Cytoscape.getCurrentNetwork();
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		
		monitor.setPercentCompleted(0);
		monitor.setStatus("indexing keys...");
		
		int[] index = network.getNodeIndicesArray();
		for (int i = 0; i < index.length; i++) {
			String nodeId = network.getNode(index[i]).getIdentifier();
			String key = (String)nodeAtts.getAttribute(nodeId, keyAN);
			key2id.put(key, nodeId);
			monitor.setPercentCompleted((i * 50) / index.length);
		}
		
		monitor.setStatus("writing attributes...");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = br.readLine();
			if (newAN == null || newAN.trim().equals("")) {
				newAN = line;
			}
			while ((line = br.readLine()) != null) {
				String[] cols = line.split("\t");
				if (cols.length == 3) {
					String key = cols[0];
					String val = cols[2];
					String id = key2id.get(key);
					if (id != null) {
						nodeAtts.setAttribute(id, newAN, val);
					} else {
						System.out.println("unmapped: "+key);
					}
				}
			}
			
			Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
			
			monitor.setStatus("done");
			monitor.setPercentCompleted(100);
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(Cytoscape.getDesktop(), 
					"Cannot read file: "+filename, 
					"Error", 
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * set task monitor.
	 */
	public void setTaskMonitor(TaskMonitor m)
			throws IllegalThreadStateException {
		monitor = m;
	}

}
