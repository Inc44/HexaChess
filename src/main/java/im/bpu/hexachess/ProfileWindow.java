package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class ProfileWindow {
	private static final String BASE_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final Map<String, String> COUNTRIES = new HashMap<>();
	static {
		COUNTRIES.put("cn", "China");
		COUNTRIES.put("de", "Germany");
		COUNTRIES.put("es", "Spain");
		COUNTRIES.put("fr", "France");
		COUNTRIES.put("it", "Italy");
		COUNTRIES.put("jp", "Japan");
		COUNTRIES.put("kr", "South Korea");
		COUNTRIES.put("pl", "Poland");
		COUNTRIES.put("ro", "Romania");
		COUNTRIES.put("ru", "Russia");
		COUNTRIES.put("ua", "Ukraine");
		COUNTRIES.put("us", "United States");
	}
	@FXML private ImageView avatarIcon;
	@FXML private Label usernameLabel;
	@FXML private Region countryFlagIcon;
	@FXML private Label ratingLabel;
	@FXML private Label locationLabel;
	@FXML private Label joinedAtLabel;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		String handle = Settings.userHandle;
		Player player = API.profile(handle);
		if (player == null)
			return;
		String username = player.getHandle();
		int rating = player.getRating();
		String location = player.getLocation();
		LocalDateTime joinedAt = player.getJoinedAt();
		String avatarUrl = (player.getAvatar() != null && !player.getAvatar().isEmpty())
			? player.getAvatar()
			: BASE_URL;
		usernameLabel.setText(username);
		ratingLabel.setText("Rating: " + rating);
		if (location != null && !location.isEmpty()) {
			String country = COUNTRIES.getOrDefault(location, location);
			locationLabel.setText(country);
			countryFlagIcon.getStyleClass().add("country-" + location);
		}
		if (joinedAt != null) {
			joinedAtLabel.setText(
				"Joined: " + joinedAt.format(DateTimeFormatter.ofPattern("MMM d yyyy")));
		}
		avatarIcon.setImage(new Image(avatarUrl, true));
	}
	@FXML
	private void openMain() {
		try {
			FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
			mainWindowLoader.setController(new MainWindow());
			Parent root = mainWindowLoader.load();
			backButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}