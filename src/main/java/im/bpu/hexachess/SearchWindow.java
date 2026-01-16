package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.getAspectRatio;
import static im.bpu.hexachess.Main.loadWindow;

public class SearchWindow {
	private static final String AVATAR_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final String FLAGS_URL =
		"https://www.chess.com/bundles/web/images/sprites/flags-128.png";
	private static final double ASPECT_RATIO_THRESHOLD = 1.5;
	private static final long DT = 500;
	private static final long MAX_DT = 6000;
	private static final int BACKOFF_FACTOR = 2;
	@FXML private TextField searchField;
	@FXML private ScrollPane searchPane;
	@FXML private VBox playerContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (getAspectRatio() < ASPECT_RATIO_THRESHOLD) {
			searchPane.setStyle(
				"-fx-pref-width: 400px; -fx-max-width: 400px;"); // CSS instead of JavaFX's
																 // setPrefWidth/setMaxWidth due to
																 // parsing precedence
		}
	}
	@FXML
	private void handleSearch() {
		final String query = searchField.getText();
		if (query.isEmpty()) {
			playerContainer.getChildren().clear();
			return;
		}
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final List<Player> players = API.search(query);
			Platform.runLater(() -> {
				playerContainer.getChildren().clear();
				if (players.isEmpty()) {
					final Label emptyLabel = new Label(bundle.getString("search.empty"));
					playerContainer.getChildren().add(emptyLabel);
				} else {
					for (final Player player : players) {
						try {
							final FXMLLoader playerItemLoader =
								new FXMLLoader(getClass().getResource("ui/playerItem.fxml"));
							final HBox playerItem = playerItemLoader.load();
							final String handle = player.getHandle();
							final int rating = player.getRating();
							final String location = player.getLocation();
							final String avatarUrl =
								(player.getAvatar() != null && !player.getAvatar().isEmpty())
								? player.getAvatar()
								: AVATAR_URL;
							final ImageView avatarIcon =
								(ImageView) playerItem.lookup("#avatarIcon");
							final Label handleLabel = (Label) playerItem.lookup("#handleLabel");
							final Region countryFlagIcon =
								(Region) playerItem.lookup("#countryFlagIcon");
							final Label ratingLabel = (Label) playerItem.lookup("#ratingLabel");
							final Button challengeButton =
								(Button) playerItem.lookup("#challengeButton");
							final File avatarFile = CacheManager.save("avatars", handle, avatarUrl);
							final Image avatarImage = new Image(avatarFile.toURI().toString());
							final String flagsFileName =
								FLAGS_URL.substring(FLAGS_URL.lastIndexOf('/') + 1);
							final File flagsFile =
								CacheManager.save("images", flagsFileName, FLAGS_URL);
							avatarIcon.setImage(avatarImage);
							handleLabel.setText(handle);
							ratingLabel.setText(bundle.getString("common.rating") + ": " + rating);
							if (location != null && !location.isEmpty()) {
								countryFlagIcon.setStyle("-fx-background-image: url('"
									+ flagsFile.toURI().toString() + "');");
								countryFlagIcon.getStyleClass().add("country-" + location);
								countryFlagIcon.setManaged(true);
								countryFlagIcon.setVisible(true);
							}
							playerItem.setOnMouseClicked(event -> openProfile(handle));
							challengeButton.setOnAction(event -> startMatchmaking(handle));
							playerContainer.getChildren().add(playerItem);
						} catch (final Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			});
		});
	}
	private void startMatchmaking(final String target) {
		Thread.ofVirtual().start(() -> {
			final String handle = SettingsManager.userHandle;
			long dt = DT;
			while (true) {
				final String resp = API.challenge(handle, target);
				if (resp != null && !resp.equals("Pending")) {
					Platform.runLater(() -> {
						final State state = State.getState();
						state.clear();
						state.isMultiplayer = true;
						state.gameId = resp;
						state.opponentHandle = target;
						state.isWhitePlayer = handle.compareTo(target) < 0;
						openMain();
					});
					break;
				}
				try {
					Thread.sleep(dt);
					dt = Math.min(MAX_DT, dt * BACKOFF_FACTOR);
				} catch (final Exception ignored) { // high-frequency polling operation
				}
			}
		});
	}
	private void openProfile(final String handle) {
		ProfileWindow.targetHandle = handle;
		loadWindow("ui/profileWindow.fxml", new ProfileWindow(), backButton);
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
}