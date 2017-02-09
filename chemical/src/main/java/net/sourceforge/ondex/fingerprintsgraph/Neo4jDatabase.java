package net.sourceforge.ondex.fingerprintsgraph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Neo4jDatabase implements Constants {

	private static Index<Node> compoundIndex, categoryIndex;
	private static GraphDatabaseService graphDb;

	static {
		// START SNIPPET: startDb
		graphDb = new EmbeddedGraphDatabase(DB_PATH);
		compoundIndex = graphDb.index().forNodes("compounds");
		categoryIndex = graphDb.index().forNodes("categories");
		registerShutdownHook();
		// END SNIPPET: startDb
	}
	
	public static Index<Node> getCategoryIndex() {
		return categoryIndex;
	}

	public static Index<Node> getCompoundIndex() {
		return compoundIndex;
	}

	public static GraphDatabaseService getGraphDb() {
		return graphDb;
	}

	private static void registerShutdownHook() {
		// Registers a shutdown hook for the Neo4j and index service instances
		// so that it shuts down nicely when the VM exits (even if you
		// "Ctrl-C" the running example before it's completed)
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
	}

	public static void shutdown() {
		graphDb.shutdown();
	}
}
