package im.bpu.hexachess;

import java.util.prefs.Preferences;

public class SettingsManager {
	private static final Preferences PREFERENCES =
		Preferences.userNodeForPackage(SettingsManager.class);
	public static int maxDepth = PREFERENCES.getInt("maxDepth", 3);
	public static double volume = PREFERENCES.getDouble("volume", 1.0);
	public static String theme = PREFERENCES.get("theme", "Light");
	public static String language = PREFERENCES.get("language", "English");
	public static String playerId = PREFERENCES.get("playerId", null);
	public static String userHandle = PREFERENCES.get("userHandle", null);
	public static String authToken = PREFERENCES.get("authToken", null);
	public static void setMaxDepth(int value) {
		if (maxDepth != value) {
			maxDepth = value;
			PREFERENCES.putInt("maxDepth", value);
		}
	}
	public static void setVolume(double value) {
		if (volume != value) {
			volume = value;
			PREFERENCES.putDouble("volume", value);
		}
	}
	public static void setTheme(String value) {
		if (!theme.equals(value)) {
			theme = value;
			PREFERENCES.put("theme", value);
		}
	}
	public static void setLanguage(String value) {
		if (!language.equals(value)) {
			language = value;
			PREFERENCES.put("language", value);
		}
	}
	public static void setPlayerId(String value) {
		if (playerId != value) {
			playerId = value;
			update("playerId", value);
		}
	}
	public static void setUserHandle(String value) {
		if (userHandle != value) {
			userHandle = value;
			update("userHandle", value);
		}
	}
	public static void setAuthToken(String value) {
		if (authToken != value) {
			authToken = value;
			update("authToken", value);
		}
	}
	private static void update(String key, String value) {
		if (value != null)
			PREFERENCES.put(key, value);
		else
			PREFERENCES.remove(key);
	}
}