package im.bpu.hexachess;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {
	private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
	public static String get(String key, String defaultValue) {
		String environmentValue = System.getenv(key);
		if (environmentValue != null) {
			return environmentValue;
		}
		String dotenvValue = dotenv.get(key);
		if (dotenvValue != null) {
			return dotenvValue;
		}
		return defaultValue;
	}
}