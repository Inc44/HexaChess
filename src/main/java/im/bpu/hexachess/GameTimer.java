package im.bpu.hexachess;

import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;

public class GameTimer {
	private static final int DEFAULT_TIMER_SECONDS = 600;
	private int playerTimeSeconds = DEFAULT_TIMER_SECONDS;
	private int opponentTimeSeconds = DEFAULT_TIMER_SECONDS;
	private final Label playerTimerLabel;
	private final Label opponentTimerLabel;
	private long lastTimerUpdate = 0;
	private final AnimationTimer timer;
	public GameTimer(Label playerTimerLabel, Label opponentTimerLabel) {
		this.playerTimerLabel = playerTimerLabel;
		this.opponentTimerLabel = opponentTimerLabel;
		this.timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (lastTimerUpdate == 0) {
					lastTimerUpdate = now;
					return;
				}
				if (now - lastTimerUpdate >= 1_000_000_000L) {
					lastTimerUpdate = now;
					tick();
				}
			}
		};
		updateLabels();
	}
	private void tick() {
		final State state = State.getState();
		if (state.board == null)
			return;
		final boolean isWhiteTurn = state.board.isWhiteTurn;
		final boolean isPlayerWhite = state.isWhitePlayer;
		if (isWhiteTurn == isPlayerWhite) {
			if (playerTimeSeconds > 0) {
				playerTimeSeconds--;
			}
		} else {
			if (opponentTimeSeconds > 0) {
				opponentTimeSeconds--;
			}
		}
		updateLabels();
		if (playerTimeSeconds == 0 || opponentTimeSeconds == 0) {
			stop();
			final ResourceBundle bundle = Main.getBundle();
			System.out.println(bundle.getString("main.timer"));
		}
	}
	private void updateLabels() {
		updateLabel(playerTimerLabel, playerTimeSeconds);
		updateLabel(opponentTimerLabel, opponentTimeSeconds);
	}
	private void updateLabel(Label label, int timeSeconds) {
		if (label == null)
			return;
		final int minutes = timeSeconds / 60;
		final int seconds = timeSeconds % 60;
		final String timeString = String.format("%02d:%02d", minutes, seconds);
		label.setText(timeString);
	}
	public void start() {
		lastTimerUpdate = 0;
		timer.start();
	}
	public void stop() {
		timer.stop();
	}
	public void reset() {
		stop();
		playerTimeSeconds = DEFAULT_TIMER_SECONDS;
		opponentTimeSeconds = DEFAULT_TIMER_SECONDS;
		updateLabels();
	}
}