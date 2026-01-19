package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.State;
import im.bpu.hexachess.model.AxialCoordinate;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Move;
import im.bpu.hexachess.model.Piece;
import im.bpu.hexachess.model.PieceType;
import im.bpu.hexachess.ui.HexPanel;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.loadWindow;

public class PuzzleWindow {
	@FXML private Canvas canvas;
	@FXML private Label statusLabel;
	@FXML private Button backButton;
	@FXML private Button resetButton;
	@FXML private VBox successContainer;
	private HexPanel hexPanel;
	// Gestion des niveaux (On va jusqu'au niveau 4)
	private int currentLevel = 1;
	private boolean isLevelFinished = false;
	// Structure du scénario
	private record PuzzleStep(List<String> whiteMoveOptions, String blackResponseStr) {}
	private final Queue<PuzzleStep> solution = new LinkedList<>();
	@FXML
	private void initialize() {
		final ResourceBundle bundle = Main.getBundle();
		// On réinitialise l'état de victoire pour ce niveau
		isLevelFinished = false;
		// 1. On prépare le scénario selon le niveau
		setupPuzzleScenario();
		// 2. On configure l'état global
		State state = State.getState();
		state.clear();
		state.isPuzzleMode = true; // IMPORTANT : Active le mode puzzle
		// 3. On place les pièces selon le niveau
		setupBoardPieces(state.board);
		// 4. On lance l'affichage
		hexPanel = new HexPanel(canvas, state, progress -> {}, loading -> {}, gameover -> {});
		// 5. On branche l'écouteur de coups
		hexPanel.onPuzzleMove = this::handlePlayerMove;
		// Mise à jour de l'interface
		updateStatusText();
		statusLabel.setStyle("-fx-text-fill: white;");
		resetButton.setText(bundle.getString("window.puzzles.resetButton"));
	}
	private void updateStatusText() {
		if (currentLevel == 4) {
			statusLabel.setText("Niveau " + currentLevel + " : Mat en 2 coups ! (Difficile)");
		} else {
			statusLabel.setText("Niveau " + currentLevel + " : Mat en 1 coup !");
		}
	}
	private void setupBoardPieces(Board board) {
		board.pieces.clear();
		if (currentLevel == 1) {
			// --- NIVEAU 1 : Le Baiser de la mort (Dame) ---
			board.pieces.put(new AxialCoordinate(0, -5), new Piece(PieceType.KING, false));
			board.pieces.put(new AxialCoordinate(0, -3), new Piece(PieceType.KING, true));
			board.pieces.put(new AxialCoordinate(2, -2), new Piece(PieceType.QUEEN, true));
		} else if (currentLevel == 2) {
			// --- NIVEAU 2 : Mat du couloir (Tour) ---
			board.pieces.put(new AxialCoordinate(0, -5), new Piece(PieceType.KING, false));
			board.pieces.put(new AxialCoordinate(0, -3), new Piece(PieceType.KING, true));
			board.pieces.put(new AxialCoordinate(-1, 0), new Piece(PieceType.ROOK, true));
		} else if (currentLevel == 3) {
			// --- NIVEAU 3 : Mat à l'étouffée (Cavalier) ---
			// Le Roi noir est coincé par ses propres pions
			board.pieces.put(new AxialCoordinate(0, -5), new Piece(PieceType.KING, false));
			board.pieces.put(
				new AxialCoordinate(0, -4), new Piece(PieceType.PAWN, false)); // Bloque
			board.pieces.put(
				new AxialCoordinate(1, -4), new Piece(PieceType.PAWN, false)); // Bloque
			board.pieces.put(
				new AxialCoordinate(-2, -5), new Piece(PieceType.KING, true)); // Bloque
			// Le Cavalier blanc doit sauter pour mater
			board.pieces.put(new AxialCoordinate(-2, -3), new Piece(PieceType.KNIGHT, true));
		} else if (currentLevel == 4) {
			// --- NIVEAU 4 : Combinaison Dame + Tour (Mat en 2) ---
			board.pieces.put(
				new AxialCoordinate(0, -5), new Piece(PieceType.KING, false)); // Roi Noir
			board.pieces.put(new AxialCoordinate(-1, -4), new Piece(PieceType.PAWN, false));
			board.pieces.put(new AxialCoordinate(0, -4), new Piece(PieceType.PAWN, false));
			board.pieces.put(new AxialCoordinate(1, -4), new Piece(PieceType.PAWN, false));
			board.pieces.put(new AxialCoordinate(2, 3), new Piece(PieceType.ROOK, false));
			// La Dame doit forcer le Roi à bouger
			board.pieces.put(new AxialCoordinate(0, 4), new Piece(PieceType.KING, true));
			board.pieces.put(new AxialCoordinate(2, -2), new Piece(PieceType.QUEEN, true));
			board.pieces.put(new AxialCoordinate(-4, -2), new Piece(PieceType.BISHOP, true));
			board.pieces.put(new AxialCoordinate(-1, 0), new Piece(PieceType.ROOK, true));
		}
	}
	private void setupPuzzleScenario() {
		solution.clear();
		if (currentLevel == 1) {
			// Dame se colle au Roi
			solution.add(new PuzzleStep(List.of("2,-2->0,-4", "2,-2->1,-3", "2,-2->-1,-5"), null));
		} else if (currentLevel == 2) {
			// Tour descend au fond
			solution.add(new PuzzleStep(List.of("-1,0->-1,-5"), null));
		} else if (currentLevel == 3) {
			// Cavalier saute en -1,-5 (Mouvement en L hexago : 2 cases tout droit, 1 côté)
			// -2,-3 -> -1,-5 est un saut valide
			solution.add(new PuzzleStep(List.of("-2,-3->1,-2"), null));
		} else if (currentLevel == 4) {
			// COUP 1 : Dame fait échec en (1, -4)
			// Le Roi noir est obligé de fuir en (-1, -4) car (0, -4) est attaqué
			solution.add(new PuzzleStep(List.of("-1,0->-1,-4"),
				"2,3->2,-2" // Réponse forcée des noirs
				));
			solution.add(new PuzzleStep(List.of("-1,-4->-1,-5"), null));
		}
	}
	private void handlePlayerMove(Move move) {
		PuzzleStep currentStep = solution.peek();
		if (currentStep == null)
			return;
		String playedMoveStr = move.from.q + "," + move.from.r + "->" + move.to.q + "," + move.to.r;
		System.out.println("Niveau " + currentLevel + " - Joué: " + playedMoveStr);
		if (currentStep.whiteMoveOptions().contains(playedMoveStr)) {
			processSuccessMove(currentStep);
		} else {
			processWrongMove();
		}
	}
	private void processSuccessMove(PuzzleStep step) {
		final ResourceBundle bundle = Main.getBundle();
		solution.poll();
		if (solution.isEmpty()) {
			isLevelFinished = true;
			statusLabel.setText(bundle.getString("window.puzzles.successLabel"));
			statusLabel.setStyle("-fx-text-fill: #2eda8e;"); // Vert
			successContainer.setVisible(true);
			successContainer.setManaged(true);
			if (currentLevel < 4) {
				resetButton.setText(bundle.getString("window.puzzles.next"));
			} else {
				resetButton.setText(bundle.getString("window.puzzles.finish"));
			}
			return;
		}
		statusLabel.setText("Bien joué ! Continue...");
		if (step.blackResponseStr() != null) {
			Thread.ofVirtual().start(() -> {
				try {
					Thread.sleep(600);
					Platform.runLater(() -> playBlackMove(step.blackResponseStr()));
				} catch (InterruptedException exception) {
					exception.printStackTrace();
				}
			});
		}
	}
	private void processWrongMove() {
		final ResourceBundle bundle = Main.getBundle();
		statusLabel.setText(bundle.getString("window.puzzles.wrong"));
		statusLabel.setStyle("-fx-text-fill: #f05142;");
		Thread.ofVirtual().start(() -> {
			try {
				Thread.sleep(500);
				Platform.runLater(() -> {
					hexPanel.rewind();
					statusLabel.setStyle("-fx-text-fill: white;");
					statusLabel.setText(bundle.getString("window.puzzles.retry"));
				});
			} catch (InterruptedException exception) {
			}
		});
	}
	private void playBlackMove(String moveStr) {
		String[] parts = moveStr.split("->");
		String[] fromParts = parts[0].split(",");
		String[] toParts = parts[1].split(",");
		AxialCoordinate from =
			new AxialCoordinate(Integer.parseInt(fromParts[0]), Integer.parseInt(fromParts[1]));
		AxialCoordinate to =
			new AxialCoordinate(Integer.parseInt(toParts[0]), Integer.parseInt(toParts[1]));
		State.getState().board.movePiece(from, to);
		State.getState().history.push(new Board(State.getState().board));
		hexPanel.repaint();
	}
	@FXML
	private void resetPuzzle() {
		if (isLevelFinished) {
			if (currentLevel < 4) {
				nextLevel();
			} else {
				openMain();
			}
		} else {
			// Reset du niveau actuel
			initialize();
			successContainer.setVisible(false);
			successContainer.setManaged(false);
		}
	}
	private void nextLevel() {
		currentLevel++;
		successContainer.setVisible(false);
		successContainer.setManaged(false);
		initialize();
	}
	@FXML
	private void openMain() {
		State.getState().isPuzzleMode = false;
		loadWindow("/fxml/window/mainWindow.fxml", new MainWindow(), backButton);
	}
}