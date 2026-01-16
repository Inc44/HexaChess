package im.bpu.hexachess;

import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
// import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static im.bpu.hexachess.Main.loadWindow;

public class TournamentWindow {
	public static Tournament targetTournament;
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");
	@FXML private Label nameLabel;
	@FXML private Label dateLabel;
	@FXML private Label statusLabel;
	@FXML private Label descriptionLabel;
	@FXML private Button joinButton;
	@FXML private Button participantsButton;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (targetTournament == null) {
			openTournaments();
			return;
		}
		final ResourceBundle bundle = Main.getBundle();
		nameLabel.setText(targetTournament.getName());
		descriptionLabel.setText(targetTournament.getDescription());
		if (targetTournament.getStartTime() != null) {
			dateLabel.setText(targetTournament.getStartTime().format(DATE_TIME_FORMATTER));
		} else {
			dateLabel.setText(bundle.getString("tournaments.tbd"));
		}
		if (targetTournament.getWinnerId() != null) {
			statusLabel.setText(
				bundle.getString("tournaments.winner") + ": " + targetTournament.getWinnerId());
			statusLabel.getStyleClass().add("text-success");
		} else {
			statusLabel.setText(bundle.getString("tournaments.status.openforregistration"));
			statusLabel.getStyleClass().add("text-danger");
		}
		joinButton.setText(bundle.getString("tournament.join"));
		participantsButton.setText(bundle.getString("tournament.participants"));
	}
	@FXML
	private void handleParticipate() {
		Thread.ofVirtual().start(() -> {
			final String tournamentId = targetTournament.getTournamentId();
			final boolean joinSuccess = API.joinTournament(tournamentId);
			final ResourceBundle bundle = Main.getBundle();
			Platform.runLater(() -> {
				statusLabel.getStyleClass().clear();
				if (joinSuccess) {
					statusLabel.setText(bundle.getString("tournament.success"));
					statusLabel.getStyleClass().add("text-success");
				} else {
					statusLabel.setText(bundle.getString("tournament.error"));
					statusLabel.getStyleClass().add("text-danger");
				}
			});
		});
	}
	@FXML
	private void handleViewParticipants() {
		// ParticipantsWindow.targetTournament = this.targetTournament;
		// loadWindow("ui/participantsWindow.fxml", new ParticipantsWindow(), backButton);
	}
	@FXML
	private void openTournaments() {
		loadWindow("ui/tournamentsWindow.fxml", new TournamentsWindow(), backButton);
	}
}