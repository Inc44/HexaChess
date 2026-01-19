package im.bpu.hexachess.ui.controller;

import im.bpu.hexachess.GameTimer;
import im.bpu.hexachess.Main;
import im.bpu.hexachess.State;
import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.manager.CacheManager;
import im.bpu.hexachess.manager.SettingsManager;
import im.bpu.hexachess.network.API;
import im.bpu.hexachess.ui.HexPanel;

import java.io.File;
import java.util.ResourceBundle;
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
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.util.Duration;

import static im.bpu.hexachess.Main.loadWindow;

public class MainWindow {
	private static final String AVATAR_URL =
		"https://www.chess.com/bundles/web/images/noavatar_l.gif";
	private static final String FLAGS_URL =
		"https://www.chess.com/bundles/web/images/sprites/flags-128.png";
	private static final double SIDEBAR_HIDDEN_X = -160;
	private static final double SIDEBAR_VISIBLE_X = 0;
	private static final int SIDEBAR_DURATION_MS = 160;
	private static final int BASE_ELO = 1200;
	private static final long DEV_MODE_MS = 2000;
	private static final int DEV_MODE_CLICKS = 7;
	private static final int DEFAULT_MAX_DEPTH = 3;
	private HexPanel hexPanel;
	private int restartClickCount = 0;
	private long startRestartClickTime = 0;
	private GameTimer gameTimer;
	@FXML private Button settingsHelpButton;
	@FXML private VBox sidebar;
	@FXML private Canvas canvas;
	@FXML private VBox gameOverContainer;
	@FXML private Label gameOverLabel;
	@FXML private Button restartButton;
	@FXML private Button rewindButton;
	@FXML private Label playerTimerLabel;
	@FXML private VBox playerItem;
	@FXML private ImageView avatarIcon;
	@FXML private Label handleLabel;
	@FXML private Region countryFlagIcon;
	@FXML private Label ratingLabel;
	@FXML private Label opponentTimerLabel;
	@FXML private VBox opponentItem;
	@FXML private ImageView opponentAvatarIcon;
	@FXML private Label opponentHandleLabel;
	@FXML private Region opponentCountryFlagIcon;
	@FXML private Label opponentRatingLabel;
	@FXML private ProgressBar opponentProgressBar;
	@FXML private VBox devModeContainer;
	@FXML private Label fontFamilyLabel;
	@FXML private Label fontNameLabel;
	@FXML private Label screenWidthLabel;
	@FXML private Label screenHeightLabel;
	@FXML private Label aspectRatioLabel;
	@FXML
	private void initialize() {
		final State state = State.getState();
		hexPanel = new HexPanel(canvas, state,
			progressPercentage
			-> Platform.runLater(() -> opponentProgressBar.setProgress(progressPercentage)),
			loadingStatus
			-> Platform.runLater(() -> {
				final boolean showProgressBar = loadingStatus
					&& SettingsManager.maxDepth > DEFAULT_MAX_DEPTH && !state.isMultiplayer;
				opponentRatingLabel.setManaged(!showProgressBar);
				opponentRatingLabel.setVisible(!showProgressBar);
				opponentProgressBar.setManaged(showProgressBar);
				opponentProgressBar.setVisible(showProgressBar);
				if (!loadingStatus)
					opponentProgressBar.setProgress(0);
			}),
			gameOverMessage -> Platform.runLater(() -> {
				canvas.setManaged(false);
				canvas.setVisible(false);
				gameOverLabel.setText(gameOverMessage);
				gameOverContainer.setManaged(true);
				gameOverContainer.setVisible(true);
				gameTimer.stop();
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
			Platform.runLater(this::showDevModeLabels);
		}
		gameTimer = new GameTimer(playerTimerLabel, opponentTimerLabel);
		gameTimer.start();
	}
	private void showDevModeLabels() {
		final ResourceBundle bundle = Main.getBundle();
		final Font font = settingsHelpButton.getFont();
		final String fontFamily = font.getFamily();
		final String fontName = font.getName();
		final double width = Screen.getPrimary().getBounds().getWidth();
		final double height = Screen.getPrimary().getBounds().getHeight();
		final double aspectRatio = width / height;
		fontFamilyLabel.setText(
			bundle.getString("window.main.fontFamilyLabel") + ": " + fontFamily);
		fontNameLabel.setText(bundle.getString("window.main.fontNameLabel") + ": " + fontName);
		screenWidthLabel.setText(
			bundle.getString("window.main.screenWidthLabel") + ": " + (int) width);
		screenHeightLabel.setText(
			bundle.getString("window.main.screenHeightLabel") + ": " + (int) height);
		aspectRatioLabel.setText(
			bundle.getString("window.main.aspectRatioLabel") + ": " + aspectRatio);
		devModeContainer.setManaged(true);
		devModeContainer.setVisible(true);
	}
	private void loadPlayerItem() {
		Thread.ofVirtual().start(() -> {
			final ResourceBundle bundle = Main.getBundle();
			final String handle = SettingsManager.userHandle;
			final Player player = API.profile(handle);
			if (player == null) {
				final String avatarFileName = AVATAR_URL.substring(AVATAR_URL.lastIndexOf('/') + 1);
				final File avatarFile = CacheManager.save("avatars", avatarFileName, AVATAR_URL);
				final Image avatarImage = new Image(avatarFile.toURI().toString());
				Platform.runLater(() -> {
					avatarIcon.setImage(avatarImage);
					handleLabel.setText(handle);
					ratingLabel.setText(bundle.getString("common.rating") + ": "
						+ bundle.getString("common.offline"));
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
				ratingLabel.setText(bundle.getString("common.rating") + ": " + rating);
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
			final ResourceBundle bundle = Main.getBundle();
			final State state = State.getState();
			String handle = bundle.getString("common.computer");
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
				opponentRatingLabel.setText(bundle.getString("common.rating") + ": " + finalRating);
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
		gameTimer.reset();
		gameTimer.start();
		if (!State.getState().isMultiplayer) {
			gameOverContainer.setManaged(false);
			gameOverContainer.setVisible(false);
			canvas.setManaged(true);
			canvas.setVisible(true);
			hexPanel.restart();
			final long startTime = System.currentTimeMillis();
			if (restartClickCount == 0 || (startTime - startRestartClickTime > DEV_MODE_MS)) {
				restartClickCount = 1;
				startRestartClickTime = startTime;
			} else
				restartClickCount++;
			if (restartClickCount >= DEV_MODE_CLICKS) {
				restartClickCount = 0;
				State.getState().isDeveloperMode = !State.getState().isDeveloperMode;
				Platform.runLater(() -> {
					if (State.getState().isDeveloperMode) {
						showDevModeLabels();
					} else {
						devModeContainer.setManaged(false);
						devModeContainer.setVisible(false);
					}
				});
				hexPanel.repaint();
			}
		}
	}
	@FXML
	private void rewind() {
		if (!State.getState().isMultiplayer) {
			gameOverContainer.setManaged(false);
			gameOverContainer.setVisible(false);
			canvas.setManaged(true);
			canvas.setVisible(true);
			hexPanel.rewind();
		}
	}
	@FXML
	private void openHelpSettings() {
		final ResourceBundle bundle = Main.getBundle();
		final ContextMenu menu = new ContextMenu();
		final MenuItem settingsItem = new MenuItem(bundle.getString("window.main.settingsMenu"));
		final MenuItem helpItem = new MenuItem(bundle.getString("window.main.helpMenu"));
		settingsItem.setOnAction(event -> openSettings());
		helpItem.setOnAction(event -> openHelp());
		menu.getItems().addAll(settingsItem, helpItem);
		menu.show(settingsHelpButton, Side.BOTTOM, 0, 0);
	}
	private void openSettings() {
		gameTimer.stop();
		loadWindow("/fxml/window/settingsWindow.fxml", new SettingsWindow(), settingsHelpButton);
	}
	private void openHelp() {
		gameTimer.stop();
		loadWindow("/fxml/window/helpWindow.fxml", new HelpWindow(), settingsHelpButton);
	}
	@FXML
	private void openSearch() {
		gameTimer.stop();
		loadWindow("/fxml/window/searchWindow.fxml", new SearchWindow(), settingsHelpButton);
	}
	@FXML
	private void openProfile() {
		gameTimer.stop();
		ProfileWindow.targetHandle = SettingsManager.userHandle;
		loadWindow("/fxml/window/profileWindow.fxml", new ProfileWindow(), settingsHelpButton);
	}
	@FXML
	private void openAchievements() {
		gameTimer.stop();
		loadWindow(
			"/fxml/window/achievementsWindow.fxml", new AchievementsWindow(), settingsHelpButton);
	}
	@FXML
	private void openPuzzles() {
		gameTimer.stop();
		loadWindow("/fxml/window/puzzlesWindow.fxml", new PuzzleWindow(), settingsHelpButton);
	}
	@FXML
	private void openTournaments() {
		gameTimer.stop();
		loadWindow(
			"/fxml/window/tournamentsWindow.fxml", new TournamentsWindow(), settingsHelpButton);
	}
	@FXML
	private void openLeaderboard() {
		gameTimer.stop();
		loadWindow(
			"/fxml/window/leaderboardWindow.fxml", new LeaderboardWindow(), settingsHelpButton);
	}
	@FXML
	private void openOpponentProfile() {
		gameTimer.stop();
		final State state = State.getState();
		if (state.opponentHandle != null) {
			ProfileWindow.targetHandle = state.opponentHandle;
		} else {
			final ResourceBundle bundle = Main.getBundle();
			ProfileWindow.targetHandle = bundle.getString("common.computer");
		}
		loadWindow("/fxml/window/profileWindow.fxml", new ProfileWindow(), settingsHelpButton);
	}
}