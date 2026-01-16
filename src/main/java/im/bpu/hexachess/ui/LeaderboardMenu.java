package im.bpu.hexachess.ui;

import im.bpu.hexachess.MainWindow;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class LeaderboardMenu {
    @FXML
    private VBox listContainer;
    @FXML
    public void initialize() {
        List<Player> players = API.getLeaderboard();
        int rank = 1;
        for (Player p : players) {
            HBox row = new HBox(20);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("player-item");
            Label rankLabel = new Label("#" + rank);
            rankLabel.setTextFill(Color.ORANGE);
            rankLabel.setMinWidth(50);
            rankLabel.setFont(new Font("Monospaced Bold", 18));
            Label nameLabel = new Label(p.getHandle());
            nameLabel.setTextFill(Color.WHITE);
            nameLabel.setFont(new Font("Arial Bold", 16));
            nameLabel.setMinWidth(200);
            Label eloLabel = new Label(p.getRating() + " pts");
            eloLabel.setTextFill(Color.LIGHTGREEN);
            eloLabel.setFont(new Font("Arial", 16));

            row.getChildren().addAll(rankLabel, nameLabel, eloLabel);
            
            listContainer.getChildren().add(row);
            
            rank++;
        }
    }

    @FXML
    private void goBack() {
        Main.loadWindow("ui/mainWindow.fxml", new MainWindow(), listContainer);
    }
}