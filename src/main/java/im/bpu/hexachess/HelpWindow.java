package im.bpu.hexachess;

import im.bpu.hexachess.model.AI;
import im.bpu.hexachess.model.Board;
import im.bpu.hexachess.model.Move;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import static im.bpu.hexachess.Main.loadWindow;

public class HelpWindow {

    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private VBox contentBox;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button showBestMoveButton;
    
    @FXML
    private Canvas moveCanvas;
    
    @FXML
    private Label bestMoveLabel;

    private static final double HEX_SIZE = 30;
    private Move bestMove = null;

    @FXML
    private void initialize() {
        // Désactiver le bouton si pas en jeu
        if (State.getState().board == null) {
            showBestMoveButton.setDisable(true);
            bestMoveLabel.setText("Start a game to see the best move");
        }
    }

    @FXML
    private void showBestMove() {
        showBestMoveButton.setDisable(true);
        bestMoveLabel.setText("Calculating best move...");
        
        Thread.ofVirtual().start(() -> {
            final State state = State.getState();
            final Board board = state.board;
            
            if (board == null) {
                Platform.runLater(() -> {
                    bestMoveLabel.setText("No active game");
                    showBestMoveButton.setDisable(false);
                });
                return;
            }

            final AI ai = new AI();
            ai.setMaxDepth(SettingsManager.maxDepth);
            
            bestMove = ai.getBestMove(board, progress -> {});
            
            Platform.runLater(() -> {
                if (bestMove != null) {
                    bestMoveLabel.setText("Best move: " + 
                        bestMove.from.q + "," + bestMove.from.r + 
                        " → " + 
                        bestMove.to.q + "," + bestMove.to.r);
                    drawBestMove();
                } else {
                    bestMoveLabel.setText("No moves available");
                }
                showBestMoveButton.setDisable(false);
            });
        });
    }

    private void drawBestMove() {
        if (bestMove == null || moveCanvas == null) return;
        
        final GraphicsContext gc = moveCanvas.getGraphicsContext2D();
        final double centerX = moveCanvas.getWidth() / 2;
        final double centerY = moveCanvas.getHeight() / 2;
        
        // Clear canvas
        gc.clearRect(0, 0, moveCanvas.getWidth(), moveCanvas.getHeight());
        
        // Draw "from" hex
        drawHex(gc, centerX - 60, centerY, HEX_SIZE, Color.LIGHTGREEN, "FROM");
        
        // Draw arrow
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(centerX - 30, centerY, centerX + 30, centerY);
        gc.strokeLine(centerX + 20, centerY - 5, centerX + 30, centerY);
        gc.strokeLine(centerX + 20, centerY + 5, centerX + 30, centerY);
        
        // Draw "to" hex
        drawHex(gc, centerX + 60, centerY, HEX_SIZE, Color.LIGHTBLUE, "TO");
    }

    private void drawHex(GraphicsContext gc, double x, double y, double size, Color color, String label) {
        final double[] xPoints = new double[6];
        final double[] yPoints = new double[6];
        
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i;
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
