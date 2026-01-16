package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;
import im.bpu.hexachess.ui.HexPanel;
import im.bpu.hexachess.ui.LeaderboardMenu;

import java.io.File;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import static im.bpu.hexachess.Main.loadWindow;

public class MainWindow {
	private static final String AVATAR_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final String FLAGS_URL =
		"https://www.chess.com/bundles/web/images/sprites/flags-128.png";
	private static final double SIDEBAR_HIDDEN_X = -160;
	private static final double SIDEBAR_VISIBLE_X = 0;
	private static final int SIDEBAR_DURATION_MS = 160;
	private static final String COMPUTER_HANDLE = "Computer";
	private static final int BASE_ELO = 1200;
	private static final long DEV_MODE_MS = 2000;
	private static final int DEFAULT_MAX_DEPTH = 3;
	private HexPanel hexPanel;
	private int restartClickCount = 0;
	private long startRestartClickTime = 0;
	private int playerTimeSeconds = 600; 
	private int opponentTimeSeconds = 600;
	private Timeline clockTimeline;
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
	@FXML private ProgressBar opponentProgressBar;
	@FXML private Label fontFamilyLabel;
	@FXML private Label fontNameLabel;
	@FXML private Label playerTimerLabel;
	@FXML private Label opponentTimerLabel;
	@FXML
	private void initialize() {
		final State state = State.getState();
		hexPanel = new HexPanel(canvas, state,
			progressPercentage
			-> Platform.runLater(() -> opponentProgressBar.setProgress(progressPercentage)),
			loadingStatus -> Platform.runLater(() -> {
				final boolean showProgressBar = loadingStatus
					&& SettingsManager.maxDepth > DEFAULT_MAX_DEPTH && !state.isMultiplayer;
				opponentRatingLabel.setVisible(!showProgressBar);
				opponentRatingLabel.setManaged(!showProgressBar);
				opponentProgressBar.setVisible(showProgressBar);
				opponentProgressBar.setManaged(showProgressBar);
				if (!loadingStatus)
					opponentProgressBar.setProgress(0);
			}));
		loadPlayerItem();
		loadOpponentItem();
		if (state.isMultiplayer) {
			restartButton.setManaged(false);
			restartButton.setVisible(false);
			rewindButton.setManaged(false);
			rewindButton.setVisible(false);
		}
		if (state.isDeveloperMode) {
			Platform.runLater(() -> {
				settingsHelpButton.applyCss();
				Font font = settingsHelpButton.getFont();
				fontFamilyLabel.setText("Family: " + font.getFamily());
				fontNameLabel.setText("Name: " + font.getName());
				fontFamilyLabel.getParent().setVisible(true);
				fontFamilyLabel.getParent().setManaged(true);
			});
		}
		setupTimers();
	}
	private void loadPlayerItem() {
		Thread.ofVirtual().start(() -> {
			final String handle = SettingsManager.userHandle;
			final Player player = API.profile(handle);
			if (player == null) {
				final String avatarFileName = AVATAR_URL.substring(AVATAR_URL.lastIndexOf('/') + 1);
				final File avatarFile = CacheManager.save("avatars", avatarFileName, AVATAR_URL);
				final Image avatarImage = new Image(avatarFile.toURI().toString());
				Platform.runLater(() -> {
					avatarIcon.setImage(avatarImage);
					handleLabel.setText(handle);
					ratingLabel.setText("Rating: Offline");
					playerItem.setManaged(true);
					playerItem.setVisible(true);
				});
				return;
			}
			final int rating = player.getRating();
			final String location = player.getLocation();
			final String avatarUrl = (player.getAvatar() != null && !player.getAvatar().isEmpty())
				? player.getAvatar()
				: AVATAR_URL;
			final File avatarFile = CacheManager.save("avatars", handle, avatarUrl);
			final Image avatarImage = new Image(avatarFile.toURI().toString());
			final String flagsFileName = FLAGS_URL.substring(FLAGS_URL.lastIndexOf('/') + 1);
			final File flagsFile = CacheManager.save("images", flagsFileName, FLAGS_URL);
			Platform.runLater(() -> {
				avatarIcon.setImage(avatarImage);
				handleLabel.setText(handle);
				ratingLabel.setText("Rating: " + rating);
				if (location != null && !location.isEmpty()) {
					countryFlagIcon.setStyle(
						"-fx-background-image: url('" + flagsFile.toURI().toString() + "');");
					countryFlagIcon.getStyleClass().add("country-" + location);
					countryFlagIcon.setManaged(true);
					countryFlagIcon.setVisible(true);
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
			String avatarUrl = AVATAR_URL;
			if (state.isMultiplayer && state.opponentHandle != null) {
				handle = state.opponentHandle;
				final Player opponent = API.profile(handle);
				if (opponent != null) {
					rating = opponent.getRating();
					location = opponent.getLocation();
					avatarUrl = (opponent.getAvatar() != null && !opponent.getAvatar().isEmpty())
						? opponent.getAvatar()
						: AVATAR_URL;
				}
			}
			final String finalHandle = handle;
			final int finalRating = rating;
			final String finalLocation = location;
			final File avatarFile = CacheManager.save("avatars", handle, avatarUrl);
			final Image avatarImage = new Image(avatarFile.toURI().toString());
			final String flagsFileName = FLAGS_URL.substring(FLAGS_URL.lastIndexOf('/') + 1);
			final File flagsFile = CacheManager.save("images", flagsFileName, FLAGS_URL);
			Platform.runLater(() -> {
				opponentAvatarIcon.setImage(avatarImage);
				opponentHandleLabel.setText(finalHandle);
				opponentRatingLabel.setText("Rating: " + finalRating);
				if (finalLocation != null && !finalLocation.isEmpty()) {
					opponentCountryFlagIcon.setStyle(
						"-fx-background-image: url('" + flagsFile.toURI().toString() + "');");
					opponentCountryFlagIcon.getStyleClass().add("country-" + finalLocation);
					opponentCountryFlagIcon.setManaged(true);
					opponentCountryFlagIcon.setVisible(true);
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
			sidebar.setManaged(true);
			sidebar.setVisible(true);
		} else {
			transition.setToX(SIDEBAR_HIDDEN_X);
			transition.setOnFinished(event -> {
				sidebar.setManaged(false);
				sidebar.setVisible(false);
			});
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
		playerTimeSeconds = 600;
		opponentTimeSeconds = 600;
		updateTimerLabels(playerTimerLabel, playerTimeSeconds);
		updateTimerLabels(opponentTimerLabel, opponentTimeSeconds);
		if (!State.getState().isMultiplayer) {
			hexPanel.restart();
			final long startTime = System.currentTimeMillis();
			if (restartClickCount == 0 || (startTime - startRestartClickTime > DEV_MODE_MS)) {
				restartClickCount = 1;
				startRestartClickTime = startTime;
			} else
				restartClickCount++;
			if (restartClickCount >= 7) {
				restartClickCount = 0;
				State.getState().isDeveloperMode = !State.getState().isDeveloperMode;
				Platform.runLater(() -> {
					settingsHelpButton.applyCss();
					Font font = settingsHelpButton.getFont();
					fontFamilyLabel.setText("Family: " + font.getFamily());
					fontNameLabel.setText("Name: " + font.getName());
					fontFamilyLabel.getParent().setVisible(State.getState().isDeveloperMode);
					fontFamilyLabel.getParent().setManaged(State.getState().isDeveloperMode);
				});
				hexPanel.repaint();
			}
		}
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
	@FXML
    private void openAchievements() {
        loadWindow("ui/achievementsWindow.fxml", new AchievementsWindow(), settingsHelpButton);
    }
	@FXML
    private void openLeaderboard() {
        loadWindow("ui/leaderboardMenu.fxml", new LeaderboardMenu(), settingsHelpButton);
    }
	private void setupTimers() {
		updateTimerLabels(playerTimerLabel, playerTimeSeconds);
		updateTimerLabels(opponentTimerLabel, opponentTimeSeconds);
		clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
			State state = State.getState();

			boolean isWhiteTurn = state.board.isWhiteTurn;
			boolean isPlayerWhite = state.isWhitePlayer;

			if (isWhiteTurn == isPlayerWhite) {
				if (playerTimeSeconds > 0) {
					playerTimeSeconds--;
					updateTimerLabels(playerTimerLabel, playerTimeSeconds);
				}
			} else {
				if (opponentTimeSeconds > 0) {
					opponentTimeSeconds--;
					updateTimerLabels(opponentTimerLabel, opponentTimeSeconds);
				}
			}
			if (playerTimeSeconds == 0 || opponentTimeSeconds == 0) {
				clockTimeline.stop();
				System.out.println("Time's up!");
			}
		}));
		clockTimeline.setCycleCount(Animation.INDEFINITE);
		clockTimeline.play();
	}

	private void updateTimerLabels(Label label, int totalSeconds) {
		if (label == null) return;
		int minutes = totalSeconds / 60;
		int seconds = totalSeconds % 60;
		String timeString = String.format("%02d:%02d", minutes, seconds);
		label.setText(timeString);
	}
}