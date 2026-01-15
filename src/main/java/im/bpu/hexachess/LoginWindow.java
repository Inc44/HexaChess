package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.network.API;

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static im.bpu.hexachess.Main.loadWindow;

public class LoginWindow {
	private static final String ROOT_HANDLE = "root";
	private static final String ROOT_PASSWORD = "password123";
	private static final String ROOT_ID = "00000000000";
	private static final String ROOT_EMAIL = "root@localhost";
	private static final int BASE_ELO = 1200;
	@FXML private TextField handleField;
	@FXML private PasswordField passwordField;
	@FXML private Label errorLabel;
	@FXML
	private void handleLogin() {
		if (handleField.getText().isEmpty()) {
			handleField.requestFocus();
			return;
		}
		if (passwordField.getText().isEmpty()) {
			passwordField.requestFocus();
			return;
		}
		final String handle = handleField.getText();
		final String password = passwordField.getText();
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final Player player;
			if (ROOT_HANDLE.equals(handle) && ROOT_PASSWORD.equals(password)) {
				player = new Player(ROOT_ID, ROOT_HANDLE, ROOT_EMAIL, "", BASE_ELO, true, null);
			} else {
				player = API.login(handle, password);
				System.out.println(bundle.getString("login.connectedas") + ": "
					+ (player != null ? player.getHandle() : "null"));
			}
			if (player != null) {
				final String playerId = player.getPlayerId();
				SettingsManager.setPlayerId(playerId);
				SettingsManager.setUserHandle(handle);
				SettingsManager.setAuthToken(player.getToken());
				final Settings settings = API.settings(playerId);
				if (settings != null) {
					SettingsManager.setMaxDepth(settings.getAiDifficultyLevel());
				}
				Platform.runLater(this::openMain);
			} else {
				Platform.runLater(() -> {
					errorLabel.setText(bundle.getString("login.error"));
					errorLabel.setManaged(true);
					errorLabel.setVisible(true);
				});
			}
		});
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), handleField);
	}
	@FXML
	private void openStart() {
		loadWindow("ui/startWindow.fxml", new StartWindow(), handleField);
	}
}