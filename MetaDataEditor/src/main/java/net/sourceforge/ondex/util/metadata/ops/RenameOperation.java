package net.sourceforge.ondex.util.metadata.ops;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.util.metadata.EditorPanel;
import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.elements.MEList;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;

public class RenameOperation<M extends MetaData> extends Operation<M> {

	private String oldId, newId;
	
	private DefaultMutableTreeNode node;
	
	private MEList<M> list;
	
	private EditorPanel<M> editorPanel;
	
	private int index;
	
	public RenameOperation(M md, String oldId, String newId, DefaultMutableTreeNode node, EditorPanel<M> ep) {
		super(md);
		this.oldId = oldId;
		this.newId = newId;
		this.node = node;
		this.editorPanel = ep;
	}
	
	public RenameOperation(M md, String oldId, String newId, MEList<M> list, int index, EditorPanel<M> ep) {
		super(md);
		this.oldId = oldId;
		this.newId = newId;
		this.list = list;
		this.index = index;
		this.editorPanel = ep;
	}

	@Override
	public void perform() {
		rename(newId);
	}

	@Override
	public void revert() {
		rename(oldId);
	}
	
	@SuppressWarnings("unchecked")
	private void rename(String newId) {
		ONDEXGraphMetaData omd = MetaDataEditor.getInstance()
			.getCurrentMetaDataWindow().getMetaData();
		switch (MetaDataType.fromClass(md)) {
		case CONCEPT_CLASS:
			ConceptClass cc = (ConceptClass) md;
			omd.deleteConceptClass(md.getId());
			md = (M)omd.createConceptClass(newId, md.getFullname(), 
					md.getDescription(), cc.getSpecialisationOf());
			break;
		case RELATION_TYPE:
			RelationType rt = (RelationType)md;
			omd.deleteRelationType(md.getId());
			md = (M)omd.createRelationType(newId, md.getFullname(), 
					md.getDescription(), rt.getInverseName(), 
					rt.isAntisymmetric(), rt.isReflexive(), 
					rt.isSymmetric(), rt.isTransitiv(), 
					rt.getSpecialisationOf());
			break;
		case CV:
			omd.deleteDataSource(md.getId());
			md = (M)omd.createDataSource(newId, md.getFullname(), 
					md.getDescription());
			break;
		case EVIDENCE_TYPE:
			omd.deleteEvidenceType(md.getId());
			md = (M)omd.createEvidenceType(newId, md.getFullname(), 
					md.getDescription());
			break;
		case UNIT:
			omd.deleteUnit(md.getId());
			md = (M)omd.createUnit(newId, md.getFullname(), 
					md.getDescription());
			break;
		case ATTRIBUTE_NAME:
			AttributeName an = (AttributeName) md;
			omd.deleteAttributeName(md.getId());
			md = (M)omd.createAttributeName(newId, md.getFullname(), md.getDescription(), 
					an.getUnit(), an.getDataType(), an.getSpecialisationOf());
			break;
		}
		
		if (node != null) {
			node.setUserObject(md);
		} else {
			list.getMDListModel().replace(index, md);
		}
		editorPanel.setContents(md);
	}

}
