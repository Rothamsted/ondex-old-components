package net.sourceforge.ondex.fingerprintsstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FingerprintsConsensus {

	// already created categories of fingerprints
	static Map<String, Integer> categories = new HashMap<String, Integer>();
	static int catCount = 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// check correct arguments
		if (args.length != 3) {
			System.out
					.println("Usage: FingerprintsPCA [file] [queryfile] [number]");
			System.exit(0);
		}

		// cutoff for results returned
		int cutoff = Integer.parseInt(args[2]);

		// the query file to load
		File file = new File(args[1]);
		Map<String, BitSet> queries = parseFile(file);

		// transform finger prints of queries into double matrix
		int i = 0;
		double[][] matrix = new double[queries.size()][categories.size()];
		for (String key : queries.keySet()) {
			BitSet bs = queries.get(key);
			for (int j = 0; j < categories.size(); j++) {
				// set matrix to 1 or 0
				matrix[i][j] = bs.get(j) ? 1 : 0;
			}
			i++;
		}
		System.out.println("Query set size: " + i);

		// output query as matrix representation
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumFractionDigits(0);
		System.out.println("Query matrix: ");
		for (int j = 0; j < matrix.length; j++) {
			for (int l = 0; l < matrix[j].length; l++) {
				System.out.print(nf.format(matrix[j][l]) + " ");
			}
			System.out.println();
		}

		// create consensus vector over matrix
		double[] consensus = new double[categories.size()];
		for (int j = 0; j < matrix.length; j++) {
			for (int l = 0; l < matrix[j].length; l++) {
				consensus[l] = (consensus[l] + matrix[j][l]) / 2;
			}
		}

		// normalise consensus vector
		double magnitude = Magnitude(consensus);
		for (int j = 0; j < consensus.length; j++) {
			consensus[j] = consensus[j] / magnitude;
		}

		// output some informations
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setMaximumIntegerDigits(1);
		nf.setMinimumIntegerDigits(1);
		System.out.println("Showing consensus vector of length "
				+ consensus.length);
		System.out.print("[ ");
		for (int j = 0; j < consensus.length; j++) {
			System.out.print(nf.format(consensus[j]) + " ");
		}
		System.out.println("] " + nf.format(Magnitude(consensus)));

		// the file to load
		file = new File(args[0]);
		Map<String, BitSet> fingerprints = parseFile(file);

		System.out.println("A total of " + categories.size()
				+ " categories found.");
		System.out.println("Fingerprints added for " + fingerprints.size()
				+ " compounds.");

		// transform length of consensus vector, all additional information gets
		// set to 0, ignoring features not in query set
		double[] temp = new double[categories.size()];
		System.arraycopy(consensus, 0, temp, 0, consensus.length);
		consensus = temp;
		System.out.println("Showing consensus vector of length "
				+ consensus.length);
		System.out.print("[ ");
		for (int j = 0; j < consensus.length; j++) {
			System.out.print(nf.format(consensus[j]) + " ");
		}
		System.out.println("] " + nf.format(Magnitude(consensus)));

		// score each database entry
		final Map<String, Double> scores = new HashMap<String, Double>();

		// calculate distance between k top components and all other
		// database finger prints
		for (String key : fingerprints.keySet()) {

			// get normalised feature vector
			double[] vector = getVector(fingerprints, key);

			// calculate distance between feature vector and consensus
			scores.put(key, Euclidian(vector, consensus));
		}

		// sort by combined score
		String[] array = scores.keySet().toArray(new String[0]);
		Arrays.sort(array, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return scores.get(o1) > scores.get(o2) ? 1 : -1;
			}
		});

		// output results
		i = 0;
		for (String key : array) {
			System.out.println(key + " " + scores.get(key));
			double[] vector = getVector(fingerprints, key);
			System.out.print("[ ");
			for (int j = 0; j < vector.length; j++) {
				System.out.print(nf.format(vector[j]) + " ");
			}
			System.out.println("] " + nf.format(Magnitude(vector)));
			i++;
			if (i == cutoff)
				break;
		}
	}

	/**
	 * Returns normalised feature vector for particular key.
	 * 
	 * @param fingerprints
	 * @param key
	 * @return
	 */
	private static double[] getVector(Map<String, BitSet> fingerprints,
			String key) {
		// calculate current feature vector
		double[] vector = new double[categories.size()];
		BitSet bs = fingerprints.get(key);
		for (int j = 0; j < categories.size(); j++) {
			vector[j] = bs.get(j) ? 1 : 0;
		}

		// normalise feature vector
		double magnitude = Magnitude(vector);
		for (int j = 0; j < categories.size(); j++) {
			vector[j] = vector[j] / magnitude;
		}
		return vector;
	}

	/**
	 * Parse finger prints file
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static Map<String, BitSet> parseFile(File file) throws Exception {

		// open file as stream, handle compressed files
		InputStream inputStream = new FileInputStream(file);
		if (file.getAbsolutePath().endsWith(".gz")) {
			inputStream = new GZIPInputStream(inputStream);
		}

		// setup reader
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		// fingerprints per compound
		Map<String, BitSet> fingerprints = new HashMap<String, BitSet>();

		// get id and category
		String id = null;

		// read file
		while (reader.ready()) {
			String line = reader.readLine();
			if (line.startsWith(">")) {
				// should be a CHEMBL ID
				if (!line.contains("CHEMBL")) {
					System.out.println("Non-CHEMBL ID found: " + id);
					// skip to next entry
					reader.readLine();
				} else
					id = line.substring(line.indexOf("CHEMBL"));
			} else {
				BitSet bs = new BitSet();

				// split tab separated categories
				for (String s : line.trim().split("\t")) {

					// check that a category exists
					if (s.trim().length() == 0) {
						System.out.println("Empty category for: " + id);
						continue;
					}

					// categories are cached
					if (!categories.containsKey(s)) {
						// Create category
						categories.put(s, Integer.valueOf(catCount));
						catCount++;
					}

					// get current category
					Integer cat = categories.get(s);
					bs.set(cat);
				}
				if (fingerprints.containsKey(id)) {
					System.out.println("Duplicated id for: " + id);
				} else {
					fingerprints.put(id, bs);
				}
			}
		}

		return fingerprints;
	}

	/**
	 * Get the dot product between two vectors
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double DotProduct(double[] a, double[] b) {
		if (a.length != b.length)
			// throw an exception of some kind
			throw new UnsupportedOperationException(
					"Vectors have to be same length!");

		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i] * b[i];

		return sum;
	}

	/**
	 * Get the euclidian distance in n space
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double Euclidian(double[] a, double[] b) {
		if (a.length != b.length)
			// throw an exception of some kind
			throw new UnsupportedOperationException(
					"Vectors have to be same length!");

		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += Math.pow((b[i] - a[i]), 2);

		return Math.sqrt(sum);
	}

	/**
	 * Get the mangitude (lenght) of a vector
	 * 
	 * @param a
	 * @return
	 */
	public static double Magnitude(double[] a) {

		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i] * a[i];

		return Math.sqrt(sum);
	}

}
