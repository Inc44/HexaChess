package im.bpu.hexachess;

import im.bpu.hexachess.ui.HexPanel;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainWindow {
	private HexPanel hexPanel;
	@FXML private Button settingsHelpButton;
	@FXML private VBox sidebar;
	@FXML private Canvas canvas;
	@FXML
	private void initialize() {
		hexPanel = new HexPanel(canvas, State.getState());
		sidebar.setTranslateX(-160);
		sidebar.setVisible(false);
	}
	@FXML
	private void toggleSidebar() {
		boolean closed = !sidebar.isVisible();
		Duration duration = Duration.millis(160);
		TranslateTransition transition = new TranslateTransition(duration, sidebar);
		if (closed) {
			transition.setToX(0);
			sidebar.setVisible(true);
		} else {
			transition.setToX(-160);
			transition.setOnFinished(ev -> sidebar.setVisible(false));
		}
		transition.play();
	}
	@FXML
	private void onBoardClicked() {
		if (sidebar.isVisible()) {
			toggleSidebar();
		}
	}
	@FXML
	private void restart() {
		hexPanel.restart();
	}
	@FXML
	private void rewind() {
		hexPanel.rewind();
	}
	@FXML
	private void openSettings() {
		try {
			FXMLLoader settingsWindowLoader =
				new FXMLLoader(getClass().getResource("ui/settingsWindow.fxml"));
			settingsWindowLoader.setController(new SettingsWindow());
			Parent root = settingsWindowLoader.load();
			settingsHelpButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}