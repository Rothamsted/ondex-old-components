package net.sourceforge.ondex.pluginws;

/**
 * Represents a maven artifact returned by nexus
 * 
 * @author hindlem
 */
public class Artifact implements Comparable<Artifact> {

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getPackaging() {
		return packaging;
	}

	public String getRepoId() {
		return repoId;
	}

	private String groupId;
	private String artifactId;
	private String version;
	private String classifier = null;
	private String packaging;
	private String repoId;

	/**
	 * @param artifactElement
	 *            a nexus jdom xml artifact element
	 */
	public Artifact(Element artifactElement) {
		groupId = artifactElement.getChild("groupId").getValue();
		artifactId = artifactElement.getChild("artifactId").getValue();
		version = artifactElement.getChild("version").getValue();
		if (artifactElement.getChild("classifier") != null)
			classifier = artifactElement.getChild("classifier").getValue();
		packaging = artifactElement.getChild("packaging").getValue();
		repoId = artifactElement.getChild("repoId").getValue();
	}

	public String toResourceURL() {
		String classifier = "";
		if (getClassifier() != null)
			classifier = "&c=" + getClassifier();
		return Main.NEXUS_ONDEX_SERVICE + "/local/artifact/maven/redirect?"
				+ "r=" + getRepoId() + "&g=" + getGroupId() + "&a="
				+ getArtifactId() + "&v=" + getVersion() + classifier + "&e="
				+ getPackaging();
	}

	public String getNexusOptions() {
		String classifier = "";
		if (getClassifier() != null)
			classifier = "&c=" + getClassifier();
		return "r=" + getRepoId() + "&g=" + getGroupId() + "&a="
				+ getArtifactId() + "&v=" + getVersion() + classifier + "&e="
				+ getPackaging();
	}

	@Override
	public int compareTo(Artifact o) {
		return (getGroupId() + "_" + getArtifactId() + "_" + getVersion())
				.compareTo(o.getGroupId() + "_" + o.getArtifactId() + "_"
						+ o.getVersion());
	}

	public int hashCode() {
		return (getGroupId() + "_" + getArtifactId() + "_" + getVersion())
				.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof Artifact) {
			Artifact art = (Artifact) obj;
			return (getGroupId() + "_" + getArtifactId() + "_" + getVersion())
					.equals(art.getGroupId() + "_" + art.getArtifactId() + "_"
							+ art.getVersion());
		}
		return false;
	}

}
