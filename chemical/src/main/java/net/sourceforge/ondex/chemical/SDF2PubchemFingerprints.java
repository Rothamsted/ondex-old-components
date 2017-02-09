package net.sourceforge.ondex.chemical;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.openscience.cdk.ChemFile;
import org.openscience.cdk.ChemObject;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.fingerprint.PubchemFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.formats.IChemFormatMatcher;
import org.openscience.cdk.io.formats.MDLFormat;
import org.openscience.cdk.io.formats.SDFFormat;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;

public class SDF2PubchemFingerprints {

	/**
	 * SDF field for ID
	 */
	private static final String CHEMBL_ID = "chembl_id";

	/**
	 * Copied from PubchemFingerprinter. // the first four bytes contains the
	 * length of the fingerprint
	 * 
	 * @param m_bits
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private static String encode(byte[] m_bits)
			throws UnsupportedEncodingException {
		byte[] pack = new byte[4 + m_bits.length];

		pack[0] = (byte) ((PubchemFingerprinter.FP_SIZE & 0xffffffff) >> 24);
		pack[1] = (byte) ((PubchemFingerprinter.FP_SIZE & 0x00ffffff) >> 16);
		pack[2] = (byte) ((PubchemFingerprinter.FP_SIZE & 0x0000ffff) >> 8);
		pack[3] = (byte) (PubchemFingerprinter.FP_SIZE & 0x000000ff);
		for (int i = 0; i < m_bits.length; ++i) {
			pack[i + 4] = m_bits[i];
		}
		return new String(Base64.encodeBase64(pack), "UTF-8");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		// check arguments
		if (args.length != 1 || args[0].trim().length() == 0) {
			System.out
					.println("Usage: SDF2PubchemFingerprints [file.sdf|.sdf.gz]");
			System.exit(0);
		}

		// file name of file to parse
		File file = new File(args[0]);

		// open file as stream, handle compressed files
		InputStream inputStream = new FileInputStream(file);
		if (file.getAbsolutePath().endsWith(".gz")) {
			inputStream = new GZIPInputStream(inputStream);
		}

		// initialise printer
		PubchemFingerprinter printer = new PubchemFingerprinter();

		// output file in same location as input
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				file.getAbsolutePath() + ".prints")));

		// guess format of file and construct reader
		ReaderFactory readerFactory = new ReaderFactory();
		readerFactory.registerFormat((IChemFormatMatcher) MDLFormat
				.getInstance());
		readerFactory.registerFormat((IChemFormatMatcher) SDFFormat
				.getInstance());
		ISimpleChemObjectReader reader = readerFactory
				.createReader(new InputStreamReader(inputStream));

		// check that file contains molecules
		if (reader.accepts(Molecule.class)) {

			System.out.println("Using Reader: " + reader);

			// read content of file
			ChemFile content = (ChemFile) reader
					.read((ChemObject) new ChemFile());

			// list all molecules
			List<IAtomContainer> containersList = ChemFileManipulator
					.getAllAtomContainers(content);
			System.out.println("Total molecules parsed:"
					+ containersList.size());

			double percent = containersList.size() / 100;

			// iterate over all molecules
			int i = 0;
			for (IAtomContainer ac : containersList) {

				// some status update
				i++;
				if (i % percent == 0)
					System.out.println((int) (100 - i / percent) + "%");

				// there should be a ChEMBL ID
				if (ac.getProperties().containsKey(CHEMBL_ID)) {
					String id = (String) ac.getProperty(CHEMBL_ID);

					// write tab-separated output
					writer.write(id);
					writer.write("\t");

					// calculate finger print
					try {
						printer.getFingerprint(ac);
						String base64 = encode(printer.getFingerprintAsBytes());
						writer.write(base64);
					} catch (Exception cdk) {
						writer.write(i + ": " + cdk.getMessage());
					}
					writer.write("\n");
				} else
					System.out.println("ChEMBLdb ID missing for entry " + i);
			}
		}

		// close writer
		writer.flush();
		writer.close();
	}
}
