package im.bpu.hexachess.network;

import im.bpu.hexachess.entity.Player;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class API {
	private static final String BASE_URL = "http://localhost:8800/api";
	private static final HttpClient client = HttpClient.newHttpClient();
	private static final ObjectMapper mapper = new ObjectMapper();
	static {
		mapper.registerModule(new JavaTimeModule());
	}
	public static Player login(String handle, String password) {
		try {
			ObjectNode jsonNode = mapper.createObjectNode();
			jsonNode.put("handle", handle);
			jsonNode.put("password", password);
			HttpRequest request =
				HttpRequest.newBuilder()
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(jsonNode)))
					.timeout(Duration.ofSeconds(6))
					.uri(URI.create(BASE_URL + "/login"))
					.build();
			HttpResponse<String> response =
				client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() == 200) {
				return mapper.readValue(response.body(), Player.class);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	public static boolean register(Player player) {
		try {
			String json = mapper.writeValueAsString(player);
			HttpRequest request = HttpRequest.newBuilder()
									  .header("Content-Type", "application/json")
									  .POST(HttpRequest.BodyPublishers.ofString(json))
									  .timeout(Duration.ofSeconds(6))
									  .uri(URI.create(BASE_URL + "/register"))
									  .build();
			HttpResponse<String> response =
				client.send(request, HttpResponse.BodyHandlers.ofString());
			return response.statusCode() == 200;
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}
}