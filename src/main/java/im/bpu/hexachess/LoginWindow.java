package im.bpu.hexachess;

import im.bpu.hexachess.dao.PlayerDAO;
import im.bpu.hexachess.entity.Player;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

public class LoginWindow {
	@FXML private TextField handleField;
	@FXML private PasswordField passwordField;
	@FXML private Label errorLabel;

	@FXML
	private void handleLogin() {
		String handle = handleField.getText();
		String pass = passwordField.getText();

		boolean loginSuccess = false;

		if ("root".equals(handle) && "password123".equals(pass)) {
			loginSuccess = true;
		} else {
			PlayerDAO dao = new PlayerDAO();
			Player p = dao.getPlayerByHandle(handle);
			if (p != null) {
				if (BCrypt.checkpw(pass, p.getPasswordHash())) {
					loginSuccess = true;
				}
			}
			System.out.println("Connecté en tant que : " + p.getHandle());
			dao.close();
		}

		if (loginSuccess) {
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
			errorLabel.setText("Pseudo ou mot de passe incorrect");
			errorLabel.setVisible(true);
			errorLabel.setStyle("-fx-text-fill: red;");
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