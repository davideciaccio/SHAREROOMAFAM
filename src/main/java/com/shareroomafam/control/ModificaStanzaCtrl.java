package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Stanza;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.sql.ResultSet;

public class ModificaStanzaCtrl {

    // Casella di testo del ModificaNomeForm
    @FXML private TextField nuovoNomeStanzaField;

    // Questa variabile riceve la stanza su cui si è cliccato "Modifica" dalla GestioneStanzeCtrl
    public static Stanza stanzaInModifica;


    // ==========================================
    // SEQUENCE: Gestione stanze – Modifica stanza – Modifica nome stanza
    // ==========================================

    // 1. L'artista cliccaModificaNomeStanza() dentro ModificaStanzaView
    @FXML
    void cliccaModificaNomeStanza(ActionEvent event) {
        // 2. ModificaStanzaView crea ModificaStanzaCtrl (Gestito da JavaFX)

        if (stanzaInModifica == null) {
            new ErrorText("Nessuna stanza selezionata.").okay();
            tornaAGestioneStanze(event);
            return;
        }

        // 3. ModificaStanzaCtrl crea ModificaNomeForm
        Router.mostraModificaNomeForm(event);
    }

    // 5. L'Artista cliccaSalva() dentro ModificaNomeForm
    @FXML
    void cliccaSalva(ActionEvent event) {
        // 4. L'artista InserisciNuovoNome() dentro ModificaNomeForm
        String nuovoNomeStanza = nuovoNomeStanzaField.getText();

        if (nuovoNomeStanza == null || nuovoNomeStanza.trim().isEmpty()) {
            new ErrorText("Il nome della stanza non può essere vuoto.").okay();
            return;
        }

        // 6. ModificaNomeForm fa PassaDati() alla ModificaStanzaCtrl
        PassaDati(event, nuovoNomeStanza.trim());
    }

    private void PassaDati(ActionEvent event, String nuovoNomeStanza) {
        ResultSet rs = null;
        try {
            // Controlliamo che il nome non sia lo stesso che ha già la stanza
            if (nuovoNomeStanza.equals(stanzaInModifica.getNomeStanza())) {
                new ErrorText("Il nome inserito è uguale a quello attuale.").okay();
                return;
            }

            // 7. ModificaStanzaCtrl fa una queryDBMSVerificaNomeStanza alla DBMSBoundary
            // passiamo il CF dell'artista loggato per verificare le SUE stanze
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();
            rs = DBMSboundary.getInstance().queryDBMSVerificaNomeStanza(cf, nuovoNomeStanza);

            boolean nomeGiaInUso = false;
            if (rs != null && rs.next()) {
                nomeGiaInUso = true;
            }

            // 8. IF nuovoNomeStanza già in uso
            if (nomeGiaInUso) {
                // 8.1 ModificaStanzaCtrl crea ErrorText, Artista cliccaOkay()
                new ErrorText("Nome stanza già in uso per questo profilo").okay();

                // 8.2 ModificaStanzaCtrl invoca il metodo mostraModificaStanzaView().
                mostraModificaStanzaView(event);

            } else {
                // 9. ELSE nuovoNomeStanza non in uso

                // 9.1 ModificaStanzaCtrl fa una query updateDBMSNomeStanza() in cui aggiorna il nome
                DBMSboundary.getInstance().updateDBMSNomeStanza(stanzaInModifica.getIdStanza(), nuovoNomeStanza);

                // 9.2 ModificaStanzaCtrl fa la setDatiNuovoNome() anche sulla EntityStanza
                stanzaInModifica.setNomeStanza(nuovoNomeStanza);

                // 9.3 ModificaStanzaCtrl crea SuccessfulText, L'artista cliccaOkay(),
                new SuccessfulText("Nome della stanza aggiornato correttamente").okay();

                // 9.4 ModificaStanzaCtrl invoca il metodo mostraModificaStanzaVuew()
                mostraModificaStanzaView(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante la connessione al database.").okay();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }


    // ==========================================
    // METODI DI ROUTING GLOBALI / STUB
    // ==========================================

    @FXML
    void mostraModificaStanzaView(ActionEvent event) {
        Router.mostraModificaStanzaView(event);
    }

    @FXML
    void tornaAGestioneStanze(ActionEvent event) {
        Router.mostraGestioneStanzeView(event);
    }

    // Stub pronti per i futuri Sequence Diagram della Modifica Stanza
    @FXML void cliccaAggiungiDocumenti(ActionEvent event) {}
    @FXML void cliccaRimuoviDocumenti(ActionEvent event) {}
    @FXML void cliccaRendiDocumentoScaricabile(ActionEvent event) {}
}