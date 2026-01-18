package im.bpu.hexachess.ui;

import im.bpu.hexachess.manager.CacheManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.image.Image;

class PieceImageLoader {
	private static final String PIECE_URL =
		"https://images.chesscomfiles.com/chess-themes/pieces/classic/300/";
	private static final int TOTAL_IMAGES = 12;
	private static final Map<String, Image> IMAGES = new HashMap<>();
	private static final String[] COLORS = {"w", "b"};
	private static final String[] TYPES = {"p", "r", "n", "b", "q", "k"};
	private static boolean loaded = false;
	private static void loadImage(String key, int[] loadedCount, Runnable onload) {
		Thread.ofVirtual().start(() -> {
			final String pieceFileName = key + ".png";
			final File pieceFile =
				CacheManager.save("images", pieceFileName, PIECE_URL + pieceFileName);
			final Image pieceImage = new Image(pieceFile.toURI().toString());
			Platform.runLater(() -> {
				IMAGES.put(key, pieceImage);
				loadedCount[0]++;
				if (loadedCount[0] == TOTAL_IMAGES) {
					loaded = true;
					if (onload != null)
						onload.run();
				}
			});
		});
	}
	static void loadImages(Runnable onload) {
		if (loaded) {
			if (onload != null)
				onload.run();
			return;
		}
		int[] loadedCount = {0};
		for (String color : COLORS)
			for (String type : TYPES) loadImage(color + type, loadedCount, onload);
	}
	static Image get(String key) {
		return IMAGES.get(key);
	}
	static boolean isLoaded() {
		return loaded;
	}
}