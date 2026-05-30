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

    // Metodo originale per gestire i Sequence Diagram in cui l'utente "cliccaOkay()"
    public boolean okay() {
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            confirmed = true;
        }
        destroy();
        return confirmed;
    }

    // NUOVO metodo per gestire i Sequence Diagram in cui l'utente "cliccaSi()"
    public boolean si() {
        // Rinominiamo i bottoni in "Sì" e "No"
        ButtonType btnSi = new ButtonType("Sì");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnSi, btnNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSi) {
            confirmed = true;
        }
        destroy();
        return confirmed;
    }

    private void destroy() {
        alert.close();
    }
}