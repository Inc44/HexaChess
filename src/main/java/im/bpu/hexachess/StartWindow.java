package im.bpu.hexachess;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;

public class StartWindow {
	@FXML private Button loginButton;
	@FXML
	private void goToLogin() {
		try {
			FXMLLoader loginWindowLoader =
				new FXMLLoader(getClass().getResource("ui/loginWindow.fxml"));
			loginWindowLoader.setController(new LoginWindow());
			Parent root = loginWindowLoader.load();
			loginButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
	@FXML
	private void goToRegister() {
		try {
			FXMLLoader registerWindowLoader =
				new FXMLLoader(getClass().getResource("ui/registerWindow.fxml"));
			registerWindowLoader.setController(new RegisterWindow());
			Parent root = registerWindowLoader.load();
			loginButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}