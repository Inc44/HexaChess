package im.bpu.hexachess.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import im.bpu.hexachess.Config;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Puzzle;
import im.bpu.hexachess.entity.Tournament;

public class API {
	private static final String DEV_URL = Config.get("DEV_URL", "http://localhost:8800/api");
	private static final String PROD_URL = Config.get("PROD_URL", "https://hexachess.bpu.im/api");
	private static final HttpClient client =
		HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.registerModule(new JavaTimeModule());
	}
	private static HttpResponse<String> sendWithFallback(
		HttpRequest.Builder requestBuilder, String endpoint) throws Exception {
		try {
			return sendRequest(requestBuilder, DEV_URL, endpoint);
		} catch (Exception primaryException) {
			return sendRequest(requestBuilder, PROD_URL, endpoint);
		}
	}
	private static HttpResponse<String> sendRequest(
		HttpRequest.Builder requestBuilder, String baseUrl, String endpoint) throws Exception {
		HttpRequest request = requestBuilder.uri(URI.create(baseUrl + endpoint)).build();
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	public static Player login(String handle, String password) {
		try {
			ObjectNode jsonNode = mapper.createObjectNode();
			jsonNode.put("handle", handle);
			jsonNode.put("password", password);
			String json = mapper.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(Duration.ofSeconds(6));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/login");
			if (response.statusCode() == 200)
				return mapper.readValue(response.body(), Player.class);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean register(Player player) {
		try {
			String json = mapper.writeValueAsString(player);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(Duration.ofSeconds(6));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/register");
			return response.statusCode() == 200;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
	public static List<Player> search(String handle) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(6));
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/search?handle=" + handle);
			if (response.statusCode() == 200)
				return List.of(mapper.readValue(response.body(), Player[].class));
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static Player profile(String handle) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(6));
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/profile?handle=" + handle);
			if (response.statusCode() == 200)
				return mapper.readValue(response.body(), Player.class);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static String challenge(String from, String to) {
		try {
			ObjectNode jsonNode = mapper.createObjectNode();
			jsonNode.put("from", from);
			jsonNode.put("to", to);
			String json = mapper.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(Duration.ofSeconds(2));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/challenge");
			return response.body();
		} catch (Exception ignored) { // high-frequency polling operation
			return null;
		}
	}
	public static void sendMove(String gameId, String move) {
		try {
			ObjectNode jsonNode = mapper.createObjectNode();
			jsonNode.put("gameId", gameId);
			jsonNode.put("move", move);
			String json = mapper.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(Duration.ofSeconds(6));
			sendWithFallback(requestBuilder, "/sync");
		} catch (Exception ignored) { // high-frequency polling operation
		}
	}
	public static String getMove(String gameId) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(2));
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/sync?gameId=" + gameId);
			return response.body();
		} catch (Exception ignored) { // high-frequency polling operation
			return null;
		}
	}
	public static List<Achievement> getAchievements() {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(6));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/achievements");
			if (response.statusCode() == 200)
				return List.of(mapper.readValue(response.body(), Achievement[].class));
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	public static List<Puzzle> getPuzzles() {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(6));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/puzzles");
			if (response.statusCode() == 200)
				return List.of(mapper.readValue(response.body(), Puzzle[].class));
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}

	public static List<Tournament> getTournaments() {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(Duration.ofSeconds(6));
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/tournaments");
			if (response.statusCode() == 200)
				return List.of(mapper.readValue(response.body(), Tournament[].class));
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
}