package im.bpu.hexachess.ui;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.MainWindow;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LeaderboardMenu {
	@FXML private VBox listContainer;
	@FXML
	public void initialize() {
		List<Player> players = API.getLeaderboard();
		int rank = 1;
		for (Player p : players) {
			HBox row = new HBox(20);
			row.setAlignment(Pos.CENTER_LEFT);
			row.getStyleClass().add("player-item");
			Label rankLabel = new Label("#" + rank);
			rankLabel.getStyleClass().add("leaderboard-rank");
			Label nameLabel = new Label(p.getHandle());
			nameLabel.getStyleClass().add("leaderboard-name");
			Label eloLabel = new Label(p.getRating() + " pts");
			eloLabel.getStyleClass().add("leaderboard-elo");
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