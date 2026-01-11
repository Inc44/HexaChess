package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;
import im.bpu.hexachess.ui.HexPanel;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
// import javafx.scene.text.Font;
import javafx.util.Duration;

import static im.bpu.hexachess.Main.loadWindow;

public class MainWindow {
	private static final String BASE_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final double SIDEBAR_HIDDEN_X = -160;
	private static final double SIDEBAR_VISIBLE_X = 0;
	private static final int SIDEBAR_DURATION_MS = 160;
	private static final String COMPUTER_HANDLE = "Computer";
	private static final int BASE_ELO = 1200;
	private HexPanel hexPanel;
	@FXML private Button settingsHelpButton;
	@FXML private VBox sidebar;
	@FXML private Canvas canvas;
	@FXML private Button restartButton;
	@FXML private Button rewindButton;
	@FXML private HBox playerItem;
	@FXML private ImageView avatarIcon;
	@FXML private Label handleLabel;
	@FXML private Region countryFlagIcon;
	@FXML private Label ratingLabel;
	@FXML private HBox opponentItem;
	@FXML private ImageView opponentAvatarIcon;
	@FXML private Label opponentHandleLabel;
	@FXML private Region opponentCountryFlagIcon;
	@FXML private Label opponentRatingLabel;
	/*
	@FXML private Label fontFamilyLabel;
	@FXML private Label fontNameLabel;
	*/
	@FXML
	private void initialize() {
		final State state = State.getState();
		hexPanel = new HexPanel(canvas, state);
		sidebar.setTranslateX(SIDEBAR_HIDDEN_X);
		sidebar.setVisible(false);
		playerItem.setManaged(false);
		playerItem.setVisible(false);
		opponentItem.setManaged(false);
		opponentItem.setVisible(false);
		loadPlayerItem();
		loadOpponentItem();
		if (state.isMultiplayer) {
			restartButton.setManaged(false);
			restartButton.setVisible(false);
			rewindButton.setManaged(false);
			rewindButton.setVisible(false);
		}
		/*
		Platform.runLater(() -> {
			settingsHelpButton.applyCss();
			Font font = settingsHelpButton.getFont();
			fontFamilyLabel.setText("Family: " + font.getFamily());
			fontNameLabel.setText("Name: " + font.getName());
		});
		*/
	}
	private void loadPlayerItem() {
		Thread.ofVirtual().start(() -> {
			final String handle = SettingsManager.userHandle;
			final Player player = API.profile(handle);
			if (player == null) {
				Platform.runLater(() -> {
					final String offline = "Offline";
					avatarIcon.setImage(new Image(BASE_URL, true));
					handleLabel.setText(handle);
					ratingLabel.setText("Rating: " + offline);
					countryFlagIcon.setManaged(false);
					countryFlagIcon.setVisible(false);
					playerItem.setManaged(true);
					playerItem.setVisible(true);
				});
				return;
			}
			final int rating = player.getRating();
			final String location = player.getLocation();
			final String avatarUrl = (player.getAvatar() != null && !player.getAvatar().isEmpty())
				? player.getAvatar()
				: BASE_URL;
			Platform.runLater(() -> {
				avatarIcon.setImage(new Image(avatarUrl, true));
				handleLabel.setText(handle);
				ratingLabel.setText("Rating: " + rating);
				if (location != null && !location.isEmpty()) {
					countryFlagIcon.getStyleClass().add("country-" + location);
				} else {
					countryFlagIcon.setManaged(false);
					countryFlagIcon.setVisible(false);
				}
				playerItem.setManaged(true);
				playerItem.setVisible(true);
			});
		});
	}
	private void loadOpponentItem() {
		Thread.ofVirtual().start(() -> {
			final State state = State.getState();
			String handle = COMPUTER_HANDLE;
			int rating = ((SettingsManager.maxDepth - 1) / 2 % 3 + 1) * BASE_ELO;
			String location = null;
			String avatarUrl = BASE_URL;
			if (state.isMultiplayer) {
				if (state.opponentHandle != null) {
					handle = state.opponentHandle;
					final Player opponent = API.profile(handle);
					if (opponent != null) {
						rating = opponent.getRating();
						location = opponent.getLocation();
						avatarUrl =
							(opponent.getAvatar() != null && !opponent.getAvatar().isEmpty())
							? opponent.getAvatar()
							: BASE_URL;
					}
				}
			}
			final String finalHandle = handle;
			final int finalRating = rating;
			final String finalLocation = location;
			final String finalAvatarUrl = avatarUrl;
			Platform.runLater(() -> {
				opponentAvatarIcon.setImage(new Image(finalAvatarUrl, true));
				opponentHandleLabel.setText(finalHandle);
				opponentRatingLabel.setText("Rating: " + finalRating);
				if (finalLocation != null && !finalLocation.isEmpty()) {
					opponentCountryFlagIcon.getStyleClass().add("country-" + finalLocation);
				} else {
					opponentCountryFlagIcon.setManaged(false);
					opponentCountryFlagIcon.setVisible(false);
				}
				opponentItem.setManaged(true);
				opponentItem.setVisible(true);
			});
		});
	}
	@FXML
	private void toggleSidebar() {
		final boolean isClosed = !sidebar.isVisible();
		final Duration duration = Duration.millis(SIDEBAR_DURATION_MS);
		final TranslateTransition transition = new TranslateTransition(duration, sidebar);
		if (isClosed) {
			transition.setToX(SIDEBAR_VISIBLE_X);
			sidebar.setVisible(true);
		} else {
			transition.setToX(SIDEBAR_HIDDEN_X);
			transition.setOnFinished(event -> sidebar.setVisible(false));
		}
		transition.play();
	}
	@FXML
	private void onBoardClicked() {
		if (sidebar.isVisible())
			toggleSidebar();
	}
	@FXML
	private void restart() {
		if (!State.getState().isMultiplayer)
			hexPanel.restart();
	}
	@FXML
	private void rewind() {
		if (!State.getState().isMultiplayer)
			hexPanel.rewind();
	}
	@FXML
	private void openSettings() {
		loadWindow("ui/settingsWindow.fxml", new SettingsWindow(), settingsHelpButton);
	}
	@FXML
	private void openHelpSettings() {
		final ContextMenu menu = new ContextMenu();
		final MenuItem settingsItem = new MenuItem("Settings");
		final MenuItem helpItem = new MenuItem("Help");
		settingsItem.setOnAction(event -> openSettings());
		helpItem.setOnAction(event -> openSettings());
		menu.getItems().addAll(settingsItem, helpItem);
		menu.show(settingsHelpButton, Side.BOTTOM, 0, 0);
	}
	@FXML
	private void openSearch() {
		loadWindow("ui/searchWindow.fxml", new SearchWindow(), settingsHelpButton);
	}
	@FXML
	private void openProfile() {
		ProfileWindow.targetHandle = SettingsManager.userHandle;
		loadWindow("ui/profileWindow.fxml", new ProfileWindow(), settingsHelpButton);
	}
	@FXML
	private void openTournaments() {
		loadWindow("ui/tournamentsWindow.fxml", new TournamentsWindow(), settingsHelpButton);
	}
}