package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.textmessage.ConfirmText;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class ModificaImmagineProfiloCtrl {

    // ==========================================
    // SEQUENCE: Gestione profilo - Gestione dati personalo – Modifica immagine profilo – Aggiungi immagine
    // ==========================================

    // 1. L'artista cliccaAggiungiImmagine() dentro ModificaImmagineProfiloView.
    @FXML
    void cliccaAggiungiImmagine(ActionEvent event) {
        // 2. ModificaImmagineProfiloView crea ModificaImmagineProfiloCtrl (Automatico in JavaFX)

        try {
            // Controllo Sessione
            if (GestioneProfiloCtrl.artistaLoggato == null) {
                new ErrorText("Errore di sessione. Effettua nuovamente il login.").okay();
                Router.mostraAuthView(event);
                return;
            }

            // 3. ModificaImmagineProfiloCtrl recuperaImmagine()
            // Questo metodo apre il file system (messaggio perso verso il SO)
            String urlImmagine = recuperaImmagine(event);

            // Se l'utente non ha chiuso la finestra del SO senza scegliere nulla
            if (urlImmagine != null) {

                String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

                // 4. ModificaImmagineProfiloCtrl fa una query alla DBMSBoundary chiamata queryDBMSUpdateImmagineProfilo(urlImmagine : String)
                DBMSboundary.getInstance().queryDBMSUpdateImmagineProfilo(cf, urlImmagine);

                // 5. ModificaImmagineProfiloCtrl fa una setDati(urlImmagineProfilo) all'Artista Entity
                // Usiamo il setter appropriato per aggiornare la entity in RAM
                GestioneProfiloCtrl.artistaLoggato.setUrlImmagineProfilo(urlImmagine);

                // 6. ModificaImmagineProfiloCtrl crea successfulText, Artista cliccaOkay()
                SuccessfulText success = new SuccessfulText("Immagine aggiornata correttamente");
                success.okay(); // Il destroy è implicito

                // 7. ModificaImmagineProfiloCtrl invoca il metodo mostraModificaImmagineProfiloView().
                mostraModificaImmagineProfiloView(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Si è verificato un errore durante l'aggiornamento dell'immagine.").okay();
        }
    }

    /**
     * Metodo privato che interagisce con il Sistema Operativo per selezionare il file
     */
    private String recuperaImmagine(ActionEvent event) {
        // Otteniamo la finestra corrente per poter "appendere" il popup del FileChooser
        Window stage = ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona la tua immagine profilo");

        // Impostiamo dei filtri per permettere solo file immagine
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg")
        );

        // Mostra il popup di Windows/Mac all'utente
        File fileSelezionato = fileChooser.showOpenDialog(stage);

        // Se l'utente ha scelto un file, ritorniamo il suo percorso assoluto
        if (fileSelezionato != null) {
            return fileSelezionato.getAbsolutePath();
        }

        // Se l'utente ha cliccato "Annulla" nel popup
        return null;
    }


    // ==========================================
    // SEQUENCE: Gestione profilo – Modifica immagine profilo - Rimuovi immagine
    // ==========================================

    // 1. L'artista cliccaRimuoviImmagine() dentro ModificaImmagineProfiloView
    @FXML
    void cliccaRimuoviImmagine(ActionEvent event) {
        // 2. ModificaImmagineProfiloView crea ModificaImmagineProfiloCtrl (Automatico)

        if (GestioneProfiloCtrl.artistaLoggato == null) {
            new ErrorText("Errore di sessione.").okay();
            Router.mostraAuthView(event);
            return;
        }

        // 3. ModificaImmagineProfiloCtrl crea ConfirmText
        ConfirmText confirm = new ConfirmText("Cancellare immagine?");

        // 4. L'artista cliccaOkay() [Utilizziamo si() per rispecchiare il flusso "L'artista clicca Si"]
        if (confirm.si()) {
            try {
                String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

                // 5. ModificaImmagineProfiloCtrl fa una query alla DBMSBoundary chiamata updateDBMSDefaultImageProfile()
                DBMSboundary.getInstance().updateDBMSDefaultImageProfile(cf);

                // 6. ModificaImmagineProfiloCtrl fa una setDefaultImageProfile() all'artista Entity
                GestioneProfiloCtrl.artistaLoggato.setDefaultImageProfile();

                // 7. ModificaImmagineProfiloCtrl crea Sucessfultext, L'artista cliccaOkay()
                SuccessfulText success = new SuccessfulText("Immagine cancellata correttamente");
                success.okay();

                // 8. Viene invocato il metodo mostraModificaImmagineProfiloView().
                mostraModificaImmagineProfiloView(event);

            } catch (Exception e) {
                e.printStackTrace();
                new ErrorText("Errore durante la rimozione dell'immagine.").okay();
            }
        }
    }


    // ==========================================
    // METODI GLOBALI / STUB
    // ==========================================

    @FXML
    void mostraModificaImmagineProfiloView(ActionEvent event) {
        Router.mostraModificaImmagineProfiloView(event);
    }

    @FXML
    void tornaAGestioneDatiPersonali(ActionEvent event) {
        Router.mostraGestioneDatiPersonaliView(event);
    }
}