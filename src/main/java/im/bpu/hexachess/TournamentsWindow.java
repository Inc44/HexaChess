package im.bpu.hexachess;

import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;

import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.getAspectRatio;

public class TournamentsWindow {
	@FXML private ScrollPane tournamentsPane;
	@FXML private VBox tournamentContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (getAspectRatio() < 1.5) {
			tournamentsPane.setStyle(
				"-fx-pref-width: 400px; -fx-max-width: 400px;"); // CSS instead of JavaFX's
																 // setPrefWidth/setMaxWidth due to
																 // parsing precedence
		}
		List<Tournament> tournaments = API.tournaments();
		if (tournaments.isEmpty()) {
			Label emptyLabel = new Label("No tournaments found.");
			tournamentContainer.getChildren().add(emptyLabel);
		} else {
			for (Tournament tournament : tournaments) {
				try {
					FXMLLoader tournamentItemLoader =
						new FXMLLoader(getClass().getResource("ui/tournamentItem.fxml"));
					VBox tournamentItem = tournamentItemLoader.load();
					Label nameLabel = (Label) tournamentItem.lookup("#nameLabel");
					Label dateLabel = (Label) tournamentItem.lookup("#dateLabel");
					Label descriptionLabel = (Label) tournamentItem.lookup("#descriptionLabel");
					Label statusLabel = (Label) tournamentItem.lookup("#statusLabel");
					nameLabel.setText(tournament.getName());
					String dateStr = (tournament.getStartTime() != null)
						? tournament.getStartTime().format(
							  DateTimeFormatter.ofPattern("MMM d yyyy HH:mm"))
						: "TBD";
					dateLabel.setText(dateStr);
					descriptionLabel.setText(tournament.getDescription());
					if (tournament.getWinnerId() != null) {
						statusLabel.setText("Winner ID: " + tournament.getWinnerId());
						statusLabel.setStyle("-fx-text-fill: rgb(46, 139, 87);");
					} else {
						statusLabel.setText("Status: Ongoing / Open");
						statusLabel.setStyle("-fx-text-fill: rgb(94, 15, 8);");
					}
					tournamentContainer.getChildren().add(tournamentItem);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}
	@FXML
	private void openMain() {
		try {
			FXMLLoader mainWindowLoader =
				new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
			mainWindowLoader.setController(new MainWindow());
			Parent root = mainWindowLoader.load();
			backButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}