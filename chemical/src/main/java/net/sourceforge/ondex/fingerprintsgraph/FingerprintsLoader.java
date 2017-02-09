package net.sourceforge.ondex.fingerprintsgraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

public class FingerprintsLoader implements Constants {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// check correct arguments
		if (args.length != 1) {
			System.out.println("Usage: FingerprintsLoader [file]");
			System.exit(0);
		}

		// START SNIPPET: startDb
		GraphDatabaseService graphDb = Neo4jDatabase.getGraphDb();
		Index<Node> compoundIndex = Neo4jDatabase.getCompoundIndex();
		Index<Node> categoryIndex = Neo4jDatabase.getCategoryIndex();

		// START SNIPPET: addCompounds
		Transaction tx = graphDb.beginTx();
		try {

			// the file to load
			File file = new File(args[0]);

			// open file as stream, handle compressed files
			InputStream inputStream = new FileInputStream(file);
			if (file.getAbsolutePath().endsWith(".gz")) {
				inputStream = new GZIPInputStream(inputStream);
			}

			// setup reader
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));

			// already created categories of fingerprints
			Map<String, Node> categories = new HashMap<String, Node>();

			String id = null;

			// read file
			int i = 0;
			while (reader.ready()) {
				String line = reader.readLine();

				// commit every 10000 entries
				i++;
				if (i % 10000 == 0) {
					tx.success();
					tx.finish();
					tx = graphDb.beginTx();
				}

				if (line.startsWith(">")) {
					// should be a CHEMBL ID
					if (!line.contains("CHEMBL")) {
						System.out.println("Non-CHEMBL ID found: " + id);
						// skip to next entry
						reader.readLine();
					} else
						id = line.substring(line.indexOf("CHEMBL"));
				} else {
					// create compound once
					Node comp = graphDb.createNode();
					comp.setProperty(COMPOUND_ID, id);
					compoundIndex.add(comp, COMPOUND_ID, id);

					// split tab separated categories
					for (String s : line.trim().split("\t")) {

						// check that a category exists
						if (s.trim().length() == 0) {
							System.out.println("Empty category for: " + id);
							continue;
						}

						// categories are cached
						if (!categories.containsKey(s)) {
							// Create category sub reference node (see design
							// guidelines on
							// http://wiki.neo4j.org/ )
							Node categoryReferenceNode = graphDb.createNode();
							categoryReferenceNode.setProperty(CATEGORY_ID, s);
							categoryIndex.add(categoryReferenceNode,
									CATEGORY_ID, s);
							graphDb.getReferenceNode().createRelationshipTo(
									categoryReferenceNode,
									RelTypes.CATEGORY_REFERENCE);
							categories.put(s, categoryReferenceNode);
						}

						// get current category
						Node cat = categories.get(s);

						// create relation between category and compound
						cat.createRelationshipTo(comp, RelTypes.CATEGORY);
					}
				}
			}

			// close reader
			reader.close();

			tx.success();

			// print out categories
			int count = 0;
			for (Relationship r : graphDb.getReferenceNode().getRelationships(
					RelTypes.CATEGORY_REFERENCE)) {
				Node cat = r.getOtherNode(graphDb.getReferenceNode());
				System.out.println(cat.getProperty(CATEGORY_ID));
				count++;
			}

			System.out.println("Categories found: " + categories.size()
					+ " == " + count + " nodes.");

		} finally {
			tx.finish();
		}
		System.out.println("Shutting down database ...");
		Neo4jDatabase.shutdown();
	}

}
