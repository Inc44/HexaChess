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
		final HttpRequest.Builder requestBuilder, final String endpoint) throws Exception {
		final String authToken = SettingsManager.authToken;
		if (authToken != null)
			requestBuilder.header("Authorization", "Bearer " + authToken);
		try {
			return sendRequest(requestBuilder, DEV_URL, endpoint);
		} catch (final HttpTimeoutException ignored) {
			throw ignored;
		} catch (final Exception developerException) {
			try {
				return sendRequest(requestBuilder, PROD_URL, endpoint);
			} catch (final HttpTimeoutException ignored) {
				throw ignored;
			} catch (final Exception productionException) {
				return null;
			}
		}
	}
	private static HttpResponse<String> sendRequest(final HttpRequest.Builder requestBuilder,
		final String baseUrl, final String endpoint) throws Exception {
		final HttpRequest request = requestBuilder.uri(URI.create(baseUrl + endpoint)).build();
		return CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
	}
	public static Player login(final String handle, final String password) {
		try {
			final ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("handle", handle);
			jsonNode.put("password", password);
			final String json = MAPPER.writeValueAsString(jsonNode);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, "/login");
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Player.class);
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean register(final Player player) {
		try {
			final String json = MAPPER.writeValueAsString(player);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, "/register");
			return response != null && response.statusCode() == 200;
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static Settings settings(final String playerId) {
		try {
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/settings?playerId=" + playerId);
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Settings.class);
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean settings(final Settings settings) {
		try {
			final String json = MAPPER.writeValueAsString(settings);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, "/settings");
			return response != null && response.statusCode() == 200;
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static List<Player> search(final String handle) {
		try {
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/search?handle=" + handle);
			if (response != null && response.statusCode() == 200)
				return List.of(MAPPER.readValue(response.body(), Player[].class));
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static Player profile(final String handle) {
		if (ROOT_HANDLE.equals(handle))
			return new Player(ROOT_ID, ROOT_HANDLE, ROOT_EMAIL, "", BASE_ELO, true, null);
		try {
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/profile?handle=" + handle);
			if (response != null && response.statusCode() == 200)
				return MAPPER.readValue(response.body(), Player.class);
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
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
	private static <T> List<T> fetch(final String endpoint, final Class<T[]> clazz) {
		try {
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, endpoint);
			if (response != null && response.statusCode() == 200)
				return List.of(MAPPER.readValue(response.body(), clazz));
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static String challenge(final String from, final String to) {
		try {
			final ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("from", from);
			jsonNode.put("to", to);
			final String json = MAPPER.writeValueAsString(jsonNode);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, "/challenge");
			return response != null ? response.body() : null;
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static void sendMove(final String gameId, final String move) {
		try {
			final ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("gameId", gameId);
			jsonNode.put("move", move);
			final String json = MAPPER.writeValueAsString(jsonNode);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			sendWithFallback(requestBuilder, "/sync");
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}
	public static String getMove(final String gameId) {
		try {
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/sync?gameId=" + gameId);
			return response != null ? response.body() : null;
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static void unlockAchievement(String achievementId) {
		try {
			String playerId = SettingsManager.playerId;
			if (playerId == null)
				return;
			ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("playerId", playerId);
			jsonNode.put("achievementId", achievementId);
			String json = MAPPER.writeValueAsString(jsonNode);
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			sendWithFallback(requestBuilder, "/unlock");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	public static List<Player> getLeaderboard() {
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response = sendWithFallback(requestBuilder, "/leaderboard");
			if (response != null && response.statusCode() == 200) {
				return List.of(MAPPER.readValue(response.body(), Player[].class));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static List<Achievement> achievementsForPlayer(String playerId) {
		if (playerId == null)
			return Collections.emptyList();
		try {
			HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder().GET().timeout(TIMEOUT_DURATION);
			HttpResponse<String> response =
				sendWithFallback(requestBuilder, "/achievements?playerId=" + playerId);
			if (response != null && response.statusCode() == 200) {
				return List.of(MAPPER.readValue(response.body(), Achievement[].class));
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return Collections.emptyList();
	}
	public static boolean joinTournament(final String tournamentId) {
		try {
			final ObjectNode jsonNode = MAPPER.createObjectNode();
			jsonNode.put("tournamentId", tournamentId);
			final String json = MAPPER.writeValueAsString(jsonNode);
			final HttpRequest.Builder requestBuilder =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(json))
					.timeout(TIMEOUT_DURATION);
			final HttpResponse<String> response = sendWithFallback(requestBuilder, "/join");
			return response != null && response.statusCode() == 200;
		} catch (final HttpTimeoutException ignored) {
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
		return false;
	}
	public static List<Player> participants(final String tournamentId) {
		return fetch("/participants?id=" + tournamentId, Player[].class);
	}



	public static boolean updateProfile(String currentPassword, String email, String location, String avatar, String newPassword) {
		try {
			ObjectNode json = MAPPER.createObjectNode();
			json.put("currentPassword", currentPassword);
			json.put("email", email);
			json.put("location", location);
			json.put("avatar", avatar);
			if (newPassword != null && !newPassword.isEmpty()) {
				json.put("newPassword", newPassword);
			}

			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString(json.toString()))
				.header("Content-Type", "application/json");

			HttpResponse<String> response = sendWithFallback(requestBuilder, "/profile/update");
			return response.statusCode() == 200;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}