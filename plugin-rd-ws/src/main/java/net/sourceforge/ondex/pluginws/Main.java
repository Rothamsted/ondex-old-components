package net.sourceforge.ondex.pluginws;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.HttpServer;

import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;

public class Main {

	public static final String NEXUS_ONDEX_SERVICE = "http://ondex.rothamsted.ac.uk/nexus/service";

	private static int getPort(int defaultPort) {
		String port = System.getProperty("jersey.test.port");
		if (null != port) {
			try {
				return Integer.parseInt(port);
			} catch (NumberFormatException e) {
			}
		}
		return defaultPort;
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://localhost/ondex-plugins/")
				.port(getPort(8085)).build();
	}

	public static final URI BASE_URI = getBaseURI();

	protected static HttpServer startServer() throws IOException {
		System.out.println("Starting grizzly...");
		ResourceConfig rc = new PackagesResourceConfig(
				"net.sourceforge.ondex.pluginws.resources");
		return GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
	}

	public static void main(String[] args) throws Exception {
		HttpServer httpServer = startServer();
		System.out.println(String.format(
				"Jersey app started with WADL available at "
						+ "%sapplication.wadl\nTry out %shelloworld", BASE_URI,
				BASE_URI));

		// busy waiting until HttpServer shutdown
		Object dummy = new Object();
		synchronized (dummy) {
			while (httpServer.isStarted()) {
				dummy.wait(100);
			}
		}
	}
}
