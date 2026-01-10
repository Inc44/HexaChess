package im.bpu.hexachess.ui;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

class PieceImageLoader {
	private static final String BASE_URL =
		"https://images.chesscomfiles.com/chess-themes/pieces/classic/300/";
	private static final int TOTAL_IMAGES = 12;
	private static final Map<String, Image> images = new HashMap<>();
	private static boolean loaded = false;
	private static void loadImage(String key, int[] loadedCount, Runnable onload) {
		Image image = new Image(BASE_URL + key + ".png", true);
		image.progressProperty().addListener((observable, oldValue, newValue) -> {
			if (!image.isError() && newValue.doubleValue() == 1.0) {
				loadedCount[0]++;
				if (loadedCount[0] == TOTAL_IMAGES) {
					loaded = true;
					if (onload != null)
						onload.run();
				}
			}
		});
		images.put(key, image);
	}
	static void loadImages(Runnable onload) {
		if (loaded) {
			if (onload != null)
				onload.run();
			return;
		}
		int[] loadedCount = {0};
		String[] colors = {"w", "b"};
		String[] types = {"p", "r", "n", "b", "q", "k"};
		for (String color : colors)
			for (String type : types) loadImage(color + type, loadedCount, onload);
	}
	static Image get(String key) {
		return images.get(key);
	}
	static boolean isLoaded() {
		return loaded;
	}
}