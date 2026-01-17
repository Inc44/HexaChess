package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;

import java.util.List;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.loadWindow;

public class LeaderboardWindow {
	@FXML private VBox listContainer;
	@FXML
	public void initialize() {
		final List<Player> players = API.getLeaderboard();
		int rank = 1;
		for (final Player player : players) {
			final HBox row = new HBox(20);
			row.setAlignment(Pos.CENTER_LEFT);
			row.getStyleClass().add("player-item");
			final Label rankLabel = new Label("#" + rank);
			rankLabel.getStyleClass().add("leaderboard-rank");
			final Label nameLabel = new Label(player.getHandle());
			nameLabel.getStyleClass().add("leaderboard-name");
			final Label eloLabel = new Label(player.getRating() + " pts");
			eloLabel.getStyleClass().add("leaderboard-elo");
			row.getChildren().addAll(rankLabel, nameLabel, eloLabel);
			listContainer.getChildren().add(row);
			rank++;
		}
	}
	@FXML
	private void goBack() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), listContainer);
	}
}