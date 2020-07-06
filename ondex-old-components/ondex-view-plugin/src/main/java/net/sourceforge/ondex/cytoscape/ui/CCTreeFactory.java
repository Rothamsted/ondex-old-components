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

import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraph;

/**
 * creates a concept class tree.
 * @author jweile
 *
 */
public class CCTreeFactory {
	
	/**
	 * root concept class (THING)
	 */
	private ConceptClass rootCC;
	
	/**
	 * constructor on OndexGraph
	 * @param og
	 * @throws Exception
	 */
	public CCTreeFactory(ONDEXGraph og) throws Exception {
		rootCC = og.getMetaData().getConceptClass("Thing");
		if (rootCC == null) {
			rootCC = og.getMetaData().createConceptClass("Thing", "Thing", "Root concept class", null);
		}
		for (ConceptClass cc: og.getMetaData().getConceptClasses()) {
			makeNode(cc);
		}
	}
	
	/**
	 * maps concept classes to nodes
	 */
	private HashMap<String, DefaultMutableTreeNode> cc2node = new HashMap<String, DefaultMutableTreeNode>();
	
	/**
	 * creates a node from a concept class
	 * @param cc
	 * @return
	 */
	private DefaultMutableTreeNode makeNode(ConceptClass cc) {
		DefaultMutableTreeNode node = cc2node.get(cc.getId());
		if (node == null) {
			node = new DefaultMutableTreeNode(cc);

			if (!cc.equals(rootCC)) {
				if (cc.getSpecialisationOf() == null) {
					cc.setSpecialisationOf(rootCC);
				}
				DefaultMutableTreeNode parent = makeNode(cc.getSpecialisationOf());
				parent.add(node);
			}
			
			cc2node.put(cc.getId(), node);
		}
		return node;
	}
	
	/**
	 * returns the root node
	 * @return
	 */
	private DefaultMutableTreeNode getRoot() {
		if (cc2node.size() == 0) {
			return null;
		} else {
			DefaultMutableTreeNode randomNode = cc2node.values().iterator().next();
			while (randomNode.getParent() != null) {
				randomNode = (DefaultMutableTreeNode) randomNode.getParent();
			}
			return randomNode;
		}
	}
	
	/**
	 * creates the tree
	 * @return
	 */
	public JTree makeTree() {
		DefaultMutableTreeNode root = getRoot();
		JTree tree = new JTree(root);
		
		DefaultTreeCellRenderer r = ((DefaultTreeCellRenderer)tree.getCellRenderer());
        Icon icon = r.getLeafIcon();
        r.setOpenIcon(icon);
        r.setClosedIcon(icon);
		
		Enumeration<?> e = root.breadthFirstEnumeration();
        while(e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)e.nextElement();
            if(node.isLeaf()) continue;
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
		
		return tree;
	}
	
}
