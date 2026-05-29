package com.shareroomafam.textmessage;

import javafx.scene.control.Alert;

public class ErrorText {
    private Alert alert;

    public ErrorText(String message) {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
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