package com.shareroomafam.control;

import com.shareroomafam.textmessage.ConfirmText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneProfiloCtrl {

    // ==========================================
    // SEQUENCE: Gestione profilo - Disconnetti profilo
    // ==========================================

    // 1. L'utente cliccaDisconnettiProfilo() dentro GestioneProfiloView
    @FXML
    void cliccaDisconnettiProfilo(ActionEvent event) {

        // 2. GestioneProfiloView crea la GestioneProfiloCtrl (Gestito in automatico dal caricamento del FXML)

        // 3. GestioneProfiloCtrl crea un ConfirmText
        ConfirmText confirmText = new ConfirmText("Disconnettere il profilo?");

        // 4. L'artista cliccaSi() dentro il confirmtext, segue destroy del confirmText
        // La classe ConfirmText mostra l'alert, attende l'input e si "distrugge" restituendo il risultato.
        boolean haCliccatoSi = confirmText.si();

        if (haCliccatoSi) {
            // 5. GestioneprofiloCtrl invoca il metodo mostraAuthView(), e avviene la destroy della entity artista.

            // Logica di "destroy" dell'entità Artista. Dato che nel nostro sistema non stiamo
            // salvando l'entità globalmente ma la gestiamo a runtime, la pulizia dei dati avviene
            // implicitamente ricaricando la pagina di login (pulizia di eventuali sessioni future).
            System.out.println("🚪 Disconnessione confermata. Destroy della entity Artista in corso...");

            Router.mostraAuthView(event);
        }
    }

    // ==========================================
    // METODI DI ROUTING E SERVIZIO GLOBALI
    // ==========================================

    @FXML
    void tornaAllaHome(ActionEvent event) {
        Router.mostraHomePageArtistaView(event);
    }
}