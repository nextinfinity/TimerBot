package net.nextinfinity.timerbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;

import java.io.IOException;
import java.net.InetSocketAddress;

/** Local readiness check backed by JDA's live Discord gateway state. */
public final class HealthCheck {
	private HealthCheck() {}

	static void start(JDA jda) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
		server.createContext("/health", exchange -> {
			try {
				exchange.sendResponseHeaders(jda.getStatus() == JDA.Status.CONNECTED ? 200 : 503, -1);
			} finally {
				exchange.close();
			}
		});
		server.start();
	}
}
