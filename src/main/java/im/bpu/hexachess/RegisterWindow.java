package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterWindow {
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
		String id = UUID.randomUUID().toString().substring(0, 11);
		String handle = handleField.getText();
		Player newPlayer = new Player(
			id, handle, emailField.getText(), passwordField.getText(), 1200, false, null);
		boolean registerSuccess = API.register(newPlayer);
		if (registerSuccess) {
			Settings.userHandle = handle;
			Settings.save();
			try {
				FXMLLoader mainWindowLoader =
					new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
				mainWindowLoader.setController(new MainWindow());
				Parent root = mainWindowLoader.load();
				handleField.getScene().setRoot(root);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		} else {
			statusLabel.setText("Error (Username taken or server error)");
			statusLabel.setVisible(true);
		}
	}
	@FXML
	private void openStart() {
		try {
			FXMLLoader startWindowLoader =
				new FXMLLoader(getClass().getResource("ui/startWindow.fxml"));
			startWindowLoader.setController(new StartWindow());
			Parent root = startWindowLoader.load();
			handleField.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}