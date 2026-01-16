package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import static im.bpu.hexachess.Main.loadWindow;

public class ProfileWindow {
	private static final String AVATAR_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final String FLAGS_URL =
		"https://www.chess.com/bundles/web/images/sprites/flags-128.png";
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d yyyy");
	public static String targetHandle;
	@FXML private HBox profileItem;
	@FXML private ImageView avatarIcon;
	@FXML private Label handleLabel;
	@FXML private Region countryFlagIcon;
	@FXML private Label ratingLabel;
	@FXML private Label locationLabel;
	@FXML private Label joinedAtLabel;
	@FXML private Button backButton;
	@FXML private Button editButton;
	@FXML
	private void initialize() {
		final String handle = targetHandle != null ? targetHandle : SettingsManager.userHandle;
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final Player player = API.profile(handle);
			final File avatarFile;
			if (player == null) {
				final String avatarFileName = AVATAR_URL.substring(AVATAR_URL.lastIndexOf('/') + 1);
				avatarFile = CacheManager.save("avatars", avatarFileName, AVATAR_URL);
			} else {
				final String avatarUrl =
					(player.getAvatar() != null && !player.getAvatar().isEmpty())
					? player.getAvatar()
					: AVATAR_URL;
				avatarFile = CacheManager.save("avatars", handle, avatarUrl);
			}
			final Image avatarImage = new Image(avatarFile.toURI().toString());
			final String flagsFileName = FLAGS_URL.substring(FLAGS_URL.lastIndexOf('/') + 1);
			final File flagsFile = CacheManager.save("images", flagsFileName, FLAGS_URL);
			Platform.runLater(() -> {
				if (handle != null && handle.equals(SettingsManager.userHandle))
					editButton.setVisible(true);
				else
					editButton.setVisible(false);
				if (player == null) {
					avatarIcon.setImage(avatarImage);
					handleLabel.setText(handle);
					ratingLabel.setText(bundle.getString("common.rating") + ": "
						+ bundle.getString("common.offline"));
					locationLabel.setText(bundle.getString("common.offline"));
					joinedAtLabel.setText(bundle.getString("common.joined") + ": "
						+ bundle.getString("common.offline"));
				} else {
					final int rating = player.getRating();
					final String location = player.getLocation();
					final LocalDateTime joinedAt = player.getJoinedAt();
					avatarIcon.setImage(avatarImage);
					handleLabel.setText(handle);
					ratingLabel.setText(bundle.getString("common.rating") + ": " + rating);
					if (location != null && !location.isEmpty()) {
						final String countryKey = "country." + location.toLowerCase();
						final String countryName = bundle.containsKey(countryKey)
							? bundle.getString(countryKey)
							: location;
						locationLabel.setText(countryName);
						countryFlagIcon.setStyle(
							"-fx-background-image: url('" + flagsFile.toURI().toString() + "');");
						countryFlagIcon.getStyleClass().add("country-" + location);
						countryFlagIcon.setManaged(true);
						countryFlagIcon.setVisible(true);
					}
					if (joinedAt != null) {
						joinedAtLabel.setText(bundle.getString("common.joined") + ": "
							+ joinedAt.format(DATE_TIME_FORMATTER));
					}
				}
				profileItem.setVisible(true);
			});
		});
	}
	@FXML
	private void openEditProfile() {
		loadWindow("ui/editProfileWindow.fxml", new EditProfileWindow(), backButton);
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
}