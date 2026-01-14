package im.bpu.hexachess;

import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.network.API;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

import static im.bpu.hexachess.Main.loadWindow;

public class SettingsWindow {
	@FXML private ComboBox<String> maxDepthComboBox;
	@FXML private Tooltip aiDifficultyLevelTooltip;
	@FXML private Slider volumeSlider;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		setupAiDifficultyLevel();
		setupVolume();
	}
	private void setupAiDifficultyLevel() {
		maxDepthComboBox.getItems().addAll("Fast", "Default", "Slowest");
		final String aiDifficultyLevel = mapMaxDepthToAiDifficultyLevel(SettingsManager.maxDepth);
		maxDepthComboBox.getSelectionModel().select(aiDifficultyLevel);
		updateAiDifficultyLevelTooltip(aiDifficultyLevel);
		maxDepthComboBox.valueProperty().addListener(
			(observable, oldValue, newValue) -> updateAiDifficultyLevelTooltip(newValue));
	}
	private void setupVolume() {
		volumeSlider.setValue(SettingsManager.volume);
		volumeSlider.valueProperty().addListener(
			(observable, oldValue, newValue) -> SettingsManager.setVolume(newValue.doubleValue()));
	}
	private void updateAiDifficultyLevelTooltip(String label) {
		aiDifficultyLevelTooltip.setText(switch (label) {
			case "Fast" -> "You can win";
			case "Slowest" -> "You can lose";
			default -> "You can draw";
		});
	}
	private String mapMaxDepthToAiDifficultyLevel(int depth) {
		return switch (depth) {
			case 1 -> "Fast";
			case 5 -> "Slowest";
			default -> "Default";
		};
	}
	private int mapAiDifficultyLevelToMaxDepth(String label) {
		return switch (label) {
			case "Fast" -> 1;
			case "Slowest" -> 5;
			default -> 3;
		};
	}
	@FXML
	private void openMain() {
		final String selected = maxDepthComboBox.getValue();
		if (selected != null) {
			SettingsManager.setMaxDepth(mapAiDifficultyLevelToMaxDepth(selected));
			Thread.ofVirtual().start(() -> {
				final Settings settings = new Settings(
					SettingsManager.playerId, "default", true, false, SettingsManager.maxDepth);
				API.settings(settings);
			});
		}
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
	@FXML
	private void openStart() {
		SettingsManager.setPlayerId(null);
		SettingsManager.setUserHandle(null);
		SettingsManager.setAuthToken(null);
		loadWindow("ui/startWindow.fxml", new StartWindow(), backButton);
	}
}