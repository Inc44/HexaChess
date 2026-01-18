package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.Main;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.entity.Tournament;
import im.bpu.hexachess.manager.CacheManager;
import im.bpu.hexachess.network.API;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import static im.bpu.hexachess.Main.loadWindow;

public class ParticipantsWindow {
	private static final String AVATAR_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final String FLAGS_URL =
		"https://www.chess.com/bundles/web/images/sprites/flags-128.png";
	public static Tournament targetTournament;
	@FXML private Label tournamentLabel;
	@FXML private VBox participantsContainer;
	@FXML private Button backButton;
	@FXML
	private void initialize() {
		if (targetTournament == null) {
			openTournament();
			return;
		}
		final ResourceBundle bundle = Main.getBundle();
		final String name = targetTournament.getName();
		tournamentLabel.setText(bundle.getString("tournament.participants.title") + ": " + name);
		loadParticipants();
	}
	private void loadParticipants() {
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final String tournamentId = targetTournament.getTournamentId();
			final List<Player> players = API.participants(tournamentId);
			Platform.runLater(() -> {
				if (players.isEmpty()) {
					final Label emptyLabel =
						new Label(bundle.getString("tournament.participants.empty"));
					participantsContainer.getChildren().add(emptyLabel);
				} else {
					for (final Player player : players) {
						try {
							final FXMLLoader playerItemLoader = new FXMLLoader(
								getClass().getResource("/fxml/playerItem.fxml"), bundle);
							final HBox playerItem = playerItemLoader.load();
							final String handle = player.getHandle();
							final int rating = player.getRating();
							final String location = player.getLocation();
							final String avatarUrl =
								(player.getAvatar() != null && !player.getAvatar().isEmpty())
								? player.getAvatar()
								: AVATAR_URL;
							final ImageView avatarIcon =
								(ImageView) playerItem.lookup("#avatarIcon");
							final Label handleLabel = (Label) playerItem.lookup("#handleLabel");
							final Region countryFlagIcon =
								(Region) playerItem.lookup("#countryFlagIcon");
							final Label ratingLabel = (Label) playerItem.lookup("#ratingLabel");
							final Button challengeButton =
								(Button) playerItem.lookup("#challengeButton");
							challengeButton.setManaged(false);
							challengeButton.setVisible(false);
							final File avatarFile = CacheManager.save("avatars", handle, avatarUrl);
							final Image avatarImage = new Image(avatarFile.toURI().toString());
							final String flagsFileName =
								FLAGS_URL.substring(FLAGS_URL.lastIndexOf('/') + 1);
							final File flagsFile =
								CacheManager.save("images", flagsFileName, FLAGS_URL);
							avatarIcon.setImage(avatarImage);
							handleLabel.setText(handle);
							ratingLabel.setText(bundle.getString("common.rating") + ": " + rating);
							if (location != null && !location.isEmpty()) {
								countryFlagIcon.setStyle("-fx-background-image: url('"
									+ flagsFile.toURI().toString() + "');");
								countryFlagIcon.getStyleClass().add("country-" + location);
								countryFlagIcon.setManaged(true);
								countryFlagIcon.setVisible(true);
							}
							playerItem.setOnMouseClicked(event -> {
								ProfileWindow.targetHandle = handle;
								loadWindow("/fxml/profileWindow.fxml", new ProfileWindow(),
									participantsContainer);
							});
							participantsContainer.getChildren().add(playerItem);
						} catch (final Exception exception) {
							exception.printStackTrace();
						}
					}
				}
			});
		});
	}
	@FXML
	private void openTournament() {
		TournamentWindow.targetTournament = targetTournament;
		loadWindow("/fxml/tournamentWindow.fxml", new TournamentWindow(), backButton);
	}
}