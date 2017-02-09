package net.sourceforge.ondex.util.metadata.ops;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.util.metadata.EditorPanel.Field;

public class UpdateOperation<M extends MetaData> extends Operation<M>{
	
	public UpdateOperation(M md, Field field, Object old, Object nu) {
		super(md);
		this.field = field;
		oldValue = old;
		newValue = nu;
	}
	
	private Field field;
	
	private Object oldValue, newValue;
	
	public void perform() {
		setValue(newValue);
	}
	
	public void revert() {
		setValue(oldValue);
	}
	
	private void setValue(Object o) {
		switch(field) {
		case FULLNAME:
			md.setFullname((String)o);
			break;
		case DESCRIPTION: 
			md.setDescription((String)o);
			break;
		default:
			switch (mdt) {
			case ATTRIBUTE_NAME:
				AttributeName an = (AttributeName) md;
				switch (field) {
				case UNIT:
					an.setUnit((Unit)o);
					break;
				}
				break;
			case RELATION_TYPE:
				RelationType rt = (RelationType) md;
				switch (field) {
				case INVERSE:
					rt.setInverseName((String)o);
					break;
				case SYMMETRIC:
					if ((Boolean) o) {
						rt.setSymmetric(true);
						rt.setAntisymmetric(false);
					} else {
						rt.setSymmetric(false);
					}
					break;
				case ANTISYMMETRIC:
					if ((Boolean)o) {
						rt.setAntisymmetric(true);
						rt.setSymmetric(false);
					} else {
						rt.setAntisymmetric(false);
					}
					break;
				case TRANSITIVE:
					rt.setTransitiv((Boolean)o);
					break;
				case REFLEXIVE:
					rt.setReflexive((Boolean)o);
					break;
				}
				break;
			}
			break;
		}
	}
	
}
