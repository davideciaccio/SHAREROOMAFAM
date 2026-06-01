package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Documento;
import com.shareroomafam.entity.Stanza;
import com.shareroomafam.textmessage.ConfirmText;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GestioneStanzeCtrl {

    // Campi FXML
    @FXML private ListView<HBox> stanzeListView; // In GestioneStanzeView
    @FXML private TextField nomeStanzaField;     // In InserisciNomeStanzaForm
    @FXML private ListView<HBox> documentiChecklistView; // In DocumentiChecklist
    @FXML private ListView<HBox> documentiScaricabiliListView; // In DocumentiScaricabiliChecklist
    @FXML private Label linkStanzaLabel; // In FinestraCopiaLinkView
    @FXML private ListView<String> visualizzatoriListView; // In ListaVisualizzatoriView

    // --- VARIABILI DI STATO TEMPORANEE (Sessione di creazione stanza) ---
    private static String nomeStanzaTemporaneo;
    private static List<Documento> listaDocumentiTotali = new ArrayList<>();
    private static List<Documento> listaDocumentiSelezionati = new ArrayList<>();
    // --- VARIABILI DI STATO TEMPORANEE (Sessione di condivisione e monitoraggio) ---
    private static String linkDaCopiare;
    private static List<String> listaVisualizzatoriTemporanea = new ArrayList<>();

    // Lista delle stanze attualmente caricate (per aggiornare la view)
    private static List<Stanza> listaStanzeAggiornata = new ArrayList<>();

    @FXML
    public void initialize() {
        // Popola la GestioneStanzeView
        if (stanzeListView != null) {
            stanzeListView.getItems().clear();

            // INTERROGHIAMO PER RECUPERARE LE STANZE AL CARICAMENTO DELLA PAGINA <---
            if (GestioneProfiloCtrl.artistaLoggato != null) {
                ResultSet rs = null;
                try {
                    String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();
                    rs = DBMSboundary.getInstance().queryDBMSListaStanze(cf);
                    listaStanzeAggiornata.clear();

                    if (rs != null) {
                        while(rs.next()) {
                            int id = rs.getInt("idStanza");
                            String nome = rs.getString("nomeStanza");
                            String link = rs.getString("link");
                            listaStanzeAggiornata.add(new Stanza(id, cf, nome, link));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (rs != null && !rs.isClosed()) rs.getStatement().close();
                    } catch (Exception ignore) {}
                }
            }
            // Popoliamo l'interfaccia grafica
            for (Stanza s : listaStanzeAggiornata) {
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);

                Label lblTesto = new Label("🏠 " + s.getNomeStanza() + " (Link: " + s.getLink() + ")");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                // Configurazione Bottone Condividi
                Button btnCondividi = new Button("Condividi");
                btnCondividi.setUserData(s.getIdStanza()); // Salviamo l'ID stanza nel bottone
                btnCondividi.setOnAction(this::cliccaCondividiStanza); // Colleghiamo l'azione

                // Configurazione Bottone Monitoraggio
                Button btnMonitora = new Button("Monitoraggio");
                btnMonitora.setUserData(s.getIdStanza()); // Salviamo l'ID stanza nel bottone
                btnMonitora.setOnAction(this::cliccaMonitoraggio); // Colleghiamo l'azione

                // Configurazione Bottone Modifica
                Button btnModifica = new Button("Modifica");
                btnModifica.setUserData(s); // PASSAGGIO CHIAVE: Salviamo l'intero oggetto Stanza nel bottone!
                btnModifica.setOnAction(this::cliccaModificaStanza); // Colleghiamo l'azione

                // Configurazione Bottone Elimina Stanza
                Button btnElimina = new Button("Elimina");
                btnElimina.setStyle("-fx-text-fill: red;");
                btnElimina.setUserData(s.getIdStanza()); // Salviamo l'ID stanza per l'eliminazione
                btnElimina.setOnAction(this::cliccaEliminaStanza); // Colleghiamo l'azione
                btnElimina.setStyle("-fx-text-fill: red;");

                row.getChildren().addAll(lblTesto, spacer, btnCondividi, btnMonitora, btnModifica, btnElimina);
                stanzeListView.getItems().add(row);
            }
        }

        // Popola la DocumentiChecklist (Punto 11.1)
        if (documentiChecklistView != null && !listaDocumentiTotali.isEmpty()) {
            documentiChecklistView.getItems().clear();
            for (Documento doc : listaDocumentiTotali) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                cb.setUserData(doc); // Nascondiamo l'oggetto doc nel checkbox
                File f = new File(doc.getPercorso());
                Label lbl = new Label(f.getName());
                row.getChildren().addAll(cb, lbl);
                documentiChecklistView.getItems().add(row);
            }
        }

        // Popola la DocumentiScaricabiliChecklist (Punto 11.5)
        if (documentiScaricabiliListView != null && !listaDocumentiSelezionati.isEmpty()) {
            documentiScaricabiliListView.getItems().clear();
            for (Documento doc : listaDocumentiSelezionati) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                CheckBox cb = new CheckBox();
                cb.setUserData(doc);
                File f = new File(doc.getPercorso());
                Label lbl = new Label(f.getName());
                row.getChildren().addAll(cb, lbl);
                documentiScaricabiliListView.getItems().add(row);
            }
        }

        // Popola la FinestraCopiaLinkView
        if (linkStanzaLabel != null && linkDaCopiare != null) {
            linkStanzaLabel.setText(linkDaCopiare);
        }

        // Popola la ListaVisualizzatoriView
        if (visualizzatoriListView != null) {
            visualizzatoriListView.getItems().clear();
            if (listaVisualizzatoriTemporanea.isEmpty()) {
                visualizzatoriListView.getItems().add("Nessuna visualizzazione registrata per questa stanza.");
            } else {
                visualizzatoriListView.getItems().addAll(listaVisualizzatoriTemporanea);
            }
        }
    }


    // ==========================================
    // SEQUENCE: Gestione stanze - Crea stanza
    // ==========================================

    // 1. L'artista cliccaCreaStanza() su GestioneStanzeView
    @FXML
    void cliccaCreaStanza(ActionEvent event) {
        // 2. GestioneStanzeView crea GestioneStanzeCtrl
        // 3. GestioneStanzeCtrl crea InserisciNomeStanzaForm
        Router.mostraInserisciNomeStanzaForm(event);
    }

    // 5. L'artista cliccaAvanti() dentro inserisciNomeStanzaForm
    @FXML
    void cliccaAvantiNome(ActionEvent event) {
        // 4. L'artista inserisciNomeStanza() dentro InserisciNomeStanzaForm
        String nomeInserito = nomeStanzaField.getText();
        if (nomeInserito == null || nomeInserito.trim().isEmpty()) {
            new ErrorText("Il nome della stanza non può essere vuoto.").okay();
            return;
        }

        // 6. InserisciNomeStanzaForm fa passaDati() a GestioneStanzeCtrl
        passaDatiNome(event, nomeInserito.trim());
    }

    private void passaDatiNome(ActionEvent event, String nomeStanza) {
        ResultSet rs = null;
        try {
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 7. GestioneStanzeCtrl fa una query queryDBMSVerificaNomeStanza()
            rs = DBMSboundary.getInstance().queryDBMSVerificaNomeStanza(cf, nomeStanza);
            boolean esisteGia = false;
            if (rs != null && rs.next()) {
                esisteGia = true;
            }

            // 8. IF nome stanza già in uso
            if (esisteGia) {
                // 8.1 GestioneStanzeCtrl crea ErroText, segue Artista cliccaOkay(), segue invoca mostraGestioneStanzeView()
                new ErrorText("Nome stanza già in uso").okay();
                mostraGestioneStanzeView(event);
            } else {
                // 9. ELSE nome stanza non in uso
                nomeStanzaTemporaneo = nomeStanza; // Salva in memoria per l'ultimo step

                // 9.1 GestioneStanzeCtrl fa una getCodiceFiscaleArtista() (Già fatto sopra)
                // 9.2 GestioneStanzeCtrl fa queryDBMSListaDocumenti()
                ResultSet rsDoc = DBMSboundary.getInstance().queryDBMSListaDocumenti(cf);

                listaDocumentiTotali.clear();
                if (rsDoc != null) {
                    while(rsDoc.next()) {
                        int idDoc = rsDoc.getInt("idDocumento");
                        boolean vis = rsDoc.getBoolean("visibile");
                        String percorso = rsDoc.getString("percorso");
                        listaDocumentiTotali.add(new Documento(idDoc, cf, vis, percorso));
                    }
                    rsDoc.getStatement().close();
                }

                // 10. IF listaDocumenti vuota
                if (listaDocumentiTotali.isEmpty()) {
                    // 10.1 GestioneStanzeCtrl crea ErrorText... mostraGestioneStanzeView()
                    new ErrorText("Nessun documento presente").okay();
                    mostraGestioneStanzeView(event);
                } else {
                    // 11. ELSE listaDocumenti NON vuota
                    // 11.1 GestioneStanzeCtrl crea DocumentiChecklist
                    Router.mostraDocumentiChecklist(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }

    // 11.3 L'artista cliccaAvanti() dentro DocumentiChecklist
    @FXML
    void cliccaAvantiDocumenti(ActionEvent event) {
        listaDocumentiSelezionati.clear();

        // 11.2 L'artista selezioneDocumenti() dentro Documenti checklist
        for (HBox row : documentiChecklistView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);
            if (cb.isSelected()) {
                listaDocumentiSelezionati.add((Documento) cb.getUserData());
            }
        }

        if (listaDocumentiSelezionati.isEmpty()) {
            new ErrorText("Seleziona almeno un documento per la stanza.").okay();
            return;
        }

        // 11.4 DocumentiChecklist fa la passaDatiDocumentiSelezionati()
        // 11.5 GestioneStanzeCtrl crea DocumentiScaricabiliChecklist popolata dai documenti selezionati
        Router.mostraDocumentiScaricabiliChecklist(event);
    }


    // 11.7 L'artista cliccaConferma() dentro DocumentiScaricabiliChecklist
    @FXML
    void cliccaConfermaCreazione(ActionEvent event) {
        // 11.6 L'artista selezionaDocumentiDaRendereScaricabili()
        List<DocumentoDownload> configurazioneFinale = new ArrayList<>();

        for (HBox row : documentiScaricabiliListView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);
            Documento doc = (Documento) cb.getUserData();
            boolean scaricabile = cb.isSelected();

            configurazioneFinale.add(new DocumentoDownload(doc.getIdDocumento(), scaricabile));
        }

        // 11.8 DocumentiScaricabiliChecklist fa passaDati()
        passaDatiCreazioneFinale(event, configurazioneFinale);
    }

    private void passaDatiCreazioneFinale(ActionEvent event, List<DocumentoDownload> confDocumenti) {
        try {
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 11.9 GestioneStanzeCtrl genera un linkStanza univoco
            String linkUnivoco = generaLinkStanza();

            // 11.10 GestioneStanzeCtrl fa la insertDBMSStanza() per inserire la stanza nel db
            int idStanzaGenerata = DBMSboundary.getInstance().insertDBMSStanza(cf, nomeStanzaTemporaneo, linkUnivoco);

            // Inserisce le relazioni nella tabella CONTIENE
            for (DocumentoDownload d : confDocumenti) {
                DBMSboundary.getInstance().insertDBMSContieneDocumento(idStanzaGenerata, d.idDocumento, d.scaricabile);
            }

            // 11.11 GestioneStanzeCtrl crea una StanzaEntity e fa il setDatiStanza()
            Stanza nuovaStanzaEntity = new Stanza(idStanzaGenerata, cf, nomeStanzaTemporaneo, linkUnivoco);

            // 11.12 GestioneStanzeCtrl crea SuccessfulText, l'artista cliccaOkay()
            new SuccessfulText("Stanza creata correttamente!").okay();

            // Svuota i file temporanei
            nomeStanzaTemporaneo = null;
            listaDocumentiTotali.clear();
            listaDocumentiSelezionati.clear();

            // Aggiorna la vista globale (Punti 11.13 e 11.14)
            aggiornaEmostraVista(event, cf);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante la creazione della stanza nel DB.").okay();
        }
    }


    // 11.9 Metodo generatore di link
    private String generaLinkStanza() {
        // Sostituito "shareroom.com/s/" con il link diretto di localhost per test immediati
        return "http://localhost:8080/s/" + UUID.randomUUID().toString().substring(0, 8);
    }

    // Esegue 11.13 e 11.14
    private void aggiornaEmostraVista(ActionEvent event, String cf) {
        // La query al DB è stata spostata nell'initialize() per garantire che venga
        // eseguita ogni volta che si entra nella GestioneStanzeView
        Router.mostraGestioneStanzeView(event);
    }


    // ==========================================
    // SEQUENCE: Gestione stanze – Condividi stanza
    // ==========================================

    // 1. L'artista cliccaCondividiStanza() dentro GestioneStanzeView
    @FXML
    void cliccaCondividiStanza(ActionEvent event) {
        // Estraiamo il bottone che ha generato l'evento
        Button btnPremuto = (Button) event.getSource();

        // 3. GestioneStanzeCtrl recupera l'iDStanza con un metodo getIdSstanza()
        int idStanza = (Integer) btnPremuto.getUserData();

        ResultSet rs = null;
        try {
            // 4. GestioneStanzeCtrl fa una queryDBMSLinkStanza() per recuperare il link
            rs = DBMSboundary.getInstance().queryDBMSLinkStanza(idStanza);

            if (rs != null && rs.next()) {
                linkDaCopiare = rs.getString("link");

                // 5. GestioneStanzeCtrl crea FinestraCopiaLinkView
                Router.mostraFinestraCopiaLinkView(event);
            } else {
                new ErrorText("Impossibile recuperare il link della stanza.").okay();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }

    // 6. L'artista cliccaCopiaLink() dentro FinestraCopiaLinkView
    @FXML
    void cliccaCopiaLink(ActionEvent event) {

        // 7. Il link viene copiato negli appunti (utilizzando l'API Clipboard di JavaFX)
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(linkDaCopiare);
        clipboard.setContent(content);

        // (Mostra un piccolo popup)
        SuccessfulText success = new SuccessfulText("Link copiato negli appunti con successo!");
        success.okay();

        // 8. GestioneStanzeCtrl invoca il metodo mostraGestioneStanzeView.
        mostraGestioneStanzeView(event);
    }


    // ==========================================
    // SEQUENCE: Gestione stanze – Monitoraggio condivisione stanza
    // ==========================================

    // 1. L'artista cliccaMonitoraggio() su GestioneStanzeView
    @FXML
    void cliccaMonitoraggio(ActionEvent event) {
        Button btnPremuto = (Button) event.getSource();

        // 3. GestioneStanzeCtrl recupera l'id della stanza
        int idStanza = (Integer) btnPremuto.getUserData();

        ResultSet rs = null;
        try {
            // 4. GestioneStanzeCtrl fa una queryDBMSListaVisualizzatori alla DBMSBoundary
            rs = DBMSboundary.getInstance().queryDBMSListaVisualizzatori(idStanza);

            listaVisualizzatoriTemporanea.clear();

            if (rs != null) {
                while (rs.next()) {
                    // Leggiamo i nuovi campi estratti dalla JOIN
                    String nome = rs.getString("nomeVisualizzatore");
                    String cognome = rs.getString("cognomeVisualizzatore");
                    String email = rs.getString("emailVisualizzatore");
                    String dataVis = rs.getString("dataVisualizzazione");

                    // Creiamo una stringa di monitoraggio elegante e completa
                    listaVisualizzatoriTemporanea.add("👤 " + nome + " " + cognome + " (" + email + ") - 📅 " + dataVis);
                }
            }

            // 5. GestioneStanzeCtrl crea ListaVisualizzatoriView
            Router.mostraListaVisualizzatoriView(event);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante il recupero dei visualizzatori.").okay();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }

    // 6. L'artista cliccaChiudi() dentro listaVisualizzatoriView.
    @FXML
    void cliccaChiudi(ActionEvent event) {
        listaVisualizzatoriTemporanea.clear();

        // 7. GestioneStanzeCtrl invoca il metodo mostraGestioneStanzeView().
        mostraGestioneStanzeView(event);
    }


    // ==========================================
    // SEQUENCE: Gestione stanze – Elimina stanza
    // ==========================================

    // 1. L'artista cliccaEliminaStanza() dentro GestioneStanzeView
    @FXML
    void cliccaEliminaStanza(ActionEvent event) {
        // 2. GestioneStanzeView crea GestioneStanzeCtrl (Automatico in JavaFX)
        Button btnPremuto = (Button) event.getSource();

        // 3. GestioneStanzeCtrl recupera idStanza della stanza che si vuole eliminare
        int idStanza = (Integer) btnPremuto.getUserData();

        // 4. GestioneStanzeCtrl crea ConfirmText
        ConfirmText confirmText = new ConfirmText("Eliminare stanza?");

        // 5. L'artista cliccaSi()
        if (confirmText.si()) {
            ResultSet rs = null;
            try {
                String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

                // 6. GestioneStanzeCtrl fa una query updateDBMSStanza() alla DBMSboundary...
                // elimina la stanza e ritorna la lista delle stanze aggiornata
                rs = DBMSboundary.getInstance().updateDBMSStanza(idStanza, cf);

                listaStanzeAggiornata.clear();
                if (rs != null) {
                    while (rs.next()) {
                        int id = rs.getInt("idStanza");
                        String nome = rs.getString("nomeStanza");
                        String link = rs.getString("link");
                        listaStanzeAggiornata.add(new Stanza(id, cf, nome, link));
                    }
                }

                // 7. GestioneStanzeCtrl crea SuccessfulText
                SuccessfulText success = new SuccessfulText("Stanza eliminata");

                // 8. L'artista cliccaOkay()
                success.okay();

                // 9. GestioneStanzeCtrl invoca il metodo mostraGestioneStanzeView() popolato dalla nuova lista
                mostraGestioneStanzeView(event);

            } catch (Exception e) {
                e.printStackTrace();
                new ErrorText("Errore durante l'eliminazione della stanza.").okay();
            } finally {
                try {
                    if (rs != null && !rs.isClosed()) rs.getStatement().close();
                } catch (Exception ignore) {}
            }
        }
    }


    // ==========================================
    // METODI GLOBALI / STUB
    // ==========================================

    @FXML
    void mostraGestioneStanzeView(ActionEvent event) {
        Router.mostraGestioneStanzeView(event);
    }

    @FXML
    void tornaAllaHome(ActionEvent event) {
        Router.mostraHomePageArtistaView(event);
    }

    @FXML
    void cliccaModificaStanza(ActionEvent event) {
        Button btnPremuto = (Button) event.getSource();
        Stanza stanzaSelezionata = (Stanza) btnPremuto.getUserData();

        // Passiamo la stanza selezionata alla nuova Control prima di cambiare vista
        ModificaStanzaCtrl.stanzaInModifica = stanzaSelezionata;

        Router.mostraModificaStanzaView(event);
    }

    // Support class per il passaggio di parametri misti id/boolean
    private static class DocumentoDownload {
        int idDocumento;
        boolean scaricabile;
        DocumentoDownload(int id, boolean scaricabile) {
            this.idDocumento = id;
            this.scaricabile = scaricabile;
        }
    }
}