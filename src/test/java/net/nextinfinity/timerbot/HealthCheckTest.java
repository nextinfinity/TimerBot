package net.nextinfinity.timerbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckTest {
	@Test
	void followsLiveGatewayStateIncludingReconnects() throws IOException {
		AtomicReference<JDA.Status> status = new AtomicReference<>(JDA.Status.INITIALIZING);
		HttpServer server = HealthCheck.start(status::get, 0);
		try {
			assertTrue(server.getAddress().getAddress().isLoopbackAddress());
			URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/health");
			for (JDA.Status state : JDA.Status.values()) {
				status.set(state);
				assertEquals(state == JDA.Status.CONNECTED, HealthCheck.isHealthy(endpoint), state.name());
			}
			status.set(JDA.Status.CONNECTED);
			assertTrue(HealthCheck.isHealthy(endpoint), "Recovery should become healthy again");
		} finally {
			server.stop(0);
		}
	}

	@Test
	void failsWhenServerIsUnavailable() throws IOException {
		HttpServer server = HealthCheck.start(() -> JDA.Status.CONNECTED, 0);
		URI endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/health");
		server.stop(0);
		assertThrows(IOException.class, () -> HealthCheck.isHealthy(endpoint));
	}
}
