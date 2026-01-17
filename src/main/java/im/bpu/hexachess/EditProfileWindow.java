package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
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
	@FXML private TextField emailField;
	@FXML private TextField locationField;
	@FXML private TextField avatarField;
	@FXML private PasswordField newPasswordField;
	@FXML private PasswordField currentPasswordField;
	@FXML private Label statusLabel;
	@FXML private Button backButton;
	private String handle;
	@FXML
	private void initialize() {
		handle = SettingsManager.userHandle;
		if (handle != null) {
			loadCurrentData();
		}
	}
	private void loadCurrentData() {
		Thread.ofVirtual().start(() -> {
			final Player player = API.profile(handle);
			if (player != null) {
				Platform.runLater(() -> {
					emailField.setText(player.getEmail());
					locationField.setText(player.getLocation());
					avatarField.setText(player.getAvatar());
					currentPasswordField.clear();
					newPasswordField.clear();
				});
			}
		});
	}
	@FXML
	private void handleSave() {
		final ResourceBundle bundle = Main.getBundle();
		final String currentPass = currentPasswordField.getText();
		if (currentPass == null || currentPass.isEmpty()) {
			statusLabel.setText(bundle.getString("profile.edit.status.required"));
			statusLabel.setStyle("-fx-text-fill: red;");
			statusLabel.setManaged(true);
			statusLabel.setVisible(true);
			return;
		}
		Thread.ofVirtual().start(() -> {
			final boolean success = API.update(currentPass, emailField.getText(),
				locationField.getText(), avatarField.getText(), newPasswordField.getText());
			Platform.runLater(() -> {
				if (success) {
					statusLabel.setText(bundle.getString("profile.edit.status.success"));
					statusLabel.setStyle("-fx-text-fill: green;");
					currentPasswordField.clear();
					newPasswordField.clear();
				} else {
					statusLabel.setText(bundle.getString("profile.edit.status.failed"));
					statusLabel.setStyle("-fx-text-fill: red;");
				}
				statusLabel.setManaged(true);
				statusLabel.setVisible(true);
			});
		});
	}
	@FXML
	private void handleCancel() {
		ProfileWindow.targetHandle = SettingsManager.userHandle;
		loadWindow("ui/profileWindow.fxml", new ProfileWindow(), backButton);
	}
}