package im.bpu.hexachess.ui;

import im.bpu.hexachess.Settings;
import im.bpu.hexachess.State;
import im.bpu.hexachess.model.AI;
import im.bpu.hexachess.model.AxialCoordinate;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Move;
import im.bpu.hexachess.model.Piece;
import im.bpu.hexachess.network.API;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Screen;

public class HexPanel {
	private State state;
	private AI ai = new AI();
	private HexGeometry geometry;
	private HexRenderer renderer;
	private AxialCoordinate selected;
	private List<AxialCoordinate> highlighted = new ArrayList<>();
	private Canvas canvas;
	private boolean isLockedIn = false;
	private String lastSyncedMoveString = "";
	public HexPanel(Canvas canvas, State state) {
		this.state = state;
		this.ai.setMaxDepth(Settings.maxDepth);
		double width = Screen.getPrimary().getBounds().getWidth();
		double height = Screen.getPrimary().getBounds().getHeight();
		double aspectRatio = width / height;
		this.geometry = new HexGeometry(aspectRatio > 1.5 ? 32 : 24);
		this.renderer = new HexRenderer(geometry, state.board);
		this.canvas = canvas;
		PieceImageLoader.loadImages(this::repaint);
		canvas.setOnMouseClicked(event -> handleMouseClick(event.getX(), event.getY()));
		if (state.isMultiplayer && state.board.isWhiteTurn != state.isWhitePlayer)
			startPolling();
		repaint();
		// accumulate opacity to remove hex gaps
		repaint();
	}
	private void drawBoard(GraphicsContext gc, double cx, double cy) {
		for (int q = -5; q <= 5; q++)
			for (int r = -5; r <= 5; r++) {
				AxialCoordinate coord = new AxialCoordinate(q, r);
				if (coord.isValid())
					renderer.drawHex(gc, cx, cy, coord, selected, highlighted);
			}
	}
	private void repaint() {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double cx = canvas.getWidth() / 2;
		double cy = canvas.getHeight() / 2;
		drawBoard(gc, cx, cy);
		renderer.drawBoardBorder(gc, cx, cy);
	}
	private void deselect() {
		selected = null;
		highlighted.clear();
		repaint();
	}
	private void executeMove(AxialCoordinate target) {
		if (isLockedIn)
			return;
		state.history.push(new Board(state.board));
		state.board.movePiece(selected, target);
		deselect();
		isLockedIn = true;
		String moveString = selected.q + "," + selected.r + "->" + target.q + "," + target.r;
		if (state.isMultiplayer) {
			new Thread(() -> {
				API.sendMove(state.gameId, moveString);
				lastSyncedMoveString = moveString;
				startPolling();
			}).start();
		} else {
			new Thread(() -> {
				Move bestMove = ai.getBestMove(state.board);
				Platform.runLater(() -> {
					if (bestMove != null)
						state.board.movePiece(bestMove.from, bestMove.to);
					isLockedIn = false;
					repaint();
				});
			}).start();
		}
	}
	private void startPolling() {
		isLockedIn = true;
		new Thread(() -> {
			while (true) {
				String moveString = API.getMove(state.gameId);
				if (moveString != null && !moveString.isEmpty()
					&& !moveString.equals(lastSyncedMoveString)) {
					lastSyncedMoveString = moveString;
					String[] moveStrings = moveString.split("->");
					String[] fromString = moveStrings[0].split(",");
					String[] toString = moveStrings[1].split(",");
					AxialCoordinate from = new AxialCoordinate(
						Integer.parseInt(fromString[0]), Integer.parseInt(fromString[1]));
					AxialCoordinate to = new AxialCoordinate(
						Integer.parseInt(toString[0]), Integer.parseInt(toString[1]));
					Platform.runLater(() -> {
						state.board.movePiece(from, to);
						isLockedIn = false;
						repaint();
					});
					break;
				}
				try {
					Thread.sleep(2000);
				} catch (Exception ignored) { // high-frequency polling operation
				}
			}
		}).start();
	}
	private void selectPiece(AxialCoordinate coord) {
		selected = coord;
		highlighted.clear();
		for (Move m : state.board.listMoves(state.board.isWhiteTurn))
			if (m.from.equals(coord))
				highlighted.add(m.to);
		repaint();
	}
	private void handleMouseClick(double x, double y) {
		if (isLockedIn)
			return;
		double cx = canvas.getWidth() / 2;
		double cy = canvas.getHeight() / 2;
		AxialCoordinate clicked = geometry.pixelToHex(x, y, cx, cy);
		if (!clicked.isValid()) {
			deselect();
			return;
		}
		if (selected != null && highlighted.contains(clicked)) {
			executeMove(clicked);
			return;
		}
		Piece piece = state.board.getPiece(clicked);
		if ((piece != null && piece.isWhite == state.board.isWhiteTurn)
			&& (!state.isMultiplayer || piece.isWhite == state.isWhitePlayer))
			selectPiece(clicked);
		else
			deselect();
	}
	public void restart() {
		if (isLockedIn)
			return;
		state.clear();
		ai.setMaxDepth(Settings.maxDepth);
		renderer.setBoard(state.board);
		deselect();
	}
	public void rewind() {
		if (isLockedIn || state.history.isEmpty())
			return;
		state.board = state.history.pop();
		renderer.setBoard(state.board);
		deselect();
	}
}