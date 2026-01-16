package im.bpu.hexachess.test;

import im.bpu.hexachess.HelpWindow;
import im.bpu.hexachess.State;
import im.bpu.hexachess.dao.*;
import im.bpu.hexachess.entity.*;
import im.bpu.hexachess.model.*;
import im.bpu.hexachess.network.API;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class HexaChessTest {
    
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final List<String> failures = new ArrayList<>();
    
    public static void main(String[] args) {
        System.out.println("=== HexaChess Test Suite ===\n");
        
        // Initialiser JavaFX pour les tests UI
        try {
            new JFXPanel();
            Platform.runLater(() -> {});
        } catch (Exception e) {
            System.err.println("Note: JavaFX initialization failed or already initialized (continuing...)");
        }
        
        // Tests de la logique du jeu
        testBoardInitialization();
        testPieceMovement();
        testPawnMovement();
        testEnPassant();
        testPromotion();
        testCheckValidMoves();
        testMoveGeneration();
        
        // Tests des coordonnées
        testAxialCoordinates();
        
        // Tests de l'IA
        testAIInitialization();
        testAIEvaluation();
        
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
            Board board = new Board();
            
            // Vérifier que toutes les pièces sont placées
            int whitePieces = 0;
            int blackPieces = 0;
            
            for (Piece piece : board.pieces.values()) {
                if (piece.isWhite) whitePieces++;
                else blackPieces++;
            }
            
            assert whitePieces == 16 : "Expected 16 white pieces, got " + whitePieces;
            assert blackPieces == 16 : "Expected 16 black pieces, got " + blackPieces;
            
            // Vérifier que c'est au tour des blancs
            assert board.isWhiteTurn : "White should start";
            
            pass("Board initialization");
        } catch (AssertionError | Exception e) {
            fail("Board initialization", e.getMessage());
        }
    }
    
    private static void testPieceMovement() {
        try {
            Board board = new Board();
            
            // Déplacer un pion blanc
            AxialCoordinate from = new AxialCoordinate(1, 1);
            AxialCoordinate to = new AxialCoordinate(0, 0);
            
            Piece piece = board.getPiece(from);
            assert piece != null : "Piece should exist at starting position";
            assert piece.type == PieceType.PAWN : "Piece should be a pawn";
            
            board.movePiece(from, to);
            
            assert board.getPiece(from) == null : "Starting position should be empty";
            assert board.getPiece(to) != null : "Target position should have piece";
            assert !board.isWhiteTurn : "Turn should switch to black";
            
            pass("Piece movement");
        } catch (AssertionError | Exception e) {
            fail("Piece movement", e.getMessage());
        }
    }
    
    private static void testPawnMovement() {
        try {
            Board board = new Board();
            
            // Test mouvement simple du pion
            AxialCoordinate pawnPos = new AxialCoordinate(1, 1);
            List<Move> moves = board.listMoves(true);
            
            boolean canMoveForward = false;
            for (Move move : moves) {
                if (move.from.equals(pawnPos)) {
                    canMoveForward = true;
                    break;
                }
            }
            
            assert canMoveForward : "Pawn should be able to move";
            
            pass("Pawn movement");
        } catch (AssertionError | Exception e) {
            fail("Pawn movement", e.getMessage());
        }
    }
    
    private static void testEnPassant() {
        try {
            Board board = new Board();
            assert board.enPassant == null : "enPassant should be null initially";
            pass("En passant initialization");
        } catch (AssertionError | Exception e) {
            fail("En passant", e.getMessage());
        }
    }
    
    private static void testPromotion() {
        try {
            Board board = new Board();
            
            // Placer un pion blanc en position de promotion
            AxialCoordinate promotionPos = new AxialCoordinate(-5, 4);
            Piece pawn = new Piece(PieceType.PAWN, true);
            board.pieces.put(promotionPos, pawn);
            
            // Déplacer vers case de promotion
            AxialCoordinate targetPos = new AxialCoordinate(-5, 3);
            board.movePiece(promotionPos, targetPos);
            
            Piece promoted = board.getPiece(targetPos);
            assert promoted.type == PieceType.QUEEN : "Pawn should be promoted to queen";
            
            pass("Pawn promotion");
        } catch (AssertionError | Exception e) {
            fail("Pawn promotion", e.getMessage());
        }
    }
    
    private static void testCheckValidMoves() {
        try {
            Board board = new Board();
            List<Move> whiteMoves = board.listMoves(true);
            List<Move> blackMoves = board.listMoves(false);
            
            assert !whiteMoves.isEmpty() : "White should have legal moves";
            assert !blackMoves.isEmpty() : "Black should have legal moves";
            
            pass("Valid moves generation");
        } catch (AssertionError | Exception e) {
            fail("Valid moves generation", e.getMessage());
        }
    }
    
    private static void testMoveGeneration() {
        try {
            Board board = new Board();
            List<Move> initialMoves = board.listMoves(true);
            assert initialMoves.size() > 0 : "Should have moves at start";
            pass("Move generation count");
        } catch (AssertionError | Exception e) {
            fail("Move generation", e.getMessage());
        }
    }
    
    // ============ Tests Coordonnées ============
    
    private static void testAxialCoordinates() {
        try {
            AxialCoordinate coord = new AxialCoordinate(0, 0);
            assert coord.q == 0 : "Q should be 0";
            assert coord.r == 0 : "R should be 0";
            
            AxialCoordinate sum = coord.add(1, 1);
            assert sum.q == 1 && sum.r == 1 : "Addition failed";
            
            AxialCoordinate invalid = new AxialCoordinate(10, 10);
            assert !invalid.isValid() : "Should be invalid";
            
            AxialCoordinate valid = new AxialCoordinate(0, 0);
            assert valid.isValid() : "Should be valid";
            
            pass("Axial coordinates");
        } catch (AssertionError | Exception e) {
            fail("Axial coordinates", e.getMessage());
        }
    }
    
    // ============ Tests IA ============
    
    private static void testAIInitialization() {
        try {
            AI ai = new AI();
            ai.setMaxDepth(3);
            pass("AI initialization");
        } catch (Exception e) {
            fail("AI initialization", e.getMessage());
        }
    }
    
    private static void testAIEvaluation() {
        try {
            AI ai = new AI();
            ai.setMaxDepth(1);
            Board board = new Board();
            Move bestMove = ai.getBestMove(board, progress -> {});
            assert bestMove != null : "AI should find a move";
            pass("AI evaluation");
        } catch (AssertionError | Exception e) {
            fail("AI evaluation", e.getMessage());
        }
    }
    
    // ============ Tests Entités ============
    
    private static void testPlayerEntity() {
        try {
            Player player = new Player("testId123", "testHandle", "test@example.com", "hash", 1200, false, LocalDateTime.now());
            assert player.getPlayerId().equals("testId123") : "Player ID mismatch";
            pass("Player entity");
        } catch (AssertionError | Exception e) {
            fail("Player entity", e.getMessage());
        }
    }
    
    private static void testGameEntity() {
        try {
            Game game = new Game("gameId123", "whiteId", "blackId", null, null, "", LocalDateTime.now(), null, null);
            assert game.getGameId().equals("gameId123") : "Game ID mismatch";
            pass("Game entity");
        } catch (AssertionError | Exception e) {
            fail("Game entity", e.getMessage());
        }
    }
    
    private static void testTournamentEntity() {
        try {
            Tournament tournament = new Tournament("tournId123", "Test", "Desc", LocalDateTime.now(), null, null);
            assert tournament.getTournamentId().equals("tournId123") : "Tournament ID mismatch";
            pass("Tournament entity");
        } catch (AssertionError | Exception e) {
            fail("Tournament entity", e.getMessage());
        }
    }
    
    private static void testPuzzleEntity() {
        try {
            Puzzle puzzle = new Puzzle("pid123", "m", "s", 1500, "t", LocalDateTime.now());
            assert puzzle.getPuzzleId().equals("pid123") : "Puzzle ID mismatch";
            pass("Puzzle entity");
        } catch (AssertionError | Exception e) {
            fail("Puzzle entity", e.getMessage());
        }
    }
    
    private static void testSettingsEntity() {
        try {
            Settings settings = new Settings("pid123", "dark", true, false, 3);
            assert settings.getPlayerId().equals("pid123") : "Player ID mismatch";
            pass("Settings entity");
        } catch (AssertionError | Exception e) {
            fail("Settings entity", e.getMessage());
        }
    }
    
    // ============ Tests DAO ============
    
    private static void testPlayerDAO() {
        try {
            PlayerDAO dao = new PlayerDAO();
            Player p = new Player("testDAO123", "h", "e@e.com", "pw", 1200, false, LocalDateTime.now());
            dao.create(p);
            assert dao.read("testDAO123") != null : "Player read failed";
            dao.delete(p);
            pass("PlayerDAO CRUD operations");
        } catch (Exception e) {
            fail("PlayerDAO", e.getMessage() + " (DB unavailable?)");
        }
    }
    
    private static void testGameDAO() {
        try {
            GameDAO dao = new GameDAO();
            Game g = new Game("testGameDAO", "1", "2", null, null, "", LocalDateTime.now(), null, null);
            dao.create(g);
            assert dao.read("testGameDAO") != null : "Game read failed";
            dao.delete(g);
            pass("GameDAO CRUD operations");
        } catch (Exception e) {
            fail("GameDAO", e.getMessage() + " (DB unavailable?)");
        }
    }
    
    private static void testTournamentDAO() {
        try {
            TournamentDAO dao = new TournamentDAO();
            Tournament t = new Tournament("testTournDAO", "N", "D", LocalDateTime.now(), null, null);
            dao.create(t);
            assert dao.read("testTournDAO") != null : "Tournament read failed";
            dao.delete(t);
            pass("TournamentDAO CRUD operations");
        } catch (Exception e) {
            fail("TournamentDAO", e.getMessage() + " (DB unavailable?)");
        }
    }
    
    private static void testSettingsDAO() {
        try {
            SettingsDAO dao = new SettingsDAO();
            Settings s = new Settings("00000000001", "d", true, false, 3);
            dao.create(s);
            assert dao.read("00000000001") != null : "Settings read failed";
            dao.delete(s);
            pass("SettingsDAO CRUD operations");
        } catch (Exception e) {
            fail("SettingsDAO", e.getMessage() + " (DB unavailable?)");
        }
    }
    
    // ============ Tests State ============
    
    private static void testStateSingleton() {
        try {
            State state1 = State.getState();
            State state2 = State.getState();
            assert state1 == state2 : "State should be singleton";
            pass("State singleton");
        } catch (AssertionError | Exception e) {
            fail("State singleton", e.getMessage());
        }
    }
    
    private static void testStateClear() {
        try {
            State state = State.getState();
            state.isMultiplayer = true;
            state.gameId = "testGame";
            state.clear();
            assert state.board != null : "Board should exist after clear";
            assert !state.isMultiplayer : "Should not be multiplayer";
            pass("State clear");
        } catch (AssertionError | Exception e) {
            fail("State clear", e.getMessage());
        }
    }

    // ============ Tests HelpWindow ============

    private static void testHelpWindowUILogic() {
        final CountDownLatch latch = new CountDownLatch(1);
        final AssertionError[] error = {null};

        Platform.runLater(() -> {
            try {
                HelpWindow helpWindow = new HelpWindow();
                Button showBestMoveButton = new Button();
                Label bestMoveLabel = new Label();

                injectField(helpWindow, "showBestMoveButton", showBestMoveButton);
                injectField(helpWindow, "bestMoveLabel", bestMoveLabel);

                // Test sans partie
                State.getState().board = null;
                invokeMethod(helpWindow, "initialize");

                if (!showBestMoveButton.isDisabled()) throw new AssertionError("Button should be disabled (no game)");
                if (!bestMoveLabel.getText().toLowerCase().contains("start")) throw new AssertionError("Label incorrect (no game)");

                // Test avec partie
                State.getState().board = new Board();
                showBestMoveButton.setDisable(false); // Reset manuel post-initialize

                invokeMethod(helpWindow, "showBestMove");

                if (!showBestMoveButton.isDisabled()) throw new AssertionError("Button should be disabled (calculating)");
                if (!bestMoveLabel.getText().toLowerCase().contains("calculating")) throw new AssertionError("Label incorrect (calculating)");

                pass("HelpWindow UI Logic");
            } catch (Exception | AssertionError e) {
                error[0] = (e instanceof AssertionError) ? (AssertionError)e : new AssertionError(e.getMessage());
                fail("HelpWindow UI Logic", e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(2, TimeUnit.SECONDS)) fail("HelpWindow UI Logic", "Timeout");
            if (error[0] != null) throw error[0];
        } catch (InterruptedException e) {
            fail("HelpWindow UI Logic", "Interrupted");
        } catch (AssertionError e) {
            // Already handled via fail()
        }
    }

    private static void testHelpWindowDraw() {
        final CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                HelpWindow helpWindow = new HelpWindow();
                Canvas canvas = new Canvas(300, 100);
                injectField(helpWindow, "moveCanvas", canvas);

                Move move = new Move(new im.bpu.hexachess.model.AxialCoordinate(0,0), new im.bpu.hexachess.model.AxialCoordinate(0,1));
                injectField(helpWindow, "bestMove", move);

                invokeMethod(helpWindow, "drawBestMove");
                pass("HelpWindow Draw Logic");
            } catch (Exception e) {
                fail("HelpWindow Draw Logic", e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("HelpWindow Draw Logic", "Interrupted");
        }
    }
    
    // ============ Utilitaires ============
    
    private static void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    private static void invokeMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }
    
    private static void pass(String testName) {
        passedTests++;
        System.out.println("✓ " + testName);
    }
    
    private static void fail(String testName, String reason) {
        failedTests++;
        String message = "✗ " + testName + ": " + reason;
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
            for (String failure : failures) System.out.println(failure);
        } else {
            System.out.println("\nAll tests passed!");
        }
    }
}
