package im.bpu.hexachess.ui;

import im.bpu.hexachess.entity.Achievement;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AchievementItemController {
	@FXML private Label nameLabel;
	@FXML private Label descriptionLabel;
	@FXML private Label statusLabel;
	public void setAchievement(final Achievement achievement) {
		nameLabel.setText(achievement.getName());
		descriptionLabel.setText(achievement.getDescription());
		if (achievement.isUnlocked()) {
			statusLabel.setText("Unlocked ✅");
			statusLabel.getStyleClass().remove("text-danger");
			statusLabel.getStyleClass().add("text-success");
		} else {
			statusLabel.setText("Locked 🔒");
		}
	}
}