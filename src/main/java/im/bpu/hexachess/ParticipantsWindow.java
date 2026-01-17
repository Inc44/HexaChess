package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.loadWindow;

public class ParticipantsWindow {
	public static Tournament targetTournament;
	@FXML private Label titleLabel;
	@FXML private VBox listContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (targetTournament == null) {
			handleBack();
			return;
		}
		final ResourceBundle bundle = Main.getBundle();
		titleLabel.setText(
			bundle.getString("tournament.participants.title") + ": " + targetTournament.getName());
		Thread.ofVirtual().start(() -> {
			final List<Player> players = API.participants(targetTournament.getTournamentId());
			Platform.runLater(() -> {
				listContainer.getChildren().clear();
				if (players.isEmpty()) {
					listContainer.getChildren().add(
						new Label(bundle.getString("tournament.participants.empty")));
				} else {
					for (final Player player : players) {
						final HBox row = new HBox(20);
						row.getStyleClass().add("participant-row");
						row.setAlignment(Pos.CENTER_LEFT);
						final Label name = new Label(player.getHandle());
						name.getStyleClass().add("participant-name");
						final Label rating = new Label(player.getRating() + " ELO");
						row.getChildren().addAll(name, rating);
						listContainer.getChildren().add(row);
					}
				}
			});
		});
	}
	@FXML
	private void handleBack() {
		TournamentWindow.targetTournament = targetTournament;
		loadWindow("ui/tournamentWindow.fxml", new TournamentWindow(), backButton);
	}
}