package im.bpu.hexachess.ui;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Achievement;

import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AchievementItemController {
	@FXML private Label nameLabel;
	@FXML private Label descriptionLabel;
	@FXML private Label statusLabel;
	public void setAchievement(final Achievement achievement) {
		final ResourceBundle bundle = Main.getBundle();
		nameLabel.setText(achievement.getName());
		descriptionLabel.setText(achievement.getDescription());
		if (achievement.getUnlocked()) {
			statusLabel.setText(bundle.getString("achievements.unlocked") + " ✅");
			statusLabel.getStyleClass().remove("text-danger");
			statusLabel.getStyleClass().add("text-success");
		} else {
			statusLabel.setText(bundle.getString("achievements.locked") + " 🔒");
		}
	}
}