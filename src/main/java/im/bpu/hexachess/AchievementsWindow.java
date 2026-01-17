package im.bpu.hexachess;

import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.network.API;
import im.bpu.hexachess.ui.AchievementItemController;

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
	@FXML private ScrollPane scrollPane;
	@FXML private VBox achievementsContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (getAspectRatio() < ASPECT_RATIO_THRESHOLD) {
			scrollPane.setStyle("-fx-pref-width: 400px; -fx-max-width: 400px;");
		}
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final String playerId = SettingsManager.playerId;
			final List<Achievement> achievements = API.achievements(playerId);
			Platform.runLater(() -> {
				achievementsContainer.getChildren().clear();
				if (achievements == null || achievements.isEmpty()) {
					achievementsContainer.getChildren().add(
						new Label(bundle.getString("achievements.empty")));
					return;
				}
				for (final Achievement achievement : achievements) {
					try {
						final FXMLLoader loader = new FXMLLoader(
							getClass().getResource("ui/achievementItem.fxml"), bundle);
						final HBox item = loader.load();
						final AchievementItemController controller = loader.getController();
						controller.setAchievement(achievement);
						achievementsContainer.getChildren().add(item);
					} catch (final Exception exception) {
						exception.printStackTrace();
					}
				}
			});
		});
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
}