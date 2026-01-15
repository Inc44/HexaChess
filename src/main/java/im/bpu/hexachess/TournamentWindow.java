package im.bpu.hexachess;

import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.loadWindow;

public class TournamentWindow {
	public static Tournament targetTournament;
	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("MMM d yyyy HH:mm");
	@FXML private Label nameLabel;
	@FXML private Label dateLabel;
	@FXML private Label statusLabel;
	@FXML private Label descriptionLabel;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (targetTournament == null) {
			openTournaments();
			return;
		}
		nameLabel.setText(targetTournament.getName());
		descriptionLabel.setText(targetTournament.getDescription());
		if (targetTournament.getStartTime() != null) {
			dateLabel.setText(targetTournament.getStartTime().format(DATE_TIME_FORMATTER));
		} else {
			dateLabel.setText("Date: TBD");
		}
		if (targetTournament.getWinnerId() != null) {
			statusLabel.setText("Winner: " + targetTournament.getWinnerId());
			statusLabel.setStyle("-fx-text-fill: #2E8B57;");
		} else {
			statusLabel.setText("Status: Open for registration");
			statusLabel.setStyle("-fx-text-fill: #b03a2e;");
		}
	}
	@FXML
	private void handleParticipate() {
		if (API.joinTournament(targetTournament.getTournamentId())) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Success");
			alert.setHeaderText(null);
			alert.setContentText("You have successfully joined the tournament!");
			alert.showAndWait();
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("Could not join. Maybe you are already registered?");
			alert.showAndWait();
		}
	}
	@FXML
	private void handleViewParticipants() {
		ParticipantsWindow.targetTournament = this.targetTournament;
		loadWindow("ui/participantsWindow.fxml", new ParticipantsWindow(), backButton);
	}
	@FXML
	private void openTournaments() {
		loadWindow("ui/tournamentsWindow.fxml", new TournamentsWindow(), backButton);
	}
}