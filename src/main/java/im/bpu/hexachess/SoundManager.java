package im.bpu.hexachess;

import java.net.URL;
import javafx.scene.media.AudioClip;

public class SoundManager {
	public static void playClick() {
		try {
			URL resource = SoundManager.class.getResource(
				"/im/bpu/sounds/mixkit-quick-win-video-game-notification-269.wav");
			if (resource != null) {
				AudioClip clip = new AudioClip(resource.toString());
				clip.play();
			} else {
				System.out.println("Erreur : Fichier son introuvable !");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}