package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

import java.sql.ResultSet;

public class GestioneDatiPersonaliCtrl {

    // Casella di testo del ModificaPasswordForm
    @FXML private PasswordField nuovaPasswordField;

    // ==========================================
    // SEQUENCE: Gestione dati personali - Cambia password
    // ==========================================

    // 1. L'artista cliccaCambiaPassword() dentro GestioneDatiPersonaliVIew
    @FXML
    void cliccaCambiaPassword(ActionEvent event) {
        // 2. GestioneDatiPersonaliView crea GestioneDatiPersonaliCtrl (Gestito in automatico da JavaFX)

        // 3. GestioneDatiPersonaliCtrl crea ModificaPasswordForm
        Router.mostraModificaPasswordForm(event);
    }

    // 5. L'artista cliccaConferma() dentro ModificaPasswordForm
    @FXML
    void cliccaConferma(ActionEvent event) {
        // 4. L'artista InserisceNuovaPassword() dentro ModificaPasswordForm
        String nuovaPassword = nuovaPasswordField.getText();

        if (nuovaPassword == null || nuovaPassword.length() < 8) {
            ErrorText errorText = new ErrorText("Errore: la password deve avere almeno 8 caratteri.");
            errorText.okay();
            return;
        }

        // 6. ModificaPasswordForm fa passaDati() alla GestioneDatiPersonaliCtrl
        passaDati(event, nuovaPassword);
    }

    private void passaDati(ActionEvent event, String nuovaPassword) {
        ResultSet rs = null;
        try {
            // Controllo preliminare sulla sessione in memoria
            if (GestioneProfiloCtrl.artistaLoggato == null) {
                new ErrorText("Errore di sessione. Riprova ad accedere.").okay();
                Router.mostraAuthView(event);
                return;
            }

            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 7. GestioneDatiPersonaliCtrl fa una queryDBMSVerificaPassword() alla DBMSBoundary
            // La query cerca l'artista usando CF e la NUOVA password, se lo trova significa che la nuova password è uguale alla vecchia
            rs = DBMSboundary.getInstance().queryDBMSVerificaPassword(cf, nuovaPassword);

            boolean passwordUgualeAttuale = false;
            if (rs != null && rs.next()) {
                passwordUgualeAttuale = true;
            }

            // 8. IF Password uguale a quella attuale
            if (passwordUgualeAttuale) {
                // 8.1 GestioneDatiPersonaliCtrl crea un ErrorText, L'artista cliccaOkay(), segueDestroy
                ErrorText errorText = new ErrorText("Password già in uso");
                errorText.okay();

                // segue mostraGestioneDatiPersonaliView()
                mostraGestioneDatiPersonaliView(event);
            } else {
                // 9. ELSE Password diversa da quella attuale

                // 9.1 GestioneDatiPersonaliCtrl fa una updateDBMSPassword() alla DBMSBoundary e aggiorna il db con la nuovapassword
                DBMSboundary.getInstance().updateDBMSPassword(cf, nuovaPassword);

                // 9.2 GestioneDatiPersonaliCtrl fa una SetNuovaPassword() sulla entity Artista
                // Usiamo il setter dell'entity salvata nella sessione
                GestioneProfiloCtrl.artistaLoggato.setPassword(nuovaPassword);

                // 9.3 GestioneDatiPersonaliCtrl crea SuccessfulText, L'artista cliccaOkay(), destroy
                SuccessfulText successText = new SuccessfulText("Password aggiornata correttamente");
                successText.okay();

                // e invocazione del metodo mostraGestioneDatiPersonaliView().
                mostraGestioneDatiPersonaliView(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) {
                    java.sql.Statement stmt = rs.getStatement();
                    if (stmt != null) stmt.close();
                }
            } catch (Exception ignore) {}
        }
    }


    // ==========================================
    // METODI DI ROUTING E SERVIZIO GLOBALI
    // ==========================================

    @FXML
    void mostraGestioneDatiPersonaliView(ActionEvent event) {
        Router.mostraGestioneDatiPersonaliView(event);
    }

    @FXML
    void tornaAGestioneProfilo(ActionEvent event) {
        Router.mostraGestioneProfiloView(event);
    }

    // Stub pronti per i futuri Sequence Diagram del RAD
    @FXML void cliccaModificaImmagine(ActionEvent event) {}
    @FXML void cliccaModificaNomeDarte(ActionEvent event) {}
    @FXML void cliccaModificaCarriera(ActionEvent event) {}
}