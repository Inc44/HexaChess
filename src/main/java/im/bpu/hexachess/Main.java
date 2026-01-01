package im.bpu.hexachess;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		Parent root;
		if (Settings.userHandle != null) {
			FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
			mainWindowLoader.setController(new MainWindow());
			root = mainWindowLoader.load();
		} else {
			FXMLLoader startWindowLoader =
				new FXMLLoader(getClass().getResource("ui/startWindow.fxml"));
			startWindowLoader.setController(new StartWindow());
			root = startWindowLoader.load();
		}
		double width = Screen.getPrimary().getBounds().getWidth();
		double height = Screen.getPrimary().getBounds().getHeight();
		double aspectRatio = width / height;
		if (aspectRatio > 1.5) {
			width = 1200;
			height = 800;
		} else {
			width = 540;
			height = 1200;
		}
		Scene scene = new Scene(root, width, height);
		scene.getStylesheets().add(getClass().getResource("ui/style.css").toExternalForm());
		stage.setTitle("HexaChess");
		stage.setScene(scene);
		stage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}