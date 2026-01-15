package im.bpu.hexachess.network;

import im.bpu.hexachess.Config;
import im.bpu.hexachess.SettingsManager;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Puzzle;
import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.entity.Tournament;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class API {
	private static final String DEFAULT_DEV_URL = "http://localhost:8800/api";
	private static final String DEFAULT_PROD_URL = "https://hexachess.bpu.im/api";
	private static final String DEV_URL = Config.get("DEV_URL", DEFAULT_DEV_URL);
	private static final String PROD_URL = Config.get("PROD_URL", DEFAULT_PROD_URL);
	private static final String ROOT_HANDLE = "root";
	private static final String ROOT_ID = "00000000000";
	private static final String ROOT_EMAIL = "root@localhost";
	private static final int BASE_ELO = 1200;
	private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(6);
	private static final HttpClient CLIENT =
		HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	private static final ObjectMapper MAPPER = new ObjectMapper();
	static {
		MAPPER.registerModule(new JavaTimeModule());
	}
	private static HttpResponse<String> sendWithFallback(
		HttpRequest.Builder requestBuilder, String endpoint) throws Exception {
		String authToken = SettingsManager.authToken;
		if (authToken != null)
			requestBuilder.header("Authorization", "Bearer " + authToken);
		try {
			return sendRequest(requestBuilder, DEV_URL, endpoint);
		} catch (HttpTimeoutException ignored) {
			throw ignored;
		} catch (Exception developerException) {
			try {
				return sendRequest(requestBuilder, PROD_URL, endpoint);
			} catch (HttpTimeoutException ignored) {
				throw ignored;
			} catch (Exception productionException) {
				return null;
			}
		}
	}
	private static HttpResponse<String> sendRequest(
		HttpRequest.Builder requestBuilder, String baseUrl, String endpoint) throws Exception {
		HttpRequest request = requestBuilder.uri(URI.create(baseUrl + endpoint)).build();
		return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
	}
	public static Player login(String handle, String password) {
		try {
			ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("handle", handle);
			jsonNode.put("password", password);
			String json = MAPPER.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/login");
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Player.class);
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean register(Player player) {
		try {
			String json = MAPPER.writeValueAsString(player);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/register");
			return response != null && response.statusCode() == 200;
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static Settings settings(String playerId) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/settings?playerId=" + playerId);
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Settings.class);
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean settings(Settings settings) {
		try {
			String json = MAPPER.writeValueAsString(settings);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/settings");
			return response != null && response.statusCode() == 200;
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static List<Player> search(String handle) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/search?handle=" + handle);
			if (response != null && response.statusCode() == 200)
				return List.of(MAPPER.readValue(response.body(), Player[].class));
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static Player profile(String handle) {
		if (ROOT_HANDLE.equals(handle))
			return new Player(ROOT_ID, ROOT_HANDLE, ROOT_EMAIL, "", BASE_ELO, true, null);
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/profile?handle=" + handle);
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Player.class);
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static List<Achievement> achievements() {
		return fetch("/achievements", Achievement[].class);
	}
	public static List<Puzzle> puzzles() {
		return fetch("/puzzles", Puzzle[].class);
	}
	public static List<Tournament> tournaments() {
		return fetch("/tournaments", Tournament[].class);
	}
	private static <T> List<T> fetch(String endpoint, Class<T[]> clazz) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, endpoint);
			if (response != null && response.statusCode() == 200)
				return List.of(MAPPER.readValue(response.body(), clazz));
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static String challenge(String from, String to) {
		try {
			ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("from", from);
			jsonNode.put("to", to);
			String json = MAPPER.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/challenge");
			return response != null ? response.body() : null;
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static void sendMove(String gameId, String move) {
		try {
			ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("gameId", gameId);
			jsonNode.put("move", move);
			String json = MAPPER.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			sendWithFallback(requestBuilder, "/sync");
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	public static String getMove(String gameId) {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/sync?gameId=" + gameId);
			return response != null ? response.body() : null;
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean joinTournament(String tournamentId) {
		try {
			ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("tournamentId", tournamentId);
			String json = MAPPER.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/join");
			return response != null && response.statusCode() == 200;
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static List<Player> getTournamentParticipants(String tournamentId) {
		try {
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET();
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/participants?id=" + tournamentId);
			if (response.statusCode() == 200) {
				return List.of(MAPPER.readValue(response.body(), Player[].class));
			}
		} catch (HttpTimeoutException ignored) {
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
}