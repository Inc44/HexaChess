package im.bpu.hexachess;

import java.net.URL;
import javafx.scene.media.AudioClip;

public class SoundManager {
	private static final String CLICK_URL =
		"/im/bpu/sounds/mixkit-quick-win-video-game-notification-269.wav";
	private static final AudioClip CLICK = loadClick(CLICK_URL);
	private static AudioClip loadClick(String path) {
		URL resource = SoundManager.class.getResource(path);
		if (resource == null) {
			return null;
		}
		return new AudioClip(resource.toString());
	}
	public static void playClick() {
		if (CLICK == null) {
			System.err.println("Audio File Not Found Error");
			return;
		}
		CLICK.play();
	}
}