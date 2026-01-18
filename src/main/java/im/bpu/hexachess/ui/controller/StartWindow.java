package im.bpu.hexachess.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import static im.bpu.hexachess.Main.loadWindow;

public class StartWindow {
	@FXML private Button loginButton;
	@FXML
	private void openLogin() {
		loadWindow("/fxml/window/loginWindow.fxml", new LoginWindow(), loginButton);
	}
	@FXML
	private void openRegister() {
		loadWindow("/fxml/window/registerWindow.fxml", new RegisterWindow(), loginButton);
	}
}