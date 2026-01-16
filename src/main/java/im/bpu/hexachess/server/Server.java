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
	public static void main(final String[] args) throws IOException {
		final HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext("/api/login", new LoginHandler());
		server.createContext("/api/register", new RegisterHandler());
		server.createContext("/api/settings", new SettingsHandler());
		server.createContext("/api/search", new SearchHandler());
		server.createContext("/api/profile", new ProfileHandler());
		server.createContext("/api/leaderboard", new LeaderboardHandler());
		server.createContext("/api/achievements", new AchievementsHandler());
		server.createContext("/api/puzzles", new PuzzlesHandler());
		server.createContext("/api/tournaments", new TournamentsHandler());
		server.createContext("/api/challenge", new ChallengeHandler());
		server.createContext("/api/sync", new SyncHandler());
		server.createContext("/api/tournaments/join", new TournamentJoinHandler());
		server.createContext("/api/tournaments/participants", new TournamentParticipantsHandler());
		server.createContext("/api/unlock", new UnlockHandler());
		server.createContext("/api/join", new JoinHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("HexaChess Server started on port " + PORT);
	}
	private static String auth(final HttpExchange exchange) {
		final String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer "))
			return null;
		final String authToken = authHeader.substring(7);
		try {
			return Jwts.parser()
				.verifyWith((SecretKey) KEY)
				.build()
				.parseSignedClaims(authToken)
				.getPayload()
				.getSubject();
		} catch (final Exception ignored) { // high-frequency polling operation
			return null;
		}
	}
	private static boolean isUserInGame(final String handle, final String gameId) {
		for (final Entry<String, String> game : GAMES.entrySet()) {
			if (game.getValue().equals(gameId) && game.getKey().contains(handle)) {
				return true;
			}
		}
		return false;
	}
	static class LoginHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final ObjectNode jsonNode =
					MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				if (jsonNode == null || !jsonNode.has("handle") || !jsonNode.has("password")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				final String handle = jsonNode.get("handle").asText();
				final String password = jsonNode.get("password").asText();
				final PlayerDAO playerDAO = new PlayerDAO();
				final Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null && BCrypt.checkpw(password, player.getPasswordHash())) {
					player.setPasswordHash(null);
					player.setToken(Jwts.builder()
							.issuedAt(new Date())
							.signWith(KEY)
							.subject(handle)
							.compact());
					final String response = MAPPER.writeValueAsString(player);
					sendResponse(exchange, 200, response);
				} else {
					sendResponse(exchange, 401, "Unauthorized");
				}
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class RegisterHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final Player player = MAPPER.readValue(exchange.getRequestBody(), Player.class);
				if (player == null) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				final String handle = player.getHandle();
				final String email = player.getEmail();
				final String password = player.getPasswordHash();
				boolean isWeakPassword = true;
				if (password != null && password.length() >= 8) {
					boolean hasDigit = false;
					boolean hasLowerCase = false;
					boolean hasUpperCase = false;
					boolean hasSpecialCharacter = false;
					for (int i = 0; i < password.length(); i++) {
						final char character = password.charAt(i);
						if (Character.isDigit(character))
							hasDigit = true;
						else if (Character.isLowerCase(character))
							hasLowerCase = true;
						else if (Character.isUpperCase(character))
							hasUpperCase = true;
						else if ("@#$%^&+=!".indexOf(character) != -1)
							hasSpecialCharacter = true;
					}
					if (hasDigit && hasLowerCase && hasUpperCase && hasSpecialCharacter)
						isWeakPassword = false;
				}
				if (isWeakPassword) {
					sendResponse(exchange, 422,
						"Weak password. Requires at least 8 characters, 1 digit, 1 lowercase "
							+ "letter, 1 uppercase letter, and 1 special character.");
					return;
				}
				if (handle == null || handle.isEmpty() || handle.length() > 32 || email == null
					|| !email.contains("@") || !email.contains(".")) {
					sendResponse(exchange, 422, "Unprocessable Content");
					return;
				}
				final PlayerDAO playerDAO = new PlayerDAO();
				if (playerDAO.getPlayerByHandle(handle) != null) {
					sendResponse(exchange, 409, "Conflict: Username taken");
					return;
				}
				final String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
				player.setPasswordHash(passwordHash);
				player.setRating(1200);
				player.setVerified(false);
				player.setJoinedAt(LocalDateTime.now());
				playerDAO.create(player);
				sendResponse(exchange, 200, "OK");
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class SettingsHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			final String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				try {
					final String query = exchange.getRequestURI().getQuery();
					if (query == null || !query.contains("playerId=")) {
						sendResponse(exchange, 400, "Bad Request");
						return;
					}
					final String playerId = query.split("=")[1];
					final PlayerDAO playerDAO = new PlayerDAO();
					final Player player = playerDAO.read(playerId);
					if (player == null || !player.getHandle().equals(handle)) {
						sendResponse(exchange, 403, "Forbidden");
						return;
					}
					final SettingsDAO settingsDAO = new SettingsDAO();
					Settings settings = settingsDAO.read(playerId);
					if (settings == null) {
						settings = new Settings(playerId);
						settingsDAO.create(settings);
					}
					final String response = MAPPER.writeValueAsString(settings);
					sendResponse(exchange, 200, response);
				} catch (final Exception exception) {
					exception.printStackTrace();
					sendResponse(exchange, 500, "Internal Server Error");
				}
			} else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				try {
					final Settings settings =
						MAPPER.readValue(exchange.getRequestBody(), Settings.class);
					if (settings == null) {
						sendResponse(exchange, 400, "Bad Request");
						return;
					}
					final String playerId = settings.getPlayerId();
					final PlayerDAO playerDAO = new PlayerDAO();
					final Player player = playerDAO.read(playerId);
					if (player == null || !player.getHandle().equals(handle)) {
						sendResponse(exchange, 403, "Forbidden");
						return;
					}
					final SettingsDAO settingsDAO = new SettingsDAO();
					settingsDAO.update(settings);
					sendResponse(exchange, 200, "OK");
				} catch (final Exception exception) {
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
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final String query = exchange.getRequestURI().getQuery();
				final String handle =
					(query != null && query.contains("=")) ? query.split("=")[1] : "";
				final PlayerDAO playerDAO = new PlayerDAO();
				final List<Player> players = playerDAO.searchPlayers(handle);
				for (final Player player : players) player.setPasswordHash(null);
				final String response = MAPPER.writeValueAsString(players);
				sendResponse(exchange, 200, response);
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class ProfileHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final String query = exchange.getRequestURI().getQuery();
				if (query == null || !query.contains("handle=")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				final String handle = query.split("=")[1];
				final PlayerDAO playerDAO = new PlayerDAO();
				final Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null) {
					player.setPasswordHash(null);
					final String response = MAPPER.writeValueAsString(player);
					sendResponse(exchange, 200, response);
				} else {
					sendResponse(exchange, 404, "Not Found");
				}
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class AchievementsHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				String query = exchange.getRequestURI().getQuery();
				AchievementDAO achievementDAO = new AchievementDAO();
				List<Achievement> achievements;
				if (query != null && query.contains("playerId=")) {
					String playerId = query.split("playerId=")[1];
					achievements = achievementDAO.readAllForPlayer(playerId);
				} else {
					achievements = achievementDAO.readAll();
				}
				String response = MAPPER.writeValueAsString(achievements);
				final AchievementDAO achievementDAO = new AchievementDAO();
				final List<Achievement> achievements = achievementDAO.readAll();
				final String response = MAPPER.writeValueAsString(achievements);
				sendResponse(exchange, 200, response);
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class PuzzlesHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final PuzzleDAO puzzleDAO = new PuzzleDAO();
				final List<Puzzle> puzzles = puzzleDAO.readAll();
				final String response = MAPPER.writeValueAsString(puzzles);
				sendResponse(exchange, 200, response);
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class TournamentsHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final TournamentDAO tournamentDAO = new TournamentDAO();
				final List<Tournament> tournaments = tournamentDAO.readAll();
				final String response = MAPPER.writeValueAsString(tournaments);
				sendResponse(exchange, 200, response);
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class ChallengeHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			final String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			final ObjectNode jsonNode =
				MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
			if (jsonNode == null || !jsonNode.has("to")) {
				sendResponse(exchange, 400, "Bad Request");
				return;
			}
			final String from = handle;
			final String to = jsonNode.get("to").asText();
			CHALLENGES.put(from, to);
			if (from.equals(CHALLENGES.get(to))) {
				final String gameId = GAMES.computeIfAbsent(from + "-" + to, key -> {
					final byte[] bytes = new byte[9];
					final SecureRandom rand = new SecureRandom();
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
	static class TournamentJoinHandler implements HttpHandler {
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
			try {
				ObjectNode json = MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				String tournamentId = json.get("tournamentId").asText();
				PlayerDAO playerDAO = new PlayerDAO();
				Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null) {
					TournamentDAO tournamentDAO = new TournamentDAO();
					if (tournamentDAO.addParticipant(tournamentId, player.getPlayerId())) {
						sendResponse(exchange, 200, "Joined");
					} else {
						sendResponse(exchange, 409, "Already joined or Error");
					}
				} else {
					sendResponse(exchange, 404, "Player not found");
				}
			} catch (Exception e) {
				e.printStackTrace();
				sendResponse(exchange, 500, "Internal Error");
			}
		}
	}
	static class TournamentParticipantsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				String query = exchange.getRequestURI().getQuery();
				if (query == null || !query.contains("id=")) {
					sendResponse(exchange, 400, "Missing tournament ID");
					return;
				}
				String tournamentId = query.split("=")[1];
				TournamentDAO dao = new TournamentDAO();
				List<Player> players = dao.getParticipants(tournamentId);
				for (Player player : players) {
					player.setEmail(null);
					player.setPasswordHash(null);
				}
				String response = MAPPER.writeValueAsString(players);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class SyncHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			final String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				final ObjectNode jsonNode =
					MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				if (jsonNode == null || !jsonNode.has("gameId") || !jsonNode.has("move")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				final String gameId = jsonNode.get("gameId").asText();
				if (!isUserInGame(handle, gameId)) {
					sendResponse(exchange, 403, "Forbidden");
					return;
				}
				final String move = jsonNode.get("move").asText();
				MOVES.put(gameId, move);
				sendResponse(exchange, 200, "OK");
			} else {
				final String query = exchange.getRequestURI().getQuery();
				if (query == null || !query.contains("gameId=")) {
					sendResponse(exchange, 400, "Bad Request");
					return;
				}
				final String gameId = query.split("=")[1];
				if (!isUserInGame(handle, gameId)) {
					sendResponse(exchange, 403, "Forbidden");
					return;
				}
				final String move = MOVES.getOrDefault(gameId, "");
				sendResponse(exchange, 200, move);
			}
		}
	}
	static class JoinHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			final String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				final ObjectNode jsonNode =
					MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				final String tournamentId = jsonNode.get("tournamentId").asText();
				final PlayerDAO playerDAO = new PlayerDAO();
				final Player player = playerDAO.getPlayerByHandle(handle);
				if (player != null) {
					final String playerId = player.getPlayerId();
					final TournamentDAO tournamentDAO = new TournamentDAO();
					if (tournamentDAO.addParticipant(tournamentId, playerId)) {
						sendResponse(exchange, 200, "OK");
					} else {
						sendResponse(exchange, 409, "Conflict: Already Joined");
					}
				} else {
					sendResponse(exchange, 404, "Player Not Found");
				}
			} catch (final Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	private static void sendResponse(final HttpExchange exchange, final int statusCode,
		final String response) throws IOException {
		final byte[] bytes = response.getBytes();
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(statusCode, bytes.length);
		try (final OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}
	static class UnlockHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			String handle = auth(exchange);
			if (handle == null) {
				sendResponse(exchange, 401, "Unauthorized");
				return;
			}
			try {
				ObjectNode jsonNode = MAPPER.readValue(exchange.getRequestBody(), ObjectNode.class);
				if (jsonNode == null || !jsonNode.has("playerId")
					|| !jsonNode.has("achievementId")) {
					sendResponse(exchange, 400, "Bad Request: Missing IDs");
					return;
				}
				String playerId = jsonNode.get("playerId").asText();
				String achievementId = jsonNode.get("achievementId").asText();
				AchievementDAO dao = new AchievementDAO();
				dao.unlock(playerId, achievementId);
				System.out.println("Succès débloqué pour " + handle + " : " + achievementId);
				sendResponse(exchange, 200, "OK");
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
	static class LeaderboardHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				PlayerDAO playerDAO = new PlayerDAO();
				List<Player> players = playerDAO.getLeaderboard();
				for (Player player : players) {
					player.setPasswordHash(null);
				}
				String response = MAPPER.writeValueAsString(players);
				sendResponse(exchange, 200, response);
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 500, "Internal Server Error");
			}
		}
	}
}