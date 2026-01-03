package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SearchWindow {
	@FXML private TextField searchField;
	@FXML private VBox playerContainer;
	@FXML private Button backButton;
	@FXML
	private void handleSearch() {
		playerContainer.getChildren().clear();
		String query = searchField.getText();
		if (query.isEmpty())
			return;
		List<Player> players = API.search(query);
		for (Player player : players) {
			String handle = player.getHandle();
			int rating = player.getRating();
			Label handleLabel = new Label(handle);
			Label ratingLabel = new Label("Rating: " + rating);
			HBox playerItem = new HBox();
			VBox playerInfo = new VBox(handleLabel, ratingLabel);
			Region spacer = new Region();
			Button challengeButton = new Button("⚔");
			HBox.setHgrow(spacer, Priority.ALWAYS);
			playerItem.getStyleClass().add("player-item");
			playerItem.getChildren().addAll(playerInfo, spacer, challengeButton);
			playerInfo.setOnMouseClicked(event -> openProfile(handle));
			challengeButton.setOnAction(event -> startMatchmaking(handle));
			playerContainer.getChildren().add(playerItem);
		}
	}
	private void startMatchmaking(String target) {
		new Thread(() -> {
			String handle = Settings.userHandle;
			while (true) {
				String resp = API.challenge(handle, target);
				if (resp != null && !resp.equals("Pending")) {
					Platform.runLater(() -> {
						State state = State.getState();
						state.clear();
						state.isMultiplayer = true;
						state.gameId = resp;
						state.isWhitePlayer = handle.compareTo(target) < 0;
						openMain();
					});
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (Exception ignored) { // high-frequency polling operation
				}
			}
		}).start();
	}
	private void openProfile(String handle) {
		try {
			Settings.userHandle = handle;
			FXMLLoader profileWindowLoader =
				new FXMLLoader(getClass().getResource("ui/profileWindow.fxml"));
			profileWindowLoader.setController(new ProfileWindow());
			Parent root = profileWindowLoader.load();
			backButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
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