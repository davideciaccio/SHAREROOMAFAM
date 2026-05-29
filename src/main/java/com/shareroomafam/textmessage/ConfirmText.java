package com.shareroomafam.textmessage;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ConfirmText {
    private Alert alert;
    private boolean confirmed = false;

    public ConfirmText(String message) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma");
        alert.setHeaderText(null);
        alert.setContentText(message);
    }

    public boolean okay() {
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            confirmed = true;
        }
        destroy();
        return confirmed;
    }

    private void destroy() {
        alert.close();
    }
}