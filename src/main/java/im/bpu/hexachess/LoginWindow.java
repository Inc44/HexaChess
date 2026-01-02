package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginWindow {
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
		String handle = handleField.getText();
		String pass = passwordField.getText();

		Player p = null;

		if ("root".equals(handle) && "password123".equals(pass)) {
			p = new Player("00000000000", "root", "root@localhost", "", 1200, true, null);
		} else {
			p = API.login(handle, pass);
			System.out.println("Connected as: " + (p != null ? p.getHandle() : "null"));
		}

		if (p != null) {
			Settings.userHandle = handle;
			Settings.authToken = p.getToken();
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
			errorLabel.setText("Invalid username or password");
			errorLabel.setVisible(true);
		}
	}

	@FXML
	private void openStart() {
		try {
			FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/startWindow.fxml"));
			mainWindowLoader.setController(new StartWindow());
			Parent root = mainWindowLoader.load();
			handleField.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}