package im.bpu.hexachess.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class CacheManager {
	private static final Path CACHE_DIR =
		Path.of(System.getProperty("user.home"), ".hexachess", "cache");
	public static File get(String category, String key) {
		final Path dir = CACHE_DIR.resolve(category);
		try {
			Files.createDirectories(dir);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		final Path filePath = dir.resolve(key);
		final File file = filePath.toFile();
		return file;
	}
	public static File save(String category, String key, String url) {
		File file = get(category, key);
		if (file.exists() && file.length() > 0)
			return file;
		try (InputStream is = URI.create(url).toURL().openStream();
			FileOutputStream fos = new FileOutputStream(file)) {
			is.transferTo(fos);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return file;
	}
}