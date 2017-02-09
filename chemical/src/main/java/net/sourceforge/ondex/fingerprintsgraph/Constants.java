package net.sourceforge.ondex.fingerprintsgraph;

import org.neo4j.graphdb.RelationshipType;

public interface Constants {

	// START SNIPPET: createRelTypes
	public static enum RelTypes implements RelationshipType {
		CATEGORY, CATEGORY_REFERENCE
	}

	public static final String COMPOUND_ID = "compound_id";
	public static final String CATEGORY_ID = "category_id";
	public static final String DB_PATH = "neo4j-store";

}
