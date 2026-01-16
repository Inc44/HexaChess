package im.bpu.hexachess;

import im.bpu.hexachess.dao.AchievementDAO;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.ui.AchievementItemController;

import java.util.List;

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

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox achievementsContainer;

    @FXML
    private Button backButton;

    @FXML
    private void initialize() {
        if (getAspectRatio() < ASPECT_RATIO_THRESHOLD) {
            scrollPane.setStyle("-fx-pref-width: 400px; -fx-max-width: 400px;");
        }
        Thread.ofVirtual().start(() -> {
            String playerId = SettingsManager.playerId;
            List<Achievement> achievements = new AchievementDAO().readAllForPlayer(playerId);

            Platform.runLater(() -> {
                achievementsContainer.getChildren().clear();

                if (achievements == null || achievements.isEmpty()) {
                    achievementsContainer.getChildren().add(new Label("No achievements found."));
                    return;
                }

                for (Achievement achievement : achievements) {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("ui/achievementItem.fxml")
                        );

                        HBox item = loader.load();
                        AchievementItemController controller = loader.getController();
                        controller.setAchievement(achievement);

                        achievementsContainer.getChildren().add(item);

                    } catch (Exception e) {
                        e.printStackTrace();
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
