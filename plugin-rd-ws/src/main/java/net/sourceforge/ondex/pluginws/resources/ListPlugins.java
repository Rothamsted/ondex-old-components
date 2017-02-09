package net.sourceforge.ondex.pluginws.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sourceforge.ondex.pluginws.Artifact;
import net.sourceforge.ondex.pluginws.Main;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

@Path("/list")
public class ListPlugins {

	HashMap<String, Map<String, Set<Artifact>>> artifactIndex = new HashMap<String, Map<String, Set<Artifact>>>();

	private void indexArtifacts(Artifact[] artifacts) {
		for (Artifact artifact : artifacts) {
			Map<String, Set<Artifact>> groups = artifactIndex.get(artifact
					.getGroupId());
			if (groups == null) {
				groups = new HashMap<String, Set<Artifact>>();
				artifactIndex.put(artifact.getGroupId(), groups);
			}
			Set<Artifact> artifactsIndex = groups.get(artifact.getArtifactId());
			if (artifactsIndex == null) {
				artifactsIndex = new HashSet<Artifact>();
				groups.put(artifact.getArtifactId(), artifactsIndex);
			}
			artifactsIndex.add(artifact);
		}
	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String invoke() {

		StringBuilder replyElement = new StringBuilder();
		replyElement.append("<html>\n");
		replyElement.append("<head>\n");
		replyElement.append("<title>Plugin List</title>\n");
		replyElement
				.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>");
		replyElement.append("</head>\n");
		replyElement.append("<body>\n");
		replyElement.append("<h1>plugin modules</h1>\n");

		try {
			// add main artifacts
			URL url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			// add apps
			url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex.apps&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			// add core
			url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex.core&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			// add modules
			url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex.modules&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			// add taverna
			url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex.taverna&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			// add webservices
			url = new URL(
					Main.NEXUS_ONDEX_SERVICE
							+ "/local/data_index/repositories/snapshots/content?g=net.sourceforge.ondex.webservices&v=0.6.0-SNAPSHOT");
			indexArtifacts(getListOfDescriptorURLs(url.openStream()));

			replyElement.append("<ul>");
			List<String> grouplist = new ArrayList<String>(
					artifactIndex.keySet());
			Collections.sort(grouplist);
			for (String groupId : grouplist) {
				if (groupId.equals("net.sourceforge.ondex")) {
					continue;
				}
				replyElement.append("<h2>" + groupId + "</h2>\n");

				List<String> artifactlist = new ArrayList<String>(artifactIndex
						.get(groupId).keySet());
				Collections.sort(artifactlist);

				for (String artifactId : artifactlist) {

					replyElement.append("<li><b>" + artifactId + "</b> ");
					List<Artifact> versions = new ArrayList<Artifact>(
							artifactIndex.get(groupId).get(artifactId));
					Collections.sort(versions);
					for (Artifact artifact : versions) {
						replyElement.append("[" + artifact.getVersion() + ": ");

						// only modules get documentation
						if (groupId.equals("net.sourceforge.ondex.modules"))
							replyElement.append("<a href=\"fetch?"
									+ artifact.getNexusOptions().replaceAll(
											"&", "&amp;") + "\""
									+ ">Documentation</a>, ");
						replyElement.append("<a href=\""
								+ Main.NEXUS_ONDEX_SERVICE
								+ "/local/artifact/maven/redirect?"
								+ artifact.getNexusOptions().replaceAll("&",
										"&amp;") + "\"" + ">Download</a>");
						replyElement.append("] ");
					}
					replyElement.append("</li>");
				}
			}
			replyElement.append("</ul>");
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		replyElement.append("</body>\n");
		replyElement.append("</html>");

		return replyElement.toString();
	}

	private Artifact[] getListOfDescriptorURLs(InputStream artifactListXml)
			throws JDOMException, IOException {

		SAXBuilder builder = new SAXBuilder(); // parameters control validation,
		// etc
		Document doc = builder.build(artifactListXml);

		Set<Artifact> urlsToDescriptors = new HashSet<Artifact>();

		Iterator<?> artifactElements = doc.getDocument().getDescendants(
				new ElementFilter("artifact"));
		while (artifactElements.hasNext()) {
			Element artifactElement = (Element) artifactElements.next();

			Artifact artifact = new Artifact(artifactElement);

			urlsToDescriptors.add(artifact);
		}
		return urlsToDescriptors
				.toArray(new Artifact[urlsToDescriptors.size()]);
	}

}
