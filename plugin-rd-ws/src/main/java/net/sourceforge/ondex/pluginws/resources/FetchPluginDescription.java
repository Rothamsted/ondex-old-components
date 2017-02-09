package net.sourceforge.ondex.pluginws.resources;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sourceforge.ondex.pluginws.Main;

@Path("/fetch")
public class FetchPluginDescription {

	private TransformerFactory tFactory = TransformerFactory.newInstance();

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String invoke(@QueryParam("r") String r, @QueryParam("g") String g,
			@QueryParam("a") String a, @QueryParam("v") String v,
			@QueryParam("e") String e) {
		String query = "r=" + r + "&g=" + g + "&a=" + a + "&v=" + v + "&e=" + e
				+ "&c=jar-with-module-descriptor";
		// System.out.println(query);
		String zipArtifact = Main.NEXUS_ONDEX_SERVICE
				+ "/local/artifact/maven/redirect?" + query;
		return getDescriptorXml(zipArtifact, true);
	}

	private String getDescriptorXml(String zipFile, boolean transformXsl) {

		try {
			URL zipFileUrl = new URL(zipFile);
			ZipInputStream zis = new ZipInputStream(zipFileUrl.openStream());
			ZipEntry entry = null;

			String xmlFile = null;

			InputStream is = Main.class.getClassLoader()
					.getResource("workflow-component-description.xsl")
					.openStream();
			String xslFile = new java.util.Scanner(is).useDelimiter("\\A")
					.next();

			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory()
						&& entry.getName().equals(
								"workflow-component-description.xml")
						|| (entry.getName().endsWith(
								"workflow-component-description.xsl") && transformXsl)) {

					long size = entry.getCompressedSize();
					if (size < 0)
						size = 32;
					ByteArrayOutputStream fout = new ByteArrayOutputStream(
							(int) size);

					for (int c = zis.read(); c != -1; c = zis.read()) {
						fout.write(c);
					}
					zis.closeEntry();
					fout.close();

					if (entry.getName().endsWith(".xml")) {
						xmlFile = new String(fout.toByteArray());
						if (!transformXsl) {
							zis.close();
							return xmlFile;
						}
					}
				}
			}
			zis.close();

			if (transformXsl && xslFile != null && xmlFile != null) {

				try {
					Transformer transformer = tFactory
							.newTransformer(new StreamSource(new StringReader(
									xslFile)));

					ByteArrayOutputStream bos = new ByteArrayOutputStream();

					transformer.transform(new StreamSource(new StringReader(
							xmlFile)),
							new javax.xml.transform.stream.StreamResult(bos));

					return new String(bos.toByteArray());
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<p>Module is empty or an aggregator</p>";
	}

}
