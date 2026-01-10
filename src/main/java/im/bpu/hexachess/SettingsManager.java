package im.bpu.hexachess;

import java.util.prefs.Preferences;

public class SettingsManager {
	private static final Preferences prefs = Preferences.userNodeForPackage(SettingsManager.class);
	public static int maxDepth = prefs.getInt("maxDepth", 3);
	public static String playerId = prefs.get("playerId", null);
	public static String userHandle = prefs.get("userHandle", null);
	public static String authToken = prefs.get("authToken", null);
	public static void save() {
		prefs.putInt("maxDepth", maxDepth);
		update("playerId", playerId);
		update("userHandle", userHandle);
		update("authToken", authToken);
	}
	private static void update(String key, String value) {
		if (value != null)
			prefs.put(key, value);
		else
			prefs.remove(key);
	}
}