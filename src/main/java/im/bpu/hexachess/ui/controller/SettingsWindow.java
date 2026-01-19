package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.manager.SettingsManager;
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
		maxDepthComboBox.getItems().addAll(bundle.getString("window.settings.aiDifficultyFast"),
			bundle.getString("window.settings.aiDifficultyDefault"),
			bundle.getString("window.settings.aiDifficultySlowest"));
		final String aiDifficultyLevel = mapMaxDepthToAiDifficultyLevel(SettingsManager.maxDepth);
		maxDepthComboBox.getSelectionModel().select(aiDifficultyLevel);
		updateAiDifficultyLevelTooltip(aiDifficultyLevel);
		maxDepthComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				SettingsManager.setMaxDepth(mapAiDifficultyLevelToMaxDepth(newValue));
				updateAiDifficultyLevelTooltip(newValue);
				syncSettings();
			}
		});
	}
	private void setupTheme() {
		themeComboBox.getItems().addAll("Light", "Dark", "Black");
		themeComboBox.getSelectionModel().select(SettingsManager.theme);
		themeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				SettingsManager.setTheme(newValue);
				Main.applyTheme(backButton.getScene());
				syncSettings();
			}
		});
	}
	private void setupLanguage() {
		languageComboBox.getItems().addAll(
			"English", "Français", "Deutsch", "Polski", "Русский", "Українська");
		languageComboBox.getSelectionModel().select(SettingsManager.language);
		languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				SettingsManager.setLanguage(newValue);
				loadWindow("/fxml/window/settingsWindow.fxml", new SettingsWindow(), backButton);
			}
		});
	}
	private void setupVolume() {
		volumeSlider.setValue(SettingsManager.volume);
		volumeSlider.valueProperty().addListener(
			(observable, oldValue, newValue) -> SettingsManager.setVolume(newValue.doubleValue()));
	}
	private void updateAiDifficultyLevelTooltip(String label) {
		final ResourceBundle bundle = Main.getBundle();
		if (label.equals(bundle.getString("window.settings.aiDifficultyFast")))
			aiDifficultyLevelTooltip.setText(
				bundle.getString("window.settings.aiDifficultyTooltipWin"));
		else if (label.equals(bundle.getString("window.settings.aiDifficultySlowest")))
			aiDifficultyLevelTooltip.setText(
				bundle.getString("window.settings.aiDifficultyTooltipLose"));
		else
			aiDifficultyLevelTooltip.setText(
				bundle.getString("window.settings.aiDifficultyTooltipDraw"));
	}
	private String mapMaxDepthToAiDifficultyLevel(int depth) {
		final ResourceBundle bundle = Main.getBundle();
		return switch (depth) {
			case 1 -> bundle.getString("window.settings.aiDifficultyFast");
			case 5 -> bundle.getString("window.settings.aiDifficultySlowest");
			default -> bundle.getString("window.settings.aiDifficultyDefault");
		};
	}
	private int mapAiDifficultyLevelToMaxDepth(String label) {
		final ResourceBundle bundle = Main.getBundle();
		if (label.equals(bundle.getString("window.settings.aiDifficultyFast")))
			return 1;
		if (label.equals(bundle.getString("window.settings.aiDifficultySlowest")))
			return 5;
		return 3;
	}
	private void syncSettings() {
		if (SettingsManager.playerId != null) {
			Thread.ofVirtual().start(() -> {
				final Settings settings = new Settings(SettingsManager.playerId,
					SettingsManager.theme.toLowerCase(), true, false, SettingsManager.maxDepth);
				API.settings(settings);
			});
		}
	}
	@FXML
	private void openStart() {
		SettingsManager.setPlayerId(null);
		SettingsManager.setUserHandle(null);
		SettingsManager.setAuthToken(null);
		loadWindow("/fxml/window/startWindow.fxml", new StartWindow(), backButton);
	}
	@FXML
	private void openMain() {
		loadWindow("/fxml/window/mainWindow.fxml", new MainWindow(), backButton);
	}
}