package net.sourceforge.ondex.util.metadata.model;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;

public enum MetaDataType {
	CONCEPT_CLASS, RELATION_TYPE, CV, EVIDENCE_TYPE, ATTRIBUTE_NAME, UNIT;
	
	public static MetaDataType fromClass(MetaData md) {
		if (md instanceof ConceptClass) {
			return CONCEPT_CLASS;
		} else if (md instanceof RelationType) {
			return RELATION_TYPE;
		} else if (md instanceof net.sourceforge.ondex.core.DataSource) {
			return CV;
		} else if (md instanceof EvidenceType) {
			return EVIDENCE_TYPE;
		} else if (md instanceof AttributeName) {
			return ATTRIBUTE_NAME;
		} else if (md instanceof Unit) {
			return UNIT;
		} else {
			return null;
		}
	}
}
