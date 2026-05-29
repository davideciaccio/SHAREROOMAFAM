package com.shareroomafam.textmessage;

import javafx.scene.control.Alert;

public class SuccessfulText {
    private Alert alert;

    public SuccessfulText(String message) {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Successo");
        alert.setHeaderText(null);
        alert.setContentText(message);
    }

    public void okay() {
        alert.showAndWait(); // Attende che l'utente clicchi "Okay"
        destroy();
    }

    private void destroy() {
        alert.close();
    }
}