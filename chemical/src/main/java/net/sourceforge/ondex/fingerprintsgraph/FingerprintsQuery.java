package net.sourceforge.ondex.fingerprintsgraph;

import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

public class FingerprintsQuery implements Constants {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// check correct arguments
		if (args.length != 1) {
			System.out.println("Usage: FingerprintsQuery [queryterm]");
			System.exit(0);
		}

		String query = args[0];

		// START SNIPPET: startDb
		GraphDatabaseService graphDb = Neo4jDatabase.getGraphDb();
		Index<Node> compoundIndex = Neo4jDatabase.getCompoundIndex();
		Index<Node> categoryIndex = Neo4jDatabase.getCategoryIndex();

		// START SNIPPET: addCompounds
		Transaction tx = graphDb.beginTx();
		try {

			long start = System.currentTimeMillis();

			// first search in compounds
			IndexHits<Node> hits = compoundIndex.get(COMPOUND_ID, query);
			if (hits.size() > 0) {
				System.out.println("Found compounds for query:");
				while (hits.hasNext()) {
					Node n = hits.next();
					String id = (String) n.getProperty(COMPOUND_ID);
					StringBuilder sb = new StringBuilder();
					int count = 0;
					for (Relationship r : n.getRelationships(RelTypes.CATEGORY)) {
						Node cat = r.getOtherNode(n);
						sb.append(cat.getProperty(CATEGORY_ID));
						sb.append(" ");
						count++;
					}
					System.out.println("Found " + count
							+ " categories for compound " + id);
					System.out.println(sb.toString());
					System.out.println("Search took: "
							+ (System.currentTimeMillis() - start));
				}
			}

			start = System.currentTimeMillis();

			hits = categoryIndex.get(CATEGORY_ID, query);
			if (hits.size() > 0) {
				System.out.println("Found categories for query:");
				while (hits.hasNext()) {
					Node n = hits.next();
					String id = (String) n.getProperty(CATEGORY_ID);
					int count = 0;
					Iterator<Relationship> it = n.getRelationships(
							RelTypes.CATEGORY).iterator();
					while (it.hasNext()) {
						it.next();
						count++;
					}
					System.out.println("Found " + count
							+ " compounds for category " + id);
					System.out.println("Search took: "
							+ (System.currentTimeMillis() - start));
				}
			}

			tx.success();
		} finally {
			tx.finish();
		}
		System.out.println("Shutting down database ...");
		Neo4jDatabase.shutdown();
	}

}
