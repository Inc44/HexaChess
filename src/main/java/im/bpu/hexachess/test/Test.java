package im.bpu.hexachess.test;

import im.bpu.hexachess.State;
import im.bpu.hexachess.dao.AchievementDAO;
import im.bpu.hexachess.dao.GameDAO;
import im.bpu.hexachess.dao.PlayerDAO;
import im.bpu.hexachess.dao.SettingsDAO;
import im.bpu.hexachess.dao.TournamentDAO;
import im.bpu.hexachess.entity.Achievement;
import im.bpu.hexachess.entity.Game;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Puzzle;
import im.bpu.hexachess.entity.Settings;
import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.manager.SoundManager;
import im.bpu.hexachess.model.AI;
import im.bpu.hexachess.model.AxialCoordinate;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Move;
import im.bpu.hexachess.model.Piece;
import im.bpu.hexachess.model.PieceType;
import im.bpu.hexachess.ui.controller.HelpWindow;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import org.mindrot.jbcrypt.BCrypt;

public class Test {
	private static int passedTests = 0;
	private static int failedTests = 0;
	private static final List<String> failures = new ArrayList<>();
	public static void main(final String[] args) {
		System.out.println("=== HexaChess Test Suite ===\n");
		// Initialiser JavaFX pour les tests UI
		try {
			new JFXPanel();
			Platform.runLater(() -> {});
		} catch (final Exception exception) {
			System.err.println(
				"Note: JavaFX initialization failed or already initialized (continuing...)");
		}
		// Tests de la logique du jeu
		testBoardInitialization();
		testPieceMovement();
		testPawnMovement();
		testEnPassant();
		testPromotion();
		testCheckValidMoves();
		testMoveGeneration();
		testIllegalMoveCheck();
		// Tests des coordonnées
		testAxialCoordinates();
		// Tests de l'IA
		testAIInitialization();
		testAIEvaluation();
		// Test sécurité
		testPasswordHashing();
		testSoundManagerLogic();
		// Tests des entités
		testPlayerEntity();
		testGameEntity();
		testTournamentEntity();
		testPuzzleEntity();
		testSettingsEntity();
		// Tests DAO (si base de données disponible)
		testPlayerDAO();
		testGameDAO();
		testTournamentDAO();
		testSettingsDAO();
		testAchievementDAO();
		// Tests State
		testStateSingleton();
		testStateClear();
		// Tests HelpWindow
		testHelpWindowUILogic();
		testHelpWindowDraw();
		// Résultats
		printResults();
		// Nécessaire pour arrêter le thread JavaFX
		System.exit(0);
	}
	// ============ Tests Logique du Jeu ============
	private static void testBoardInitialization() {
		try {
			final Board board = new Board();
			// Vérifier que toutes les pièces sont placées
			int whitePieces = 0;
			int blackPieces = 0;
			for (final Piece piece : board.pieces.values()) {
				if (piece.isWhite)
					whitePieces++;
				else
					blackPieces++;
			}
			assert whitePieces == 16 : "Expected 16 white pieces, got " + whitePieces;
			assert blackPieces == 16 : "Expected 16 black pieces, got " + blackPieces;
			// Vérifier que c'est au tour des blancs
			assert board.isWhiteTurn : "White should start";
			pass("Board initialization");
		} catch (final AssertionError | Exception exception) {
			fail("Board initialization", exception.getMessage());
		}
	}
	private static void testPieceMovement() {
		try {
			final Board board = new Board();
			// Déplacer un pion blanc
			final AxialCoordinate from = new AxialCoordinate(1, 1);
			final AxialCoordinate to = new AxialCoordinate(0, 0);
			final Piece piece = board.getPiece(from);
			assert piece != null : "Piece should exist at starting position";
			assert piece.type == PieceType.PAWN : "Piece should be a pawn";
			board.movePiece(from, to);
			assert board.getPiece(from) == null : "Starting position should be empty";
			assert board.getPiece(to) != null : "Target position should have piece";
			assert !board.isWhiteTurn : "Turn should switch to black";
			pass("Piece movement");
		} catch (final AssertionError | Exception exception) {
			fail("Piece movement", exception.getMessage());
		}
	}
	private static void testPawnMovement() {
		try {
			final Board board = new Board();
			// Test mouvement simple du pion
			final AxialCoordinate pawnPos = new AxialCoordinate(1, 1);
			final List<Move> moves = board.listMoves(true);
			boolean canMoveForward = false;
			for (final Move move : moves) {
				if (move.from.equals(pawnPos)) {
					canMoveForward = true;
					break;
				}
			}
			assert canMoveForward : "Pawn should be able to move";
			pass("Pawn movement");
		} catch (final AssertionError | Exception exception) {
			fail("Pawn movement", exception.getMessage());
		}
	}
	private static void testEnPassant() {
		try {
			final Board board = new Board();
			assert board.enPassant == null : "enPassant should be null initially";
			pass("En passant initialization");
		} catch (final AssertionError | Exception exception) {
			fail("En passant", exception.getMessage());
		}
	}
	private static void testPromotion() {
		try {
			final Board board = new Board();
			// Placer un pion blanc en position de promotion
			final AxialCoordinate promotionPos = new AxialCoordinate(-5, 4);
			final Piece pawn = new Piece(PieceType.PAWN, true);
			board.pieces.put(promotionPos, pawn);
			// Déplacer vers case de promotion
			final AxialCoordinate targetPos = new AxialCoordinate(-5, 3);
			board.movePiece(promotionPos, targetPos);
			final Piece promoted = board.getPiece(targetPos);
			assert promoted.type == PieceType.QUEEN : "Pawn should be promoted to queen";
			pass("Pawn promotion");
		} catch (final AssertionError | Exception exception) {
			fail("Pawn promotion", exception.getMessage());
		}
	}
	private static void testCheckValidMoves() {
		try {
			final Board board = new Board();
			final List<Move> whiteMoves = board.listMoves(true);
			final List<Move> blackMoves = board.listMoves(false);
			assert !whiteMoves.isEmpty() : "White should have legal moves";
			assert !blackMoves.isEmpty() : "Black should have legal moves";
			pass("Valid moves generation");
		} catch (final AssertionError | Exception exception) {
			fail("Valid moves generation", exception.getMessage());
		}
	}
	private static void testMoveGeneration() {
		try {
			final Board board = new Board();
			final List<Move> initialMoves = board.listMoves(true);
			assert initialMoves.size() > 0 : "Should have moves at start";
			pass("Move generation count");
		} catch (final AssertionError | Exception exception) {
			fail("Move generation", exception.getMessage());
		}
	}
	private static void testSoundManagerLogic() {
		try {
			// Test de la formule logarithmique de volume via réflexion car la méthode est privée
			final Method calcMethod =
				SoundManager.class.getDeclaredMethod("calculatePerceivedVolume", double.class);
			calcMethod.setAccessible(true);
			final double vol0 = (double) calcMethod.invoke(null, 0.0);
			final double vol1 = (double) calcMethod.invoke(null, 1.0);
			assert vol0 < vol1 : "Volume should increase with slider value";
			assert vol0 >= 0.0 && vol1 <= 1.0 : "Volume must be bounded";
			pass("SoundManager volume logic");
		} catch (final Exception exception) {
			// Si SoundManager n'est pas trouvable ou erreur de réflexion
			fail("SoundManager logic", exception.getMessage());
		}
	}
	private static void testIllegalMoveCheck() {
		try {
			final Board board = new Board();
			// On vide le plateau pour un scénario précis
			board.pieces.clear();
			// Scénario : Roi blanc en (0,0), Tour noire en (0,5) qui l'attaque
			// Le Roi ne devrait pas pouvoir aller en (0,1) car toujours sur la ligne de la tour
			final AxialCoordinate kingPos = new AxialCoordinate(0, 0);
			final AxialCoordinate rookPos = new AxialCoordinate(0, 4); // Tour en ligne droite
			board.pieces.put(kingPos, new Piece(PieceType.KING, true));
			board.pieces.put(rookPos, new Piece(PieceType.ROOK, false));
			// Vérifier que le système détecte l'échec si on bouge
			final Move illegalMove = new Move(kingPos, new AxialCoordinate(0, 1));
			// isMoveIntoCheck renvoie true si le coup met le roi en danger
			assert board.isMoveIntoCheck(illegalMove, true)
				: "Move exposing King to check should be detected as dangerous";
			pass("Illegal move detection (Check)");
		} catch (final AssertionError | Exception exception) {
			fail("Illegal move detection", exception.getMessage());
		}
	}
	// ============ Tests Coordonnées ============
	private static void testAxialCoordinates() {
		try {
			final AxialCoordinate coord = new AxialCoordinate(0, 0);
			assert coord.q == 0 : "Q should be 0";
			assert coord.r == 0 : "R should be 0";
			final AxialCoordinate sum = coord.add(1, 1);
			assert sum.q == 1 && sum.r == 1 : "Addition failed";
			final AxialCoordinate invalid = new AxialCoordinate(10, 10);
			assert !invalid.isValid() : "Should be invalid";
			final AxialCoordinate valid = new AxialCoordinate(0, 0);
			assert valid.isValid() : "Should be valid";
			pass("Axial coordinates");
		} catch (final AssertionError | Exception exception) {
			fail("Axial coordinates", exception.getMessage());
		}
	}
	// ============ Tests IA ============
	private static void testAIInitialization() {
		try {
			final AI ai = new AI();
			ai.setMaxDepth(3);
			pass("AI initialization");
		} catch (final Exception exception) {
			fail("AI initialization", exception.getMessage());
		}
	}
	private static void testAIEvaluation() {
		try {
			final AI ai = new AI();
			ai.setMaxDepth(1);
			final Board board = new Board();
			final Move bestMove = ai.getBestMove(board, progress -> {});
			assert bestMove != null : "AI should find a move";
			pass("AI evaluation");
		} catch (final AssertionError | Exception exception) {
			fail("AI evaluation", exception.getMessage());
		}
	}
	// ============ Tests Entités ============
	private static void testPlayerEntity() {
		try {
			final Player player = new Player("testId123", "testHandle", "test@example.com", "hash",
				1200, false, LocalDateTime.now());
			assert player.getPlayerId().equals("testId123") : "Player ID mismatch";
			pass("Player entity");
		} catch (final AssertionError | Exception exception) {
			fail("Player entity", exception.getMessage());
		}
	}
	private static void testGameEntity() {
		try {
			final Game game = new Game(
				"gameId123", "whiteId", "blackId", null, null, "", LocalDateTime.now(), null, null);
			assert game.getGameId().equals("gameId123") : "Game ID mismatch";
			pass("Game entity");
		} catch (final AssertionError | Exception exception) {
			fail("Game entity", exception.getMessage());
		}
	}
	private static void testTournamentEntity() {
		try {
			final Tournament tournament =
				new Tournament("tournId123", "Test", "Desc", LocalDateTime.now(), null, null);
			assert tournament.getTournamentId().equals("tournId123") : "Tournament ID mismatch";
			pass("Tournament entity");
		} catch (final AssertionError | Exception exception) {
			fail("Tournament entity", exception.getMessage());
		}
	}
	private static void testPuzzleEntity() {
		try {
			final Puzzle puzzle = new Puzzle("pid123", "m", "s", 1500, "t", LocalDateTime.now());
			assert puzzle.getPuzzleId().equals("pid123") : "Puzzle ID mismatch";
			pass("Puzzle entity");
		} catch (final AssertionError | Exception exception) {
			fail("Puzzle entity", exception.getMessage());
		}
	}
	private static void testSettingsEntity() {
		try {
			final Settings settings = new Settings("pid123", "dark", true, false, 3);
			assert settings.getPlayerId().equals("pid123") : "Player ID mismatch";
			pass("Settings entity");
		} catch (final AssertionError | Exception exception) {
			fail("Settings entity", exception.getMessage());
		}
	}
	// ============ Tests DAO ============
	private static void testPlayerDAO() {
		try {
			final PlayerDAO playerDAO = new PlayerDAO();
			final Player player =
				new Player("testDAO123", "h", "e@e.com", "pw", 1200, false, LocalDateTime.now());
			playerDAO.create(player);
			assert playerDAO.read("testDAO123") != null : "Player read failed";
			playerDAO.delete(player);
			pass("PlayerDAO CRUD operations");
		} catch (final Exception exception) {
			fail("PlayerDAO", exception.getMessage() + " (DB unavailable?)");
		}
	}
	private static void testGameDAO() {
		try {
			final GameDAO gameDAO = new GameDAO();
			final Game game =
				new Game("testGameDAO", "1", "2", null, null, "", LocalDateTime.now(), null, null);
			gameDAO.create(game);
			assert gameDAO.read("testGameDAO") != null : "Game read failed";
			gameDAO.delete(game);
			pass("GameDAO CRUD operations");
		} catch (final Exception exception) {
			fail("GameDAO", exception.getMessage() + " (DB unavailable?)");
		}
	}
	private static void testTournamentDAO() {
		try {
			final TournamentDAO tournamentDAO = new TournamentDAO();
			final Tournament tournament =
				new Tournament("testTournDAO", "N", "D", LocalDateTime.now(), null, null);
			tournamentDAO.create(tournament);
			assert tournamentDAO.read("testTournDAO") != null : "Tournament read failed";
			tournamentDAO.delete(tournament);
			pass("TournamentDAO CRUD operations");
		} catch (final Exception exception) {
			fail("TournamentDAO", exception.getMessage() + " (DB unavailable?)");
		}
	}
	private static void testSettingsDAO() {
		try {
			final SettingsDAO settingsDAO = new SettingsDAO();
			final Settings settings = new Settings("00000000001", "d", true, false, 3);
			settingsDAO.create(settings);
			assert settingsDAO.read("00000000001") != null : "Settings read failed";
			settingsDAO.delete(settings);
			pass("SettingsDAO CRUD operations");
		} catch (final Exception exception) {
			fail("SettingsDAO", exception.getMessage() + " (DB unavailable?)");
		}
	}
	private static void testAchievementDAO() {
		try {
			final AchievementDAO achievementDAO = new AchievementDAO();
			final Achievement achievement =
				new Achievement("TEST_ACH_001", "Test Name", "Desc", false);
			// Création
			achievementDAO.create(achievement);
			// Lecture
			final Achievement read = achievementDAO.read("TEST_ACH_001");
			assert read != null : "Achievement read failed";
			assert read.getName().equals("Test Name") : "Achievement content mismatch";
			// Nettoyage
			achievementDAO.delete(achievement);
			pass("AchievementDAO CRUD operations");
		} catch (final Exception exception) {
			fail("AchievementDAO", exception.getMessage() + " (DB unavailable?)");
		}
	}
	// ============ Tests State ============
	private static void testStateSingleton() {
		try {
			final State state1 = State.getState();
			final State state2 = State.getState();
			assert state1 == state2 : "State should be singleton";
			pass("State singleton");
		} catch (final AssertionError | Exception exception) {
			fail("State singleton", exception.getMessage());
		}
	}
	private static void testStateClear() {
		try {
			final State state = State.getState();
			state.isMultiplayer = true;
			state.gameId = "testGame";
			state.clear();
			assert state.board != null : "Board should exist after clear";
			assert !state.isMultiplayer : "Should not be multiplayer";
			pass("State clear");
		} catch (final AssertionError | Exception exception) {
			fail("State clear", exception.getMessage());
		}
	}
	// ============ Tests HelpWindow ============
	private static void testHelpWindowUILogic() {
		final CountDownLatch latch = new CountDownLatch(1);
		final AssertionError[] error = {null};
		Platform.runLater(() -> {
			try {
				final HelpWindow helpWindow = new HelpWindow();
				final Button showBestMoveButton = new Button();
				final Label bestMoveLabel = new Label();
				injectField(helpWindow, "showBestMoveButton", showBestMoveButton);
				injectField(helpWindow, "bestMoveLabel", bestMoveLabel);
				// Test sans partie
				State.getState().board = null;
				invokeMethod(helpWindow, "initialize");
				if (!showBestMoveButton.isDisabled())
					throw new AssertionError("Button should be disabled (no game)");
				if (!bestMoveLabel.getText().toLowerCase().contains("start"))
					throw new AssertionError("Label incorrect (no game)");
				// Test avec partie
				State.getState().board = new Board();
				showBestMoveButton.setDisable(false); // Reset manuel post-initialize
				invokeMethod(helpWindow, "showBestMove");
				if (!showBestMoveButton.isDisabled())
					throw new AssertionError("Button should be disabled (calculating)");
				if (!bestMoveLabel.getText().toLowerCase().contains("calculating"))
					throw new AssertionError("Label incorrect (calculating)");
				pass("HelpWindow UI Logic");
			} catch (final Exception | AssertionError exception) {
				error[0] = (exception instanceof AssertionError)
					? (AssertionError) exception
					: new AssertionError(exception.getMessage());
				fail("HelpWindow UI Logic", exception.getMessage());
			} finally {
				latch.countDown();
			}
		});
		try {
			if (!latch.await(2, TimeUnit.SECONDS))
				fail("HelpWindow UI Logic", "Timeout");
			if (error[0] != null)
				throw error[0];
		} catch (final InterruptedException exception) {
			fail("HelpWindow UI Logic", "Interrupted");
		} catch (final AssertionError exception) {
			// Already handled via fail()
		}
	}
	private static void testHelpWindowDraw() {
		final CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try {
				final HelpWindow helpWindow = new HelpWindow();
				final Canvas canvas = new Canvas(300, 100);
				injectField(helpWindow, "moveCanvas", canvas);
				final Move move = new Move(new im.bpu.hexachess.model.AxialCoordinate(0, 0),
					new im.bpu.hexachess.model.AxialCoordinate(0, 1));
				injectField(helpWindow, "bestMove", move);
				invokeMethod(helpWindow, "drawBestMove");
				pass("HelpWindow Draw Logic");
			} catch (final Exception exception) {
				fail("HelpWindow Draw Logic", exception.getMessage());
			} finally {
				latch.countDown();
			}
		});
		try {
			latch.await(1, TimeUnit.SECONDS);
		} catch (final InterruptedException exception) {
			fail("HelpWindow Draw Logic", "Interrupted");
		}
	}
	// ============ Tests Sécurité ============
	private static void testPasswordHashing() {
		try {
			final String password = "mySecretPassword123";
			final String hash = BCrypt.hashpw(password, BCrypt.gensalt());
			assert hash != null : "Hash should not be null";
			assert !hash.equals(password) : "Password should be hashed, not plain text";
			assert BCrypt.checkpw(password, hash) : "Password verification failed";
			assert !BCrypt.checkpw("wrongPassword", hash) : "Wrong password accepted";
			pass("BCrypt password hashing");
		} catch (final Exception exception) {
			fail("BCrypt security", exception.getMessage());
		}
	}
	// ============ Utilitaires ============
	private static void injectField(final Object target, final String fieldName, final Object value)
		throws Exception {
		final Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
	}
	private static void invokeMethod(final Object target, final String methodName)
		throws Exception {
		final Method method = target.getClass().getDeclaredMethod(methodName);
		method.setAccessible(true);
		method.invoke(target);
	}
	private static void pass(final String testName) {
		passedTests++;
		System.out.println("✓ " + testName);
	}
	private static void fail(final String testName, final String reason) {
		failedTests++;
		final String message = "✗ " + testName + ": " + reason;
		System.out.println(message);
		failures.add(message);
	}
	private static void printResults() {
		System.out.println("\n=== Test Results ===");
		System.out.println("Passed: " + passedTests);
		System.out.println("Failed: " + failedTests);
		System.out.println("Total: " + (passedTests + failedTests));
		if (failedTests > 0) {
			System.out.println("\n=== Failures ===");
			for (final String failure : failures) System.out.println(failure);
		} else {
			System.out.println("\nAll tests passed!");
		}
	}
}