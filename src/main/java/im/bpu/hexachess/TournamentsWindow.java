package im.bpu.hexachess;

import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.getAspectRatio;
import static im.bpu.hexachess.Main.loadWindow;

public class TournamentsWindow {
	private static final double ASPECT_RATIO_THRESHOLD = 1.5;
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");
	@FXML private ScrollPane tournamentsPane;
	@FXML private VBox tournamentContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (getAspectRatio() < ASPECT_RATIO_THRESHOLD) {
			tournamentsPane.setStyle(
				"-fx-pref-width: 400px; -fx-max-width: 400px;"); // CSS instead of JavaFX's
																 // setPrefWidth/setMaxWidth due to
																 // parsing precedence
		}
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final List<Tournament> tournaments = API.tournaments();
			Platform.runLater(() -> {
				if (tournaments.isEmpty()) {
					final Label emptyLabel = new Label(bundle.getString("tournaments.empty"));
					tournamentContainer.getChildren().add(emptyLabel);
				} else {
					for (final Tournament tournament : tournaments) {
						try {
							final FXMLLoader tournamentItemLoader =
								new FXMLLoader(getClass().getResource("ui/tournamentItem.fxml"));
							final VBox tournamentItem = tournamentItemLoader.load();
							final LocalDateTime startTime = tournament.getStartTime();
							final String winnerId = tournament.getWinnerId();
							final Label nameLabel = (Label) tournamentItem.lookup("#nameLabel");
							final Label dateLabel = (Label) tournamentItem.lookup("#dateLabel");
							final Label descriptionLabel =
								(Label) tournamentItem.lookup("#descriptionLabel");
							final Label statusLabel = (Label) tournamentItem.lookup("#statusLabel");
							nameLabel.setText(tournament.getName());
							if (startTime != null) {
								dateLabel.setText(startTime.format(DATE_TIME_FORMATTER));
							} else {
								dateLabel.setText(bundle.getString("tournaments.tbd"));
							}
							descriptionLabel.setText(tournament.getDescription());
							if (winnerId != null) {
								statusLabel.setText(
									bundle.getString("tournaments.winner") + ": " + winnerId);
								statusLabel.getStyleClass().add("text-success");
							} else {
								statusLabel.setText(
									bundle.getString("tournaments.status.ongoingopen"));
								statusLabel.getStyleClass().add("text-danger");
							}
							tournamentItem.setOnMouseClicked(
								event -> openTournamentPage(tournament));
							tournamentContainer.getChildren().add(tournamentItem);
						} catch (final Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			});
		});
	}
	private void openTournamentPage(final Tournament tournament) {
		TournamentWindow.targetTournament = tournament;
		loadWindow("ui/tournamentWindow.fxml", new TournamentWindow(), backButton);
	}
	@FXML
	private void openMain() {
		loadWindow("ui/mainWindow.fxml", new MainWindow(), backButton);
	}
}