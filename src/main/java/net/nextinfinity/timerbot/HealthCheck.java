package net.nextinfinity.timerbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.function.Supplier;

/** Local readiness check backed by JDA's live Discord gateway state. */
public final class HealthCheck {
	private HealthCheck() {}

	static HttpServer start(Supplier<JDA.Status> status, int port) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
		server.createContext("/health", exchange -> {
			try {
				exchange.sendResponseHeaders(status.get() == JDA.Status.CONNECTED ? 200 : 503, -1);
			} finally {
				exchange.close();
			}
		});
		server.start();
		return server;
	}

	static boolean isHealthy(URI endpoint) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) endpoint.toURL().openConnection();
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(2000);
		connection.setInstanceFollowRedirects(false);
		try {
			return connection.getResponseCode() == 200;
		} finally {
			connection.disconnect();
		}
	}

	/** Docker probe: query the running bot, without opening another Discord session. */
	public static void main(String[] args) {
		try {
			System.exit(isHealthy(URI.create("http://127.0.0.1:8080/health")) ? 0 : 1);
		} catch (IOException exception) {
			System.err.println("Bot healthcheck failed: " + exception.getMessage());
			System.exit(1);
		}
	}
}
