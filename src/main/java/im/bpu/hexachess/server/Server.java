package im.bpu.hexachess.server;

import im.bpu.hexachess.dao.PlayerDAO;
import im.bpu.hexachess.entity.Player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;

public class Server {
	private static final int PORT = 8800;
	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.registerModule(new JavaTimeModule());
	}
	private static final Key KEY =
		Keys.hmacShaKeyFor("HexaChess Secret Key with a minimum of 32 bytes".getBytes());
	public static void main(String[] args) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
		server.createContext("/api/login", new LoginHandler());
		server.createContext("/api/register", new RegisterHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("HexaChess Server started on port " + PORT);
	}
	static class LoginHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed");
				return;
			}
			try {
				ObjectNode jsonNode = mapper.readValue(exchange.getRequestBody(), ObjectNode.class);
				String handle = jsonNode.get("handle").asText();
				String password = jsonNode.get("password").asText();
				PlayerDAO dao = new PlayerDAO();
				Player p = dao.getPlayerByHandle(handle);
				dao.close();
				if (p != null && BCrypt.checkpw(password, p.getPasswordHash())) {
					p.setPasswordHash(null);
					p.setToken(Jwts.builder()
							.issuedAt(new Date())
							.signWith(KEY)
							.subject(handle)
							.compact());
					String response = mapper.writeValueAsString(p);
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
				Player newPlayer = mapper.readValue(exchange.getRequestBody(), Player.class);
				String pass = newPlayer.getPasswordHash();
				String passwordHash = BCrypt.hashpw(pass, BCrypt.gensalt());
				newPlayer.setPasswordHash(passwordHash);
				newPlayer.setRating(1200);
				newPlayer.setVerified(false);
				newPlayer.setJoinedAt(LocalDateTime.now());
				PlayerDAO dao = new PlayerDAO();
				dao.create(newPlayer);
				dao.close();
				sendResponse(exchange, 200, "OK");
			} catch (Exception exception) {
				exception.printStackTrace();
				sendResponse(exchange, 409, "Conflict: Username taken or server error");
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