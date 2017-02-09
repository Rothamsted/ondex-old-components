package net.sourceforge.ondex.fingerprintsgraph;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import scala.actors.threadpool.Arrays;

/**
 * Write simple statistics about a give query set and the CHEMBL database
 * itself.
 * 
 * @author taubertj
 * 
 */
public class FingerprintsClusterStats implements Constants {

	/**
	 * Entry point for class
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// check correct arguments
		if (args.length == 0) {
			System.out
					.println("Usage: FingerprintsClusterStats [file1] [file2] [file3]...");
			System.exit(0);
		}

		// new processing instance
		new FingerprintsClusterStats(args);
	}

	/**
	 * Constructor coordinates processing of files
	 * 
	 * @param args
	 * @throws Exception
	 */
	private FingerprintsClusterStats(String[] args) throws Exception {

		// START SNIPPET: startDb
		GraphDatabaseService graphDb = Neo4jDatabase.getGraphDb();

		// START SNIPPET: addCompounds
		Transaction tx = graphDb.beginTx();
		try {

			// work in directory of first file argument
			File baseDir = new File(args[0]).getParentFile();
			System.out.println("Saving output to directory: "
					+ baseDir.getAbsolutePath());

			// write output to file
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
					baseDir.getAbsolutePath() + File.separator
							+ "fingerprint_stats.tab")));

			// write category statistics
			final Map<Node, Integer> totalPerCategory = getTotalPerCategory();
			writeTotalPerCategory(writer, totalPerCategory);
			Map<Node, Double> totalPercentage = calculatePercentage(totalPerCategory);

			// sort categories alphabetically
			Node[] sortedByName = totalPercentage.keySet().toArray(new Node[0]);
			Arrays.sort(sortedByName, new Comparator<Node>() {
				@Override
				public int compare(Node o1, Node o2) {
					return o1.getProperty(CATEGORY_ID).toString()
							.compareTo(o2.getProperty(CATEGORY_ID).toString());
				}
			});

			// create the dataset...
			DefaultCategoryDataset datasetPercentage = new DefaultCategoryDataset();
			DefaultCategoryDataset datasetRank = new DefaultCategoryDataset();

			// add all categories to bar chart
			for (Node n : sortedByName) {
				String category = (String) n.getProperty(CATEGORY_ID);

				// total percentage
				datasetPercentage.addValue(totalPercentage.get(n), "ChEMBL",
						category);
			}

			// sort by rank
			Node[] sortedByRank = totalPercentage.keySet().toArray(new Node[0]);
			Arrays.sort(sortedByRank, new Comparator<Node>() {
				@Override
				public int compare(Node o1, Node o2) {
					return totalPerCategory.get(o2) > totalPerCategory.get(o1) ? 1
							: -1;
				}
			});

			// add all categories to bar chart
			int i = 0;
			for (Node n : sortedByRank) {
				String category = (String) n.getProperty(CATEGORY_ID);

				// value is #categories - rank
				datasetRank.addValue(sortedByName.length - i, "ChEMBL",
						category);
				i++;
			}

			// process all files
			for (String arg : args) {

				// parse file
				File file = new File(arg);
				Set<Node> hits = getHitsFromFile(file);
				writer.write("\nTotal hits found for " + file.getAbsolutePath()
						+ "\t" + hits.size() + "\n\n");

				// write query hits to categories
				final Map<Node, Integer> hitsPerCategory = getHitsPerCategory(hits);
				writeTotalPerCategory(writer, hitsPerCategory);

				// rank locally
				Node[] array = hitsPerCategory.keySet().toArray(new Node[0]);
				Arrays.sort(array, new Comparator<Node>() {
					@Override
					public int compare(Node o1, Node o2) {
						return hitsPerCategory.get(o2) > hitsPerCategory
								.get(o1) ? 1 : -1;
					}
				});

				// calculate rank
				Map<Node, Integer> ranks = new HashMap<Node, Integer>();
				i = 0;
				for (Node n : array) {
					ranks.put(n, array.length - i);
					i++;
				}

				// get percentage distribution
				Map<Node, Double> hitsPercentage = calculatePercentage(hitsPerCategory);
				writePercentage(writer, hitsPercentage, totalPercentage);

				// add all categories to bar chart
				for (Node n : sortedByName) {
					String category = (String) n.getProperty(CATEGORY_ID);

					// hits percentage
					if (hitsPercentage.containsKey(n)) {
						datasetPercentage.addValue(hitsPercentage.get(n),
								file.getName(), category);
					} else {
						// no hits in this category
						datasetPercentage.addValue(0.0, file.getName(),
								category);
					}
				}

				// add all categories to bar chart
				for (Node n : sortedByRank) {
					String category = (String) n.getProperty(CATEGORY_ID);

					// hits rank
					datasetRank
							.addValue(ranks.get(n), file.getName(), category);
				}
			}

			writer.close();
			tx.success();

			System.out.println("Exporting bar chart.");

			// create chart panel and saves it
			JFreeChart chart = createChart(datasetPercentage, "Percentage",
					false);
			ChartUtilities.saveChartAsPNG(new File(baseDir.getAbsolutePath()
					+ File.separator + "fingerprint_percent.png"), chart,
					15000, 5000);

			chart = createChart(datasetRank, "Inverse Rank", true);
			ChartUtilities.saveChartAsPNG(new File(baseDir.getAbsolutePath()
					+ File.separator + "fingerprint_rank.png"), chart, 15000,
					5000);

		} finally {
			tx.finish();
		}
		System.out.println("Shutting down database ...");
		Neo4jDatabase.shutdown();
	}

	/**
	 * Calculates the percent value of each category.
	 * 
	 * @param map
	 *            Map<Node, Integer>
	 * @return Map<Node, Double>
	 */
	private Map<Node, Double> calculatePercentage(Map<Node, Integer> map) {

		// return value
		Map<Node, Double> results = new HashMap<Node, Double>();

		// calculate total category assignments
		int total = 0;
		for (Node cat : map.keySet()) {
			total += map.get(cat);
		}

		// get count for each category and calculate percent of total
		for (Node cat : map.keySet()) {
			double percentInQueries = (double) map.get(cat) / (double) total
					* 100;
			results.put(cat, percentInQueries);
		}

		return results;
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * @param label
	 *            the value label
	 * @return The chart.
	 */
	private JFreeChart createChart(CategoryDataset dataset, String label,
			boolean stacked) {

		// create the chart...
		JFreeChart chart;

		if (stacked)
			chart = ChartFactory.createStackedBarChart("Fingerprint profile",
			// chart title
					"Fingerprint", // domain axis label
					label, // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					true, // include legend
					true, // tooltips?
					false // URLs?
					);
		else
			chart = ChartFactory.createBarChart("Fingerprint profile",
			// chart title
					"Fingerprint", // domain axis label
					label, // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					true, // include legend
					true, // tooltips?
					false // URLs?
					);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.white);

		// set the range axis to display integers only...
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		// disable bar outlines...
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setDrawBarOutline(false);

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions
				.createUpRotationLabelPositions(Math.PI / 6.0));
		// OPTIONAL CUSTOMISATION COMPLETED.

		return chart;
	}

	/**
	 * Extract nodes which matches the ID specified in the file
	 * 
	 * @param file
	 *            File to parse
	 * @return Set<Node>
	 * @throws Exception
	 */
	private Set<Node> getHitsFromFile(File file) throws Exception {

		// return value
		Set<Node> results = new HashSet<Node>();

		// search in all compounds
		Index<Node> compoundIndex = Neo4jDatabase.getCompoundIndex();

		// open file as stream, handle compressed files
		InputStream inputStream = new FileInputStream(file);
		if (file.getAbsolutePath().endsWith(".gz")) {
			inputStream = new GZIPInputStream(inputStream);
		}

		// setup reader
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));

		// parse all queries from file
		List<String> queries = new ArrayList<String>();
		while (reader.ready()) {
			String line = reader.readLine();
			queries.add(line);
		}
		reader.close();

		// collect all hit nodes
		System.out.println("Collecting query hits.");
		for (String query : queries) {
			Iterator<Node> it = compoundIndex.get(COMPOUND_ID, query)
					.iterator();
			while (it.hasNext()) {
				results.add(it.next());
			}
		}

		return results;
	}

	/**
	 * Get categories counts for the hits
	 * 
	 * @param hits
	 *            Set<Node>
	 * @return Map<Node, Integer>
	 */
	private Map<Node, Integer> getHitsPerCategory(Set<Node> hits) {

		// return value
		Map<Node, Integer> results = new HashMap<Node, Integer>();

		// simple categories statistics
		System.out.println("Processing categories for cluster hits.");
		for (Node n : hits) {
			for (Relationship r : n.getRelationships(RelTypes.CATEGORY)) {
				Node cat = r.getOtherNode(n);
				if (!results.containsKey(cat))
					results.put(cat, 1);
				else
					results.put(cat, results.get(cat) + 1);
			}
		}

		return results;
	}

	/**
	 * Extract all available categories and their counts
	 * 
	 * @return Map<Node, Integer>
	 */
	private Map<Node, Integer> getTotalPerCategory() {

		// return value
		Map<Node, Integer> results = new HashMap<Node, Integer>();

		// get Neo4J database
		GraphDatabaseService graphDb = Neo4jDatabase.getGraphDb();

		// count number of compounds per category
		System.out.println("Processing categories.");
		for (Relationship r : graphDb.getReferenceNode().getRelationships(
				RelTypes.CATEGORY_REFERENCE)) {
			Node cat = r.getEndNode();
			int count = 0;
			Iterator<Relationship> it = cat.getRelationships().iterator();
			while (it.hasNext()) {
				it.next();
				count++;
			}
			results.put(cat, count);
		}

		return results;
	}

	/**
	 * Writes percentage values for two given maps
	 * 
	 * @param first
	 *            Map<Node, Double>
	 * @param second
	 *            Map<Node, Double>
	 * @throws Exception
	 */
	private void writePercentage(BufferedWriter writer,
			final Map<Node, Double> first, final Map<Node, Double> second)
			throws Exception {

		NumberFormat formatter = new DecimalFormat(".00");

		// sort categories by highest percentage
		Node[] array = first.keySet().toArray(new Node[0]);
		Arrays.sort(array, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return first.get(o2) > first.get(o1) ? 1 : -1;
			}
		});

		// write percentage
		for (Node cat : array) {

			// get category name
			String id = (String) cat.getProperty(CATEGORY_ID);

			// percentage in first followed by second
			writer.write(id + "\t" + formatter.format(first.get(cat)) + "%\t"
					+ formatter.format(second.get(cat)) + "%\n");
		}
	}

	/**
	 * Write categories and their count to a file
	 * 
	 * @param writer
	 *            output to file
	 * @param map
	 *            Map<Node, Integer>
	 * @throws Exception
	 */
	private void writeTotalPerCategory(BufferedWriter writer,
			Map<Node, Integer> map) throws Exception {

		// sort categories by name
		Node[] array = map.keySet().toArray(new Node[0]);
		Arrays.sort(array, new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return o1.getProperty(CATEGORY_ID).toString()
						.compareTo(o2.getProperty(CATEGORY_ID).toString());
			}
		});

		// write categories
		int totalAssignments = 0;
		for (Node cat : array) {
			writer.write(cat.getProperty(CATEGORY_ID) + "\t" + map.get(cat)
					+ "\n");
			totalAssignments += map.get(cat);
		}
		writer.write("\nTotal number of categories\t" + array.length + "\n");
		writer.write("Total number of assignments\t" + totalAssignments
				+ "\n\n");
	}

}
