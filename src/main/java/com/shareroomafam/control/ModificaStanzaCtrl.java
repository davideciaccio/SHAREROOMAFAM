package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Documento;
import com.shareroomafam.entity.Stanza;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ModificaStanzaCtrl {

    // Casella di testo del ModificaNomeForm
    @FXML private TextField nuovoNomeStanzaField;

    // Checklists per Aggiungi Documenti
    @FXML private ListView<HBox> documentiDaInserireListView;
    @FXML private ListView<HBox> documentiScaricabiliModificaListView;

    // Questa variabile riceve la stanza su cui si è cliccato "Modifica" dalla GestioneStanzeCtrl
    public static Stanza stanzaInModifica;

    // Variabili di stato per il Sequence Aggiungi Documenti
    private static List<Documento> listaDocumentiNonInStanza = new ArrayList<>();
    private static List<Documento> listaDocumentiDainserire = new ArrayList<>();

    @FXML
    public void initialize() {
        // Popola la DocumentiDaInserireChecklist
        if (documentiDaInserireListView != null && !listaDocumentiNonInStanza.isEmpty()) {
            documentiDaInserireListView.getItems().clear();
            for (Documento doc : listaDocumentiNonInStanza) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                cb.setUserData(doc);
                File f = new File(doc.getPercorso());
                Label lbl = new Label(f.getName());
                row.getChildren().addAll(cb, lbl);
                documentiDaInserireListView.getItems().add(row);
            }
        }

        // Popola la DocumentiScaricabiliChecklist
        if (documentiScaricabiliModificaListView != null && !listaDocumentiDainserire.isEmpty()) {
            documentiScaricabiliModificaListView.getItems().clear();
            for (Documento doc : listaDocumentiDainserire) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                cb.setUserData(doc);
                File f = new File(doc.getPercorso());
                Label lbl = new Label(f.getName());
                row.getChildren().addAll(cb, lbl);
                documentiScaricabiliModificaListView.getItems().add(row);
            }
        }
    }


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
    // SEQUENCE: Gestione stanze – Modifica stanza – Aggiungi documenti
    // ==========================================

    // 1. L'artista cliccaAggiungiDocumenti() dentro ModificaStanzaView
    @FXML
    void cliccaAggiungiDocumenti(ActionEvent event) {
        // 2. ModificaStanzaView crea ModificaStanzaCtrl (JavaFX automatico)

        ResultSet rs = null;
        try {
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 3. ModificaStanzaCtrl fa una listaDocumentiNonInStanza = queryDocumentiNonInStanza() alla DBMSBoundary
            rs = DBMSboundary.getInstance().queryDocumentiNonInStanza(cf, stanzaInModifica.getIdStanza());

            listaDocumentiNonInStanza.clear();
            if (rs != null) {
                while(rs.next()) {
                    int idDoc = rs.getInt("idDocumento");
                    boolean vis = rs.getBoolean("visibile");
                    String percorso = rs.getString("percorso");
                    listaDocumentiNonInStanza.add(new Documento(idDoc, cf, vis, percorso));
                }
            }

            // 4. IF [listaDocumentiNonInStanza è vuota]
            if (listaDocumentiNonInStanza.isEmpty()) {
                // 4.1 ModificaStanzaCtrl crea ErrorText, l'Artista cliccaOkay()
                // 4.2 ModificaStanzaCtrl distrugge ErrorText
                new ErrorText("Tutti i documenti sono già stati inseriti").okay();

                // 4.3 ModificaStanzaCtrl invoca il metodo mostraModificaStanzaView()
                mostraModificaStanzaView(event);
            } else {
                // 5. ELSE [listaDocumentiNonInStanza non è vuota]
                // 5.1 ModificaStanzaCtrl crea DocumentiDainserireChecklist
                Router.mostraDocumentiDaInserireChecklist(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore nel recupero dei documenti dal database.").okay();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }

    // 5.3 L'artista cliccaAvanti() dentro DocumentiDainserireChecklist
    @FXML
    void cliccaAvantiAggiunta(ActionEvent event) {
        List<Documento> estrattiList = new ArrayList<>();

        // 5.2 L'artista selezionaDocumentiDainserire() dentro DocumentiDainserireChecklist
        for (HBox row : documentiDaInserireListView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);
            if (cb.isSelected()) {
                estrattiList.add((Documento) cb.getUserData());
            }
        }

        if (estrattiList.isEmpty()) {
            new ErrorText("Seleziona almeno un documento da aggiungere.").okay();
            return;
        }

        // 5.4 DocumentiDainserireChecklist fa passaDati() alla ModificaStanzaCtrl
        // Usiamo l'array per bypassare i limiti di ereditarietà generica di Java
        Documento[] documentiDaInserireArr = estrattiList.toArray(new Documento[0]);
        passaDati(event, documentiDaInserireArr);
    }

    // Overload 1: Riceve i documenti selezionati per l'aggiunta
    private void passaDati(ActionEvent event, Documento[] documentiDaInserireArr) {
        listaDocumentiDainserire.clear();
        for (Documento doc : documentiDaInserireArr) {
            listaDocumentiDainserire.add(doc);
        }

        // 5.5 ModificaStanzaCtrl crea DocumentiScaricabiliChecklist
        Router.mostraModificaDocumentiScaricabiliChecklist(event);
    }


    // 5.7 L'artista cliccaConferma() dentro DocumentiScaricabiliChecklist
    @FXML
    void cliccaConfermaAggiunta(ActionEvent event) {
        List<DocumentoDownload> confFinaleList = new ArrayList<>();

        // 5.6 L'artista selezionaDocumentiScaricabili() dentro DocumentiScaricabiliChecklist
        for (HBox row : documentiScaricabiliModificaListView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);
            Documento doc = (Documento) cb.getUserData();
            boolean scaricabile = cb.isSelected();
            confFinaleList.add(new DocumentoDownload(doc.getIdDocumento(), scaricabile));
        }

        // 5.8 DocumentiScaricabiliChecklist fa passaDati() alla ModificaStanzaCtrl
        DocumentoDownload[] documentiScaricabiliArr = confFinaleList.toArray(new DocumentoDownload[0]);
        passaDati(event, documentiScaricabiliArr);
    }

    // Overload 2: Riceve i documenti finali con il flag scaricabile configurato
    private void passaDati(ActionEvent event, DocumentoDownload[] documentiScaricabiliArr) {
        try {
            // 5.9 ModificaStanzaCtrl fa la setDati(...) sulla StanzaEntity
            // (La StanzaEntity attualmente non detiene la lista in RAM, l'operazione è concettualmente delegata al DBMS)

            for (DocumentoDownload config : documentiScaricabiliArr) {
                // 5.10 ModificaStanzaCtrl fa insertDocumentiStanzaDBMS() alla DBMSBoundary
                DBMSboundary.getInstance().insertDocumentiStanzaDBMS(stanzaInModifica.getIdStanza(), config.idDocumento, config.scaricabile);
            }

            // 5.11 ModificaStanzaCtrl crea SuccessfulText, l'Artista cliccaOkay()
            new SuccessfulText("Documenti aggiunti correttamente").okay();

            // 5.12 ModificaStanzaCtrl distrugge SuccessfulText, DocumentiScaricabiliChecklist e DocumentiDainserireChecklist
            listaDocumentiNonInStanza.clear();
            listaDocumentiDainserire.clear();

            // 5.13 ModificaStanzaCtrl invoca il metodo mostraModificaStanzaView()
            mostraModificaStanzaView(event);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante l'inserimento dei documenti nel DB.").okay();
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
        // Ripuliamo l'oggetto temporaneo prima di tornare alla lista generale
        stanzaInModifica = null;
        Router.mostraGestioneStanzeView(event);
    }

    // Stub pronti per i futuri Sequence Diagram della Modifica Stanza
    @FXML void cliccaRimuoviDocumenti(ActionEvent event) {}
    @FXML void cliccaRendiDocumentoScaricabile(ActionEvent event) {}

    // ==========================================
    // CLASSE SUPPORTO INTERNA
    // ==========================================
    private static class DocumentoDownload {
        int idDocumento;
        boolean scaricabile;
        DocumentoDownload(int id, boolean scaricabile) {
            this.idDocumento = id;
            this.scaricabile = scaricabile;
        }
    }
}