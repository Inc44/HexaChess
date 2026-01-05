package im.bpu.hexachess;

import java.time.format.DateTimeFormatter;
import java.util.List;

import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.network.API;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TournamentsWindow {
	@FXML private VBox tournamentContainer;
	@FXML private Button backButton;

	@FXML
	private void initialize() {
		List<Tournament> tournaments = API.getTournaments();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

		if (tournaments.isEmpty()) {
			Label emptyLabel = new Label("No tournaments found.");
			tournamentContainer.getChildren().add(emptyLabel);
		} else {
			for (Tournament t : tournaments) {
				VBox card = new VBox(8);
				card.getStyleClass().add("player-item");
				
				HBox header = new HBox(10);
				Label nameLabel = new Label(t.getName());
				nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: black;");
				
				Region spacer = new Region();
				HBox.setHgrow(spacer, Priority.ALWAYS);
				
				String dateStr = (t.getStartTime() != null) ? t.getStartTime().format(formatter) : "TBD";
				Label dateLabel = new Label(dateStr);
				dateLabel.setStyle("-fx-font-size: 12px;");
				
				header.getChildren().addAll(nameLabel, spacer, dateLabel);

				Label descLabel = new Label(t.getDescription());
				descLabel.setWrapText(true);
				descLabel.setStyle("-fx-font-weight: normal; -fx-font-size: 14px;");

				Label statusLabel;
				if (t.getWinnerId() != null) {
					statusLabel = new Label("Winner ID: " + t.getWinnerId());
					statusLabel.setStyle("-fx-text-fill: #2E8B57;");
				} else {
					statusLabel = new Label("Status: Ongoing / Open");
					statusLabel.setStyle("-fx-text-fill: #A52A2A;");
				}

				card.getChildren().addAll(header, descLabel, statusLabel);
				tournamentContainer.getChildren().add(card);
			}
		}
	}

	@FXML
	private void openMain() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ui/mainWindow.fxml"));
			loader.setController(new MainWindow());
			Parent root = loader.load();
			backButton.getScene().setRoot(root);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
