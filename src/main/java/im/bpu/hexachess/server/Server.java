package im.bpu.hexachess.server;

import im.bpu.hexachess.Config;
import im.bpu.hexachess.dao.AchievementDAO;
import im.bpu.hexachess.dao.PlayerDAO;
import im.bpu.hexachess.dao.PuzzleDAO;
import im.bpu.hexachess.dao.SettingsDAO;
import im.bpu.hexachess.dao.TournamentDAO;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Puzzle;
import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.entity.Tournament;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Key;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;

public class Server {
	private static final int PORT = Integer.parseInt(Config.get("PORT", "8800"));
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final Map<String, String> CHALLENGES = new ConcurrentHashMap<>();
	private static final Map<String, String> GAMES = new ConcurrentHashMap<>();
	private static final Map<String, String> MOVES = new ConcurrentHashMap<>();
	static {
		MAPPER.registerModule(new JavaTimeModule());
	}
	private static final Key KEY = Keys.hmacShaKeyFor(
		Config.get("KEY", "hexachess_secret_key_with_a_minimum_of_32_bytes").getBytes());
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext("/api/login", new LoginHandler());
		server.createContext("/api/register", new RegisterHandler());
		server.createContext("/api/settings", new SettingsHandler());
		server.createContext("/api/search", new SearchHandler());
		server.createContext("/api/profile", new ProfileHandler());
		server.createContext("/api/achievements", new AchievementsHandler());
		server.createContext("/api/puzzles", new PuzzlesHandler());
		server.createContext("/api/tournaments", new TournamentsHandler());
		server.createContext("/api/challenge", new ChallengeHandler());
		server.createContext("/api/sync", new SyncHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("HexaChess Server started on port " + PORT);
	}
	private static String auth(HttpExchange exchange) {
		String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer "))
			return null;
		String authToken = authHeader.substring(7);
		try {
			return Jwts.parser()
				.verifyWith((SecretKey) KEY)
				.build()
				.parseSignedClaims(authToken)
				.getPayload()
				.getSubject();
		} catch (Exception ignored) { // high-frequency polling operation
			return null;
		}
	}
	private static boolean isUserInGame(String handle, String gameId) {
		for (Entry<String, String> game : GAMES.entrySet()) {
			if (game.getValue().equals(gameId) && game.getKey().contains(handle)) {
				return true;
			}
		}
		return false;
	}
	static class LoginHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				ObjectNode jsonNode = MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				if (jsonNode == null || !jsonNode.has("handle") || !jsonNode.has("password")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				String handle = jsonNode.get("handle").asText();
				String password = jsonNode.get("password").asText();
				PlayerDAO playerDAO = new PlayerDAO();
				Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null && BCrypt.checkpw(password, player.getPasswordHash())) {
					player.setPasswordHash(null);
					player.setToken(Jwts.builder()
							.issuedAt(new Date())
							.signWith(KEY)
							.subject(handle)
							.compact());
					String response = MAPPER.writeValueAsString(player);
					sendResponse(exchange, 200, response);
				} else {
					sendResponse(exchange, 401, "Unauthorized");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class RegisterHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				Player player = MAPPER.readValue(exchange.getRequestBody(), Player.class);
				if (player == null) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				String handle = player.getHandle();
				String email = player.getEmail();
				String password = player.getPasswordHash();
				if (handle == null || handle.isEmpty() || handle.length() > 32 || email == null
					|| !email.contains("@") || !email.contains(".") || password == null
					|| password.length() < 8) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				PlayerDAO playerDAO = new PlayerDAO();
				if (playerDAO.getPlayerByHandle(handle) != null) {
					sendResponse(exchange, 409, "Conflict: Username taken");
					return;
				}
				String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
				player.setPasswordHash(passwordHash);
				player.setRating(1200);
				player.setVerified(false);
				player.setJoinedAt(LocalDateTime.now());
				playerDAO.create(player);
				sendResponse(exchange, 200, "OK");
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class SettingsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				try {
					String query = exchange.getRequestURI().getQuery();
					if (query == null || !query.contains("playerId=")) {
						sendResponse(exchange, 400, "Bad Request");
						return;
					}
					String playerId = query.split("=")[1];
					PlayerDAO playerDAO = new PlayerDAO();
					Player player = playerDAO.read(playerId);
					if (player == null || !player.getHandle().equals(handle)) {
						sendResponse(exchange, 403, "Forbidden");
						return;
					}
					SettingsDAO settingsDAO = new SettingsDAO();
					Settings settings = settingsDAO.read(playerId);
					if (settings == null) {
						settings = new Settings(playerId);
						settingsDAO.create(settings);
					}
					String response = MAPPER.writeValueAsString(settings);
					sendResponse(exchange, 200, response);
				} catch (Exception exception) {
					exception.printStackTrace();
					sendResponse(exchange, 500, "Internal Server Error");
				}
			} else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				try {
					Settings settings = MAPPER.readValue(exchange.getRequestBody(), Settings.class);
					if (settings == null) {
						sendResponse(exchange, 400, "Bad Request");
						return;
					}
					String playerId = settings.getPlayerId();
					PlayerDAO playerDAO = new PlayerDAO();
					Player player = playerDAO.read(playerId);
					if (player == null || !player.getHandle().equals(handle)) {
						sendResponse(exchange, 403, "Forbidden");
						return;
					}
					SettingsDAO settingsDAO = new SettingsDAO();
					settingsDAO.update(settings);
					sendResponse(exchange, 200, "OK");
				} catch (Exception exception) {
					exception.printStackTrace();
					sendResponse(exchange, 500, "Internal Server Error");
				}
			} else {
				sendResponse(exchange, 405, "Method Not Allowed");
			}
		}
	}
	static class SearchHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				String query = exchange.getRequestURI().getQuery();
				String handle = (query != null && query.contains("=")) ? query.split("=")[1] : "";
				PlayerDAO playerDAO = new PlayerDAO();
				List<Player> players = playerDAO.searchPlayers(handle);
				for (Player player : players) player.setPasswordHash(null);
				String response = MAPPER.writeValueAsString(players);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class ProfileHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				String query = exchange.getRequestURI().getQuery();
				if (query == null || !query.contains("handle=")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				String handle = query.split("=")[1];
				PlayerDAO playerDAO = new PlayerDAO();
				Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null) {
					player.setPasswordHash(null);
					String response = MAPPER.writeValueAsString(player);
					sendResponse(exchange, 200, response);
				} else {
					sendResponse(exchange, 404, "Not Found");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class AchievementsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				AchievementDAO achievementDAO = new AchievementDAO();
				List<Achievement> achievements = achievementDAO.readAll();
				String response = MAPPER.writeValueAsString(achievements);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class PuzzlesHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				PuzzleDAO puzzleDAO = new PuzzleDAO();
				List<Puzzle> puzzles = puzzleDAO.readAll();
				String response = MAPPER.writeValueAsString(puzzles);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class TournamentsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				TournamentDAO tournamentDAO = new TournamentDAO();
				List<Tournament> tournaments = tournamentDAO.readAll();
				String response = MAPPER.writeValueAsString(tournaments);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class ChallengeHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			ObjectNode jsonNode = MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
			if (jsonNode == null || !jsonNode.has("to")) {
				sendResponse(exchange, 400, "Bad Request");
				return;
			}
			String from = handle;
			String to = jsonNode.get("to").asText();
			CHALLENGES.put(from, to);
			if (from.equals(CHALLENGES.get(to))) {
				String gameId = GAMES.computeIfAbsent(from + "-" + to, key -> {
					byte[] bytes = new byte[9];
					SecureRandom rand = new SecureRandom();
					rand.nextBytes(bytes);
					return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(
						0, 11);
				});
				GAMES.put(to + "-" + from, gameId);
				sendResponse(exchange, 200, gameId);
			} else {
				sendResponse(exchange, 200, "Pending");
			}
		}
	}
	static class SyncHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				ObjectNode jsonNode = MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				if (jsonNode == null || !jsonNode.has("gameId") || !jsonNode.has("move")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				String gameId = jsonNode.get("gameId").asText();
				if (!isUserInGame(handle, gameId)) {
					sendResponse(exchange, 403, "Forbidden");
					return;
				}
				String move = jsonNode.get("move").asText();
				MOVES.put(gameId, move);
				sendResponse(exchange, 200, "OK");
			} else {
				String query = exchange.getRequestURI().getQuery();
				if (query == null || !query.contains("gameId=")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				String gameId = query.split("=")[1];
				if (!isUserInGame(handle, gameId)) {
					sendResponse(exchange, 403, "Forbidden");
					return;
				}
				String move = MOVES.getOrDefault(gameId, "");
				sendResponse(exchange, 200, move);
			}
		}
	}
	private static void sendResponse(HttpExchange exchange, int statusCode, String response)
		throws IOException {
		byte[] bytes = response.getBytes();
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(statusCode, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}
}