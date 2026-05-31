package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Artista;
import com.shareroomafam.textmessage.ConfirmText;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class GestioneProfiloCtrl {

    // Simulazione dell'Entity allocata in memoria (Sessione corrente dell'utente loggato)
    public static Artista artistaLoggato;

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
    // SEQUENCE: Gestione profilo - Cancella profilo
    // ==========================================

    // 1. L'artista cliccaCancellaProfilo() sulla GestioneProfiloView
    @FXML
    void cliccaCancellaProfilo(ActionEvent event) {

        // 2. GestioneProfiloView crea GestioneProfiloCtrl (Gestito in automatico da JavaFX)

        // 3. GestioneProfiloCtrl crea ConfirmText
        ConfirmText confirmText = new ConfirmText("Vuoi cancellare il profilo?");

        // 4. L'artista cliccaSi()
        boolean haCliccatoSi = confirmText.si();

        if (haCliccatoSi) {
            try {
                // Controllo di sicurezza: se per qualche motivo la sessione è vuota, blocchiamo l'operazione
                if (artistaLoggato == null) {
                    ErrorText errorText = new ErrorText("Errore di sessione. Nessun artista loggato.");
                    errorText.okay();
                    return;
                }

                // 5. GestioneProfiloCtrl fa la getCodiceFiscaleArtista dalla Entity Allocata in memoria di artista
                String cf = artistaLoggato.getCodiceFiscale();

                // 6. GestioneProfiloCtrl fa una query alla DBMSBoundary chiamata removeDBMSProfiloArtista()
                int righeCancellate = DBMSboundary.getInstance().removeDBMSProfiloArtista(cf);

                if (righeCancellate > 0) {
                    // 7. GestioneProfiloCtrl crea un SuccessfulText
                    SuccessfulText successText = new SuccessfulText("Operazione avvenuta con successo");

                    // 8. L'utente CliccaOkay()
                    successText.okay();

                    // 9. Viene Distrutta la entity Artista
                    artistaLoggato = null;
                    System.out.println("🗑️ Profilo eliminato definitivamente dal DBMS.");

                    // GestioneProfiloCtrl invoca il metodo MostraAuthView().
                    Router.mostraAuthView(event);
                } else {
                    ErrorText errorText = new ErrorText("Errore: impossibile trovare l'artista nel database.");
                    errorText.okay();
                }

            } catch (Exception e) {
                e.printStackTrace();
                ErrorText errorText = new ErrorText("Errore di connessione al database durante l'eliminazione.");
                errorText.okay();
            }
        }
    }


    // ==========================================
    // METODI DI ROUTING E SERVIZIO GLOBALI
    // ==========================================

    @FXML
    void tornaAllaHome(ActionEvent event) {
        Router.mostraHomePageArtistaView(event);
    }

    @FXML
    void cliccaGestioneDatiPersonali(ActionEvent event) {
        Router.mostraGestioneDatiPersonaliView(event);
    }
}