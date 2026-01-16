package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static im.bpu.hexachess.Main.loadWindow;

public class RegisterWindow {
	private static final int MAX_HANDLE_LENGTH = 32;
	private static final int MIN_PASSWORD_LENGTH = 8;
	private static final int BASE_ELO = 1200;
	@FXML private TextField handleField;
	@FXML private TextField emailField;
	@FXML private PasswordField passwordField;
	@FXML private Label statusLabel;
	@FXML
	private void handleRegister() {
		if (handleField.getText().isEmpty()) {
			handleField.requestFocus();
			return;
		}
		if (emailField.getText().isEmpty()) {
			emailField.requestFocus();
			return;
		}
		if (passwordField.getText().isEmpty()) {
			passwordField.requestFocus();
			return;
		}
		final ResourceBundle bundle = Main.getBundle();
		final String handle = handleField.getText();
		final String email = emailField.getText();
		final String password = passwordField.getText();
		if (handle.length() > MAX_HANDLE_LENGTH) {
			statusLabel.setText(
				MAX_HANDLE_LENGTH + " " + bundle.getString("register.error.handle"));
			statusLabel.setManaged(true);
			statusLabel.setVisible(true);
			return;
		}
		if (!email.contains("@") || !email.contains(".")) {
			statusLabel.setText(bundle.getString("register.error.email"));
			statusLabel.setManaged(true);
			statusLabel.setVisible(true);
			return;
		}
		if (password.length() < MIN_PASSWORD_LENGTH) {
			statusLabel.setText(
				MIN_PASSWORD_LENGTH + " " + bundle.getString("register.error.password"));
			statusLabel.setManaged(true);
			statusLabel.setVisible(true);
			return;
		}
		Thread.ofVirtual().start(() -> {
			final byte[] bytes = new byte[9];
			final SecureRandom rand = new SecureRandom();
			rand.nextBytes(bytes);
			final String playerId =
				Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 11);
			final Player player =
				new Player(playerId, handle, email, password, BASE_ELO, false, null);
			final boolean registerSuccess = API.register(player);
			Platform.runLater(() -> {
				if (registerSuccess) {
					SettingsManager.setUserHandle(handle);
					openMain();
				} else {
					statusLabel.setText(bundle.getString("register.error.server"));
					statusLabel.setManaged(true);
					statusLabel.setVisible(true);
				}
			});
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