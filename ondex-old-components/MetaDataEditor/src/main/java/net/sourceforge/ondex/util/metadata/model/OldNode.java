package net.sourceforge.ondex.util.metadata.model;

import javax.swing.tree.DefaultMutableTreeNode;

public class OldNode {
		
		private DefaultMutableTreeNode node;
		private int oldIndex;
		private DefaultMutableTreeNode parent;
		
		public OldNode(DefaultMutableTreeNode n) {
			node = n;
			parent = (DefaultMutableTreeNode)n.getParent();
			oldIndex = parent.getIndex(node);
		}
		
		public DefaultMutableTreeNode getNode() {
			return node;
		}
		
		public int getOldIndex() {
			return oldIndex;
		}
		
		public DefaultMutableTreeNode getOldParent() {
			return parent;
		}
	}
