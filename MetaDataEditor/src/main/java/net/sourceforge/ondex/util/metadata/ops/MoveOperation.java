package net.sourceforge.ondex.util.metadata.ops;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.util.metadata.elements.METree;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;
import net.sourceforge.ondex.util.metadata.model.OldNode;

public class MoveOperation<M extends MetaData> extends Operation<M> {

	private METree tree;
	
	private DefaultMutableTreeNode newParent;
	
	private OldNode[] nodes;
	
//	private DefaultMutableTreeNode[] newNodes;
	
	private int newIndex;
	
	public MoveOperation(METree tree, OldNode[] nodes, 
								DefaultMutableTreeNode newParent, int newIndex) {
		super(null);
		this.tree = tree;
		this.newParent = newParent;
		this.newIndex = newIndex;
		this.nodes = nodes;
//		this.newNodes = newNodes;
	}

	@Override
	public void perform() {
            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
            int index = newIndex;
            for (OldNode oldNode : nodes) {
                model.removeNodeFromParent(oldNode.getNode());
                model.insertNodeInto(oldNode.getNode(), newParent, index++);
                adaptMetaDataHierarchy(oldNode.getNode(), newParent);
            }
	}

	@Override
	public void revert() {
		DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
		for (OldNode oldNode : nodes) {
			model.removeNodeFromParent(oldNode.getNode());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) oldNode.getOldParent();
			model.insertNodeInto(oldNode.getNode(), parent, oldNode.getOldIndex());
			adaptMetaDataHierarchy(oldNode.getNode(), parent);
		}
	}
	
	private void adaptMetaDataHierarchy(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
		MetaData md = (MetaData) node.getUserObject();
		switch (MetaDataType.fromClass(md)) {
		case CONCEPT_CLASS:
			ConceptClass cc_curr = (ConceptClass) md;
			ConceptClass cc_parent = (ConceptClass) parent.getUserObject();
			cc_curr.setSpecialisationOf(cc_parent);
//			System.out.println("new parent: "+cc_curr.getSpecialisationOf());
			break;
		case RELATION_TYPE: 
			RelationType rt_curr = (RelationType) md;
			RelationType rt_parent = (RelationType) parent.getUserObject();
			rt_curr.setSpecialisationOf(rt_parent);
			break;
		case ATTRIBUTE_NAME:
			AttributeName an_curr = (AttributeName) md;
			AttributeName an_parent = (AttributeName) parent.getUserObject();
			an_curr.setSpecialisationOf(an_parent);
			break;
		}
	}
	
	
	
}
