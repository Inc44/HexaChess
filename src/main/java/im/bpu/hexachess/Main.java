package im.bpu.hexachess;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader mainWindowLoader = new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
		mainWindowLoader.setController(new MainWindow());
		Scene scene = new Scene(mainWindowLoader.load());
		scene.getStylesheets().add(getClass().getResource("ui/style.css").toExternalForm());
		stage.setScene(scene);
		stage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}