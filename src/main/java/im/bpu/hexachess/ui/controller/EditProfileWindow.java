package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.manager.SettingsManager;
import im.bpu.hexachess.network.API;

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static im.bpu.hexachess.Main.loadWindow;

public class EditProfileWindow {
	@FXML private TextField avatarField;
	@FXML private TextField locationField;
	@FXML private TextField emailField;
	@FXML private PasswordField newPasswordField;
	@FXML private PasswordField passwordField;
	@FXML private Label statusLabel;
	@FXML private Button backButton;
	private String handle;
	@FXML
	private void initialize() {
		handle = SettingsManager.userHandle;
		if (handle != null) {
			loadProfile();
		}
	}
	private void loadProfile() {
		Thread.ofVirtual().start(() -> {
			final Player player = API.profile(handle);
			if (player != null) {
				final String avatarUrl = player.getAvatar();
				final String location = player.getLocation();
				final String email = player.getEmail();
				Platform.runLater(() -> {
					avatarField.setText(avatarUrl);
					locationField.setText(location);
					emailField.setText(email);
					newPasswordField.clear();
					passwordField.clear();
				});
			}
		});
	}
	@FXML
	private void save() {
		final ResourceBundle bundle = Main.getBundle();
		final String password = passwordField.getText();
		if (password == null || password.isEmpty()) {
			statusLabel.setText(bundle.getString("window.editprofile.requiredError"));
			statusLabel.getStyleClass().setAll("text-danger");
			statusLabel.setManaged(true);
			statusLabel.setVisible(true);
			return;
		}
		Thread.ofVirtual().start(() -> {
			final String avatarUrl = avatarField.getText();
			final String location = locationField.getText();
			final String email = emailField.getText();
			final String newPassword = newPasswordField.getText();
			final boolean updateSuccess =
				API.update(password, email, avatarUrl, location, newPassword);
			Platform.runLater(() -> {
				if (updateSuccess) {
					statusLabel.setText(bundle.getString("window.editprofile.success"));
					statusLabel.getStyleClass().setAll("text-success");
					passwordField.clear();
					newPasswordField.clear();
				} else {
					statusLabel.setText(bundle.getString("window.editprofile.fail"));
					statusLabel.getStyleClass().setAll("text-danger");
				}
				statusLabel.setManaged(true);
				statusLabel.setVisible(true);
			});
		});
	}
	@FXML
	private void openProfile() {
		ProfileWindow.targetHandle = SettingsManager.userHandle;
		loadWindow("/fxml/window/profileWindow.fxml", new ProfileWindow(), backButton);
	}
}