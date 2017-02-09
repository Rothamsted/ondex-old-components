package net.sourceforge.ondex.util.metadata.ops;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.elements.MEList;
import net.sourceforge.ondex.util.metadata.elements.METree;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;

public class RemovalOperation<M extends MetaData> extends Operation<M> {

	private METree tree;

	private MEList<M> list;

	private DefaultMutableTreeNode parent;

	private int index;

	private DefaultMutableTreeNode node;

	public RemovalOperation(M md, METree tree, DefaultMutableTreeNode node,
			int index) {
		super(md);
		this.tree = tree;
		this.node = node;
		this.parent = (DefaultMutableTreeNode) node.getParent();
		this.index = index;
	}

	public RemovalOperation(M md, MEList<M> list, int index) {
		super(md);
		this.list = list;
		this.index = index;
	}

	@Override
	public void perform() {
		if (tree != null) {
			if (node != null) {
				((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
			}

			TreePath path = new TreePath(parent.getPath());
			tree.setSelectionPath(path);
		} else {
			index = list.convertRowIndexToModel(index);
			list.getMDListModel().remove(index);
		}

		ONDEXGraphMetaData omd = MetaDataEditor.getInstance()
				.getCurrentMetaDataWindow().getMetaData();
		MetaDataType mdt = MetaDataType.fromClass(md);
		switch (mdt) {
		case CONCEPT_CLASS:
			omd.deleteConceptClass(md.getId());
			break;
		case RELATION_TYPE:
			omd.deleteRelationType(md.getId());
			break;
		case CV:
			omd.deleteDataSource(md.getId());
			break;
		case EVIDENCE_TYPE:
			omd.deleteEvidenceType(md.getId());
			break;
		case UNIT:
			omd.deleteUnit(md.getId());
			break;
		case ATTRIBUTE_NAME:
			omd.deleteAttributeName(md.getId());
			break;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void revert() {
		ONDEXGraphMetaData omd = MetaDataEditor.getInstance()
				.getCurrentMetaDataWindow().getMetaData();

		// restore metadata from backup copy
		MetaDataType mdt = MetaDataType.fromClass(md);
		switch (mdt) {
		case CONCEPT_CLASS:
			if (omd.getConceptClass(md.getId()) == null) {
				ConceptClass cc = (ConceptClass) md;
				md = (M) omd.createConceptClass(md.getId(), md.getFullname(),
						md.getDescription(), cc.getSpecialisationOf());
			}
			break;
		case RELATION_TYPE:
			if (omd.getRelationType(md.getId()) == null) {
				RelationType rt = (RelationType) md;
				md = (M) omd.createRelationType(md.getId(), md.getFullname(),
						md.getDescription(), rt.getInverseName(),
						rt.isAntisymmetric(), rt.isReflexive(),
						rt.isSymmetric(), rt.isTransitiv(),
						rt.getSpecialisationOf());
			}
			break;
		case CV:
			if (omd.getDataSource(md.getId()) == null) {
				md = (M) omd.createDataSource(md.getId(), md.getFullname(),
						md.getDescription());
			}
			break;
		case EVIDENCE_TYPE:
			if (omd.getEvidenceType(md.getId()) == null) {
				md = (M) omd.createEvidenceType(md.getId(), md.getFullname(),
						md.getDescription());
			}
			break;
		case UNIT:
			if (omd.getUnit(md.getId()) == null) {
				md = (M) omd.createEvidenceType(md.getId(), md.getFullname(),
						md.getDescription());
			}
			break;
		case ATTRIBUTE_NAME:
			if (omd.getAttributeName(md.getId()) == null) {
				AttributeName an = (AttributeName) md;
				md = (M) omd.createAttributeName(md.getId(), md.getFullname(),
						md.getDescription(), an.getUnit(), an.getDataType(),
						an.getSpecialisationOf());
			}
			break;
		}

		if (tree != null) {
			node = new DefaultMutableTreeNode(md);
			((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parent,
					index);
			TreePath path = new TreePath(parent.getPath());
			tree.expandPath(path);

			path = new TreePath(node.getPath());
			tree.setSelectionPath(path);
		} else {
			list.getMDListModel().add(md, index);
			list.getSelectionModel().setSelectionInterval(index, index);
		}
	}

}
