package im.bpu.hexachess;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
	private static final String FONT_URL =
		"https://raw.githubusercontent.com/ryanoasis/nerd-fonts/refs/heads/master/patched-fonts/"
		+ "Noto/Sans-Mono/NotoSansMNerdFontMono-Bold.ttf";
	private static final double ASPECT_RATIO_THRESHOLD = 1.5;
	private static final double DESKTOP_WIDTH = 1200;
	private static final double DESKTOP_HEIGHT = 800;
	private static final double MOBILE_WIDTH = 540;
	private static final double MOBILE_HEIGHT = 1200;
	private static final String WINDOW_TITLE = "HexaChess";
	private static final String LANGUAGE_PACKAGE = "im.bpu.hexachess.ui.lang";
	@Override
	public void start(final Stage stage) throws Exception {
		try {
			final String fontFileName = FONT_URL.substring(FONT_URL.lastIndexOf("/") + 1);
			final File fontFile = CacheManager.save("fonts", fontFileName, FONT_URL);
			if (fontFile.exists()) {
				try (final FileInputStream fontFileInputStream = new FileInputStream(fontFile)) {
					Font.loadFont(fontFileInputStream, 16);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		final ResourceBundle bundle = getBundle();
		final Parent root;
		if (SettingsManager.userHandle != null) {
			final FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"), bundle);
			mainWindowLoader.setController(new MainWindow());
			root = mainWindowLoader.load();
		} else {
			final FXMLLoader startWindowLoader =
				new FXMLLoader(getClass().getResource("ui/startWindow.fxml"), bundle);
			startWindowLoader.setController(new StartWindow());
			root = startWindowLoader.load();
		}
		final double width;
		final double height;
		if (getAspectRatio() > ASPECT_RATIO_THRESHOLD) {
			width = DESKTOP_WIDTH;
			height = DESKTOP_HEIGHT;
		} else {
			width = MOBILE_WIDTH;
			height = MOBILE_HEIGHT;
		}
		final Scene scene = new Scene(root, width, height);
		applyTheme(scene);
		stage.setTitle(WINDOW_TITLE);
		stage.setScene(scene);
		stage.show();
	}
	public static ResourceBundle getBundle() {
		return ResourceBundle.getBundle(LANGUAGE_PACKAGE, getLocale());
	}
	public static Locale getLocale() {
		return switch (SettingsManager.language) {
			case "Français" -> Locale.FRENCH;
			case "Русский" -> Locale.of("ru");
			case "Українська" -> Locale.of("ua");
			default -> Locale.ENGLISH;
		};
	}
	public static double getAspectRatio() {
		final double width = Screen.getPrimary().getBounds().getWidth();
		final double height = Screen.getPrimary().getBounds().getHeight();
		final double aspectRatio = width / height;
		return aspectRatio;
	}
	public static void applyTheme(Scene scene) {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(Main.class.getResource("ui/style.css").toExternalForm());
		String themeFileName = "ui/" + SettingsManager.theme.toLowerCase() + ".css";
		scene.getStylesheets().add(Main.class.getResource(themeFileName).toExternalForm());
	}
	public static void loadWindow(final String path, final Object controller, final Node node) {
		try {
			final ResourceBundle bundle = getBundle();
			final FXMLLoader loader = new FXMLLoader(Main.class.getResource(path), bundle);
			loader.setController(controller);
			Scene scene = node.getScene();
			scene.setRoot(loader.load());
			applyTheme(scene);
		} catch (final Exception exception) {
			exception.printStackTrace();
		}
	}
	public static void main(final String[] args) {
		launch(args);
	}
}