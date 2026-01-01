package im.bpu.hexachess;

import im.bpu.hexachess.dao.PlayerDAO;
import im.bpu.hexachess.entity.Player;

import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterWindow {
	@FXML private TextField handleField;
	@FXML private TextField emailField;
	@FXML private PasswordField passwordField;
	@FXML private Label statusLabel;

	@FXML
	private void handleRegister() {
		PlayerDAO dao = new PlayerDAO();
		String id = UUID.randomUUID().toString().substring(0, 11);
		String handle = handleField.getText();
		Player newPlayer = new Player(id, handle, emailField.getText(),
			BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()), 1200, false,
			java.time.LocalDateTime.now());

		try {
			dao.create(newPlayer);
			Settings.userHandle = handle;
			Settings.save();
			FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
			mainWindowLoader.setController(new MainWindow());
			Parent root = mainWindowLoader.load();
			handleField.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
			statusLabel.setText("Erreur (Pseudo déjà pris ?)");
			statusLabel.setVisible(true);
			statusLabel.setStyle("-fx-text-fill: red;");
		}
		dao.close();
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