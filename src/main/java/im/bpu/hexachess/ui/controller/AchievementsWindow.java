package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.manager.SettingsManager;
import im.bpu.hexachess.network.API;

import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.getAspectRatio;
import static im.bpu.hexachess.Main.loadWindow;

public class AchievementsWindow {
	private static final double ASPECT_RATIO_THRESHOLD = 1.5;
	@FXML private ScrollPane achievementsPane;
	@FXML private VBox achievementsContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (getAspectRatio() < ASPECT_RATIO_THRESHOLD) {
			achievementsPane.setStyle(
				"-fx-pref-width: 400px; -fx-max-width: 400px;"); // CSS instead of JavaFX's
																 // setPrefWidth/setMaxWidth due to
																 // parsing precedence
		}
		loadAchievements();
	}
	private void loadAchievements() {
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final String playerId = SettingsManager.playerId;
			final List<Achievement> achievements = API.achievements(playerId);
			Platform.runLater(() -> {
				if (achievements.isEmpty()) {
					final Label emptyLabel = new Label(bundle.getString("achievements.empty"));
					achievementsContainer.getChildren().add(emptyLabel);
				} else {
					for (final Achievement achievement : achievements) {
						try {
							final FXMLLoader achievementItemLoader = new FXMLLoader(
								getClass().getResource("/fxml/achievementItem.fxml"), bundle);
							final HBox achievementItem = achievementItemLoader.load();
							final String name = achievement.getName();
							final String description = achievement.getDescription();
							final boolean unlocked = achievement.getUnlocked();
							final Label nameLabel = (Label) achievementItem.lookup("#nameLabel");
							final Label descriptionLabel =
								(Label) achievementItem.lookup("#descriptionLabel");
							final Label statusLabel =
								(Label) achievementItem.lookup("#statusLabel");
							nameLabel.setText(name);
							descriptionLabel.setText(description);
							if (unlocked) {
								statusLabel.setText(bundle.getString("achievements.unlocked"));
								statusLabel.getStyleClass().add("text-success");
							} else {
								statusLabel.setText(bundle.getString("achievements.locked"));
								statusLabel.getStyleClass().add("text-danger");
							}
							achievementsContainer.getChildren().add(achievementItem);
						} catch (final Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			});
		});
	}
	@FXML
	private void openMain() {
		loadWindow("/fxml/mainWindow.fxml", new MainWindow(), backButton);
	}
}