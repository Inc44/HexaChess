package im.bpu.hexachess;

import im.bpu.hexachess.model.AI;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Move;

import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static im.bpu.hexachess.Main.loadWindow;

public class HelpWindow {
	private static final double HEX_SIZE = 30;
	@FXML private VBox contentBox;
	@FXML private Button backButton;
	@FXML private Button showBestMoveButton;
	@FXML private Canvas moveCanvas;
	@FXML private Label bestMoveLabel;
	private Move bestMove;
	@FXML
	private void initialize() {
		final ResourceBundle bundle = Main.getBundle();
		// Disable the button if not in the game
		if (State.getState().board == null) {
			showBestMoveButton.setDisable(true);
			bestMoveLabel.setText(bundle.getString("help.game.empty"));
		} else {
			bestMoveLabel.setText(bundle.getString("help.game.title"));
		}
	}
	@FXML
	private void showBestMove() {
		final ResourceBundle bundle = Main.getBundle();
		showBestMoveButton.setDisable(true);
		bestMoveLabel.setText(bundle.getString("help.calculating"));
		Thread.ofVirtual().start(() -> {
			final State state = State.getState();
			final Board board = state.board;
			if (board == null) {
				Platform.runLater(() -> {
					bestMoveLabel.setText(bundle.getString("help.game.empty"));
					showBestMoveButton.setDisable(false);
				});
				return;
			}
			final AI ai = new AI();
			ai.setMaxDepth(SettingsManager.maxDepth);
			bestMove = ai.getBestMove(board, progress -> {});
			Platform.runLater(() -> {
				if (bestMove != null) {
					bestMoveLabel.setText(bundle.getString("help.title") + ": " + bestMove.from.q
						+ "," + bestMove.from.r + " -> " + bestMove.to.q + "," + bestMove.to.r);
					drawBestMove();
				} else {
					bestMoveLabel.setText(bundle.getString("help.empty"));
				}
				showBestMoveButton.setDisable(false);
			});
		});
	}
	private void drawBestMove() {
		if (bestMove == null || moveCanvas == null)
			return;
		final ResourceBundle bundle = Main.getBundle();
		final GraphicsContext gc = moveCanvas.getGraphicsContext2D();
		final double cx = moveCanvas.getWidth() / 2;
		final double cy = moveCanvas.getHeight() / 2;
		// Clear canvas
		gc.clearRect(0, 0, moveCanvas.getWidth(), moveCanvas.getHeight());
		// Draw "from" hex
		drawHex(gc, cx - 60, cy, HEX_SIZE, Color.LIGHTGREEN, bundle.getString("help.from"));
		// Draw arrow
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(2);
		gc.strokeLine(cx - 30, cy, cx + 30, cy);
		gc.strokeLine(cx + 20, cy - 5, cx + 30, cy);
		gc.strokeLine(cx + 20, cy + 5, cx + 30, cy);
		// Draw "to" hex
		drawHex(gc, cx + 60, cy, HEX_SIZE, Color.LIGHTBLUE, bundle.getString("help.to"));
	}
	private void drawHex(final GraphicsContext gc, final double x, final double y,
		final double size, final Color color, final String label) {
		final double[] xPoints = new double[6];
		final double[] yPoints = new double[6];
		for (int i = 0; i < 6; i++) {
			final double angle = Math.PI / 3 * i;
			xPoints[i] = x + size * Math.cos(angle);
			yPoints[i] = y + size * Math.sin(angle);
		}
		gc.setFill(color);
		gc.fillPolygon(xPoints, yPoints, 6);
		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);
		gc.strokePolygon(xPoints, yPoints, 6);
		gc.setFill(Color.BLACK);
		gc.fillText(label, x - 15, y + 5);
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
}