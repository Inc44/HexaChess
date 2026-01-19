package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static im.bpu.hexachess.Main.loadWindow;

public class TournamentWindow {
	public static Tournament targetTournament;
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");
	@FXML private Label nameLabel;
	@FXML private Label descriptionLabel;
	@FXML private Label startTimeLabel;
	@FXML private Label statusLabel;
	@FXML private Button joinButton;
	@FXML private Button participantsButton;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (targetTournament == null) {
			openTournaments();
			return;
		}
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final String name = targetTournament.getName();
			final String description = targetTournament.getDescription();
			final LocalDateTime startTime = targetTournament.getStartTime();
			final String winnerId = targetTournament.getWinnerId();
			Platform.runLater(() -> {
				nameLabel.setText(name);
				descriptionLabel.setText(description);
				if (startTime != null) {
					startTimeLabel.setText(startTime.format(DATE_TIME_FORMATTER));
				} else {
					startTimeLabel.setText(bundle.getString("window.tournament.tbd"));
				}
				if (winnerId != null) {
					statusLabel.setText(
						bundle.getString("window.tournament.winner") + ": " + winnerId);
					statusLabel.getStyleClass().add("text-success");
				} else {
					statusLabel.setText(bundle.getString("window.tournament.statusOpen"));
					statusLabel.getStyleClass().add("text-danger");
				}
				joinButton.setText(bundle.getString("window.tournament.joinButton"));
				participantsButton.setText(
					bundle.getString("window.tournament.participantsButton"));
			});
		});
	}
	@FXML
	private void participate() {
		Thread.ofVirtual().start(() -> {
			final String tournamentId = targetTournament.getTournamentId();
			final boolean joinSuccess = API.join(tournamentId);
			final ResourceBundle bundle = Main.getBundle();
			Platform.runLater(() -> {
				if (joinSuccess) {
					statusLabel.setText(bundle.getString("window.tournament.joinSuccess"));
					statusLabel.getStyleClass().add("text-success");
				} else {
					statusLabel.setText(bundle.getString("window.tournament.joinError"));
					statusLabel.getStyleClass().add("text-danger");
				}
			});
		});
	}
	@FXML
	private void openTournaments() {
		loadWindow("/fxml/window/tournamentsWindow.fxml", new TournamentsWindow(), backButton);
	}
	@FXML
	private void openParticipants() {
		ParticipantsWindow.targetTournament = targetTournament;
		loadWindow("/fxml/window/participantsWindow.fxml", new ParticipantsWindow(), backButton);
	}
}