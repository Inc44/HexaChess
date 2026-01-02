package im.bpu.hexachess;

import java.util.prefs.Preferences;

public class Settings {
	private static final Preferences prefs = Preferences.userNodeForPackage(Settings.class);
	public static int maxDepth = prefs.getInt("maxDepth", 3);
	public static String userHandle = prefs.get("userHandle", null);
	public static String authToken = prefs.get("authToken", null);
	public static void save() {
		prefs.putInt("maxDepth", maxDepth);
		if (userHandle != null)
			prefs.put("userHandle", userHandle);
		else
			prefs.remove("userHandle");
		if (authToken != null)
			prefs.put("authToken", authToken);
		else
			prefs.remove("authToken");
	}
}