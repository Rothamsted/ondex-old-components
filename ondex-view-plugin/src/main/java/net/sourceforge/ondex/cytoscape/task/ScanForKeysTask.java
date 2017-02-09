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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * scans the attribute names of the graph for suitable keys.
 * @author jweile
 *
 */
public class ScanForKeysTask implements Task {

	/**
	 * monitor.
	 */
	private TaskMonitor monitor;
	
	/**
	 * list of keys that were found.
	 */
	private List<String> keys = null;
	
	/**
	 * underlying network.
	 */
	private CyNetwork network;
	
	//####CONSTRUCTOR####
	
	/**
	 * constructor with given network.
	 * @param network
	 */
	public ScanForKeysTask(CyNetwork network) {
		this.network = network;
	}
	
	/**
	 * constructor using /current/ network
	 */
	public ScanForKeysTask() {
		this.network = Cytoscape.getCurrentNetwork();
	}
	
	//####METHODS####
	/**
	 * task name
	 */
	public String getTitle() {
		return "Scanning for potential key attributes";
	}

	/**
	 * not implemented.
	 */
	public void halt() {
		
	}

	/**
	 * starts the task.
	 */
	public void run() {
		monitor.setPercentCompleted(0);
		monitor.setStatus("scanning...");
		
		CyAttributes nodeAtts = Cytoscape.getNodeAttributes();
		String[] ans = nodeAtts.getAttributeNames();
		
		HashMap<String, HashMap<String,String>> anMap = new HashMap<String, HashMap<String,String>>();
		for (String an : ans) {
			anMap.put(an, new HashMap<String, String>());
		}
		
		Map<String, Integer> counts = LazyMap.decorate(new HashMap<String, Integer>(), new Factory<Integer>() {
			@Override
			public Integer create() {
				return Integer.valueOf(0);
			}});

		ArrayList<String> clashedANs= new ArrayList<String>();
		ArrayList<String> remainingANs = new ArrayList<String>();
		for (String an : ans) {
			remainingANs.add(an);
		}
		
		int[] index = network.getNodeIndicesArray();
		for (int i = 0; i < index.length; i++) {
			String nodeId = network.getNode(index[i]).getIdentifier();
			clashedANs.clear();
			for (String an : remainingANs) {
				Object val = nodeAtts.getAttribute(nodeId, an);
				if (val != null && val instanceof String) {
					String sval = (String) val;
					
					int oldcount = counts.get(an);
					counts.put(an, oldcount + 1);
					
					HashMap<String,String> map = anMap.get(an);
					if (map.get(sval) == null) {
						map.put(sval, nodeId);
					} else {//an has clashes!
						clashedANs.add(an);
					}
				}
			}
			remainingANs.removeAll(clashedANs);
			if (remainingANs.size() == 0) {
				break;
			}
			monitor.setPercentCompleted((i * 100) / index.length);
		}
		
		
		for (String an : ans) {
			if (counts.get(an) == 0) {
				remainingANs.remove(an);
			}
		}
		
		monitor.setPercentCompleted(100);
		monitor.setStatus("done");
		
		keys = remainingANs;
	}
	
	/**
	 * sends querying thread to sleep until answer available
	 * @return
	 */
	public List<String> waitForKeys() {
		while (keys == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return null;
			}
		}
		return keys;
	}

	/**
	 * set monitor.
	 */
	public void setTaskMonitor(TaskMonitor m)
			throws IllegalThreadStateException {
		this.monitor = m;
	}

}
