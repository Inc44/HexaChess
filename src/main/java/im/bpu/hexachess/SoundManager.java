package im.bpu.hexachess;

import java.net.URL;
import javafx.scene.media.AudioClip;

public class SoundManager {
	private static final String CLICK_URL =
		"/sounds/mixkit-quick-win-video-game-notification-269.wav";
	private static final AudioClip CLICK = loadClick(CLICK_URL);
	// private static final double SCALING_FACTOR = 9.0;
	private static AudioClip loadClick(final String path) {
		final URL resource = SoundManager.class.getResource(path);
		if (resource == null) {
			return null;
		}
		return new AudioClip(resource.toString());
	}
	private static double calculatePerceivedVolume(double sliderValue) {
		// grows too quickly at low slider values
		// return Math.log10(1 + SCALING_FACTOR * sliderValue); // log10(1) = 0 to log10(10) = 1
		// perceived gain, 3 represents 60dB range, 20dB per 10x power doubling/halving difference
		// return Math.pow(10, 3 * (sliderValue - 1)); // 10^(3*(0-1))=0.001 to 10^(3*(1-1))=1
		return Math.pow(sliderValue, 3); // 0^3=0 to 1^3=1
	}
	public static void playClick() {
		if (CLICK == null) {
			System.err.println("Audio File Not Found Error");
			return;
		}
		double perceivedVolume = calculatePerceivedVolume(SettingsManager.volume);
		CLICK.setVolume(perceivedVolume);
		CLICK.play();
	}
}