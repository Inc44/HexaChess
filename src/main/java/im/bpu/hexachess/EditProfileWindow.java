package im.bpu.hexachess;

import im.bpu.hexachess.entity.Player;
import im.bpu.hexachess.network.API;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static im.bpu.hexachess.Main.loadWindow;

public class EditProfileWindow {

    @FXML private TextField emailField;
    @FXML private TextField locationField;
    @FXML private TextField avatarField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField currentPasswordField;
    @FXML private Label statusLabel;
    @FXML private Button backButton;

    private String myHandle;

    @FXML
    private void initialize() {
        myHandle = SettingsManager.userHandle;
        if (myHandle != null) {
            loadCurrentData();
        }
    }

    private void loadCurrentData() {
        new Thread(() -> {
            Player me = API.profile(myHandle);
            if (me != null) {
                javafx.application.Platform.runLater(() -> {
                    emailField.setText(me.getEmail());
                    locationField.setText(me.getLocation());
                    avatarField.setText(me.getAvatar());
                    currentPasswordField.clear();
                    newPasswordField.clear();
                });
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        String currentPass = currentPasswordField.getText();
        if (currentPass == null || currentPass.isEmpty()) {
            statusLabel.setText("Current password is required!");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setVisible(true);
            return;
        }

        if (API.updateProfile(currentPass, emailField.getText(), locationField.getText(), avatarField.getText(), newPasswordField.getText())) {
            statusLabel.setText("Profile updated successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setVisible(true);
            currentPasswordField.clear();
            newPasswordField.clear();
        } else {
            statusLabel.setText("Update failed (Wrong password?)");
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setVisible(true);
        }
    }

    @FXML
    private void handleCancel() {
        ProfileWindow.targetHandle = SettingsManager.userHandle;
        loadWindow("ui/profileWindow.fxml", new ProfileWindow(), backButton);
    }
}