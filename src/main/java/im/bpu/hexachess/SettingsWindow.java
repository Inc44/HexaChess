package im.bpu.hexachess;

import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.network.API;

import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;

import static im.bpu.hexachess.Main.loadWindow;

public class SettingsWindow {
	@FXML private ComboBox<String> maxDepthComboBox;
	@FXML private Tooltip aiDifficultyLevelTooltip;
	@FXML private ComboBox<String> themeComboBox;
	@FXML private ComboBox<String> languageComboBox;
	@FXML private Slider volumeSlider;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		setupAiDifficultyLevel();
		setupTheme();
		setupLanguage();
		setupVolume();
	}
	private void setupAiDifficultyLevel() {
		final ResourceBundle bundle = Main.getBundle();
		maxDepthComboBox.getItems().addAll(bundle.getString("settings.aidifficulty.fast"),
			bundle.getString("settings.aidifficulty.default"),
			bundle.getString("settings.aidifficulty.slowest"));
		final String aiDifficultyLevel = mapMaxDepthToAiDifficultyLevel(SettingsManager.maxDepth);
		maxDepthComboBox.getSelectionModel().select(aiDifficultyLevel);
		updateAiDifficultyLevelTooltip(aiDifficultyLevel);
		maxDepthComboBox.valueProperty().addListener(
			(observable, oldValue, newValue) -> updateAiDifficultyLevelTooltip(newValue));
	}
	private void setupTheme() {
		themeComboBox.getItems().addAll("Light", "Dark", "Black");
		themeComboBox.getSelectionModel().select(SettingsManager.theme);
	}
	private void setupLanguage() {
		languageComboBox.getItems().addAll("English", "Français", "Русский", "Українська");
		languageComboBox.getSelectionModel().select(SettingsManager.language);
	}
	private void setupVolume() {
		volumeSlider.setValue(SettingsManager.volume);
		volumeSlider.valueProperty().addListener(
			(observable, oldValue, newValue) -> SettingsManager.setVolume(newValue.doubleValue()));
	}
	private void updateAiDifficultyLevelTooltip(String label) {
		final ResourceBundle bundle = Main.getBundle();
		if (label.equals(bundle.getString("settings.aidifficulty.fast")))
			aiDifficultyLevelTooltip.setText(bundle.getString("settings.aidifficulty.tooltip.win"));
		else if (label.equals(bundle.getString("settings.aidifficulty.slowest")))
			aiDifficultyLevelTooltip.setText(
				bundle.getString("settings.aidifficulty.tooltip.lose"));
		else
			aiDifficultyLevelTooltip.setText(
				bundle.getString("settings.aidifficulty.tooltip.draw"));
	}
	private String mapMaxDepthToAiDifficultyLevel(int depth) {
		final ResourceBundle bundle = Main.getBundle();
		return switch (depth) {
			case 1 -> bundle.getString("settings.aidifficulty.fast");
			case 5 -> bundle.getString("settings.aidifficulty.slowest");
			default -> bundle.getString("settings.aidifficulty.default");
		};
	}
	private int mapAiDifficultyLevelToMaxDepth(String label) {
		final ResourceBundle bundle = Main.getBundle();
		if (label.equals(bundle.getString("settings.aidifficulty.fast")))
			return 1;
		if (label.equals(bundle.getString("settings.aidifficulty.slowest")))
			return 5;
		return 3;
	}
	@FXML
	private void openMain() {
		final String selectedAiDifficultyLevel = maxDepthComboBox.getValue();
		final String selectedTheme = themeComboBox.getValue();
		final String selectedLanguage = languageComboBox.getValue();
		if (selectedAiDifficultyLevel != null)
			SettingsManager.setMaxDepth(mapAiDifficultyLevelToMaxDepth(selectedAiDifficultyLevel));
		if (selectedTheme != null)
			SettingsManager.setTheme(selectedTheme);
		if (selectedLanguage != null)
			SettingsManager.setLanguage(selectedLanguage);
		if (SettingsManager.playerId != null) {
			Thread.ofVirtual().start(() -> {
				final Settings settings = new Settings(SettingsManager.playerId,
					SettingsManager.theme.toLowerCase(), true, false, SettingsManager.maxDepth);
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