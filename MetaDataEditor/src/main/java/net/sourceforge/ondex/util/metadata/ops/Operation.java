package net.sourceforge.ondex.util.metadata.ops;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;

public abstract class Operation<M extends MetaData> {
	protected M md;
	
	protected MetaDataType mdt;
	
	public Operation(M md) {
		mdt = MetaDataType.fromClass(md);
		this.md = md;
	}
	
	public abstract void perform();
	public abstract void revert();
}
