package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Artista;
import com.shareroomafam.entity.Utente;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.awt.Desktop;
import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualizzaProfiliCtrl {

    // Campi FXML
    @FXML private TextField ricercaField;           // In CercaArtistaView
    @FXML private ListView<HBox> risultatiListView; // In ListaArtistiView
    // Campi FXML Filtro Dati Artistici
    @FXML private TextField carrieraFiltroField;      // In CampiFiltriForm
    @FXML private TextField anniCarrieraFiltroField;  // In CampiFiltriForm
    // Campi FXML ProfiloView
    @FXML private Label profiloNomeDarteLabel;
    @FXML private Label profiloNomeLabel;
    @FXML private Label profiloCognomeLabel;
    @FXML private Label profiloSessoLabel;
    @FXML private Label profiloEmailLabel;
    @FXML private ImageView profiloImmagine;
    @FXML private ListView<String> documentiListView;

    // Variabili di stato
    private Utente utenteCorrente;
    private static Artista artistaDaVisualizzare; // Per passare i dati alla ProfiloView

    // Lista statica per passare i dati dei risultati tra la schermata di ricerca e la schermata della lista
    private static List<String[]> risultatiRicercaTemporanei = new ArrayList<>();

    // Mappa per collegare il testo visualizzato nella lista al percorso reale del file
    private Map<String, String> mappaDocumenti = new HashMap<>();

    @FXML
    public void initialize() {
        // 3. VisualizzaProfiliCtrl crea un UtenteEntity
        if (utenteCorrente == null) {
            // Genera un ID univoco casuale (positivo) per la sessione dell'utente anonimo
            int idUnivoco = Math.abs(new java.util.Random().nextInt());

            // Usa il costruttore corretto della tua Entity
            utenteCorrente = new Utente(idUnivoco);
            System.out.println("✅ Entity Utente creata (Accesso pubblico/anonimo) con ID temporaneo: " + idUnivoco);
        }

        // 4. VisualizzaProfiliCtrl crea una view chiamata CercaArtistaView (Avviene automaticamente al caricamento del FXML)

        // Se ci troviamo nella ListaArtistiView, popoliamo la lista a schermo con i bottoni "Visualizza" per ogni artista
        if (risultatiListView != null) {
            risultatiListView.getItems().clear();

            for (String[] datiArtista : risultatiRicercaTemporanei) {
                String cf = datiArtista[0];
                String testoDisplay = datiArtista[1];

                // Creiamo un contenitore orizzontale (HBox) per riga
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                // Creiamo il testo e il bottone richiesto dal RAD
                Label lblTesto = new Label(testoDisplay);
                Button btnVisualizza = new Button("Visualizza");
                btnVisualizza.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-weight: bold;");

                // Nascondiamo il CF dentro il bottone, e colleghiamo l'azione al metodo del Sequence!
                btnVisualizza.setUserData(cf);
                btnVisualizza.setOnAction(this::cliccaVisualizza);

                // Impaginazione (il testo a sinistra, il bottone tutto a destra)
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                row.getChildren().addAll(lblTesto, spacer, btnVisualizza);
                risultatiListView.getItems().add(row);
            }
        }

        // --- POPOLAMENTO DATI PROFILO (ProfiloView) ---
        if (profiloNomeDarteLabel != null && artistaDaVisualizzare != null) {
            profiloNomeDarteLabel.setText(artistaDaVisualizzare.getNomeDarte());
            profiloNomeLabel.setText(artistaDaVisualizzare.getNome());
            profiloCognomeLabel.setText(artistaDaVisualizzare.getCognome());
            profiloSessoLabel.setText(artistaDaVisualizzare.getSesso());
            profiloEmailLabel.setText(artistaDaVisualizzare.getEmail());

            // --- CARICAMENTO IMMAGINE PROFILO ---
            String pathImmagine = artistaDaVisualizzare.getUrlImmagineProfilo();
            if (pathImmagine != null && !pathImmagine.trim().isEmpty()) {
                try {
                    File fileImmagine = new File(pathImmagine);
                    if (fileImmagine.exists()) {
                        // JavaFX richiede un URI formattato per caricare file locali
                        Image image = new Image(fileImmagine.toURI().toString());
                        profiloImmagine.setImage(image);
                    } else {
                        System.out.println("Immagine non trovata nel percorso: " + pathImmagine);
                    }
                } catch (Exception e) {
                    System.out.println("Impossibile caricare l'immagine del profilo.");
                }
            }
            // --- CARICAMENTO E GESTIONE DOCUMENTI PUBBLICI (VISIBILI) ---
            if (documentiListView != null) {
                documentiListView.getItems().clear();
                mappaDocumenti.clear(); // Pulisce la memoria precedente

                ResultSet rsDocs = null;
                try {
                    rsDocs = DBMSboundary.getInstance().queryDBMSDocumentiVisibili(artistaDaVisualizzare.getCodiceFiscale());
                    boolean haDocumenti = false;

                    if (rsDocs != null) {
                        while (rsDocs.next()) {
                            haDocumenti = true;
                            String percorso = rsDocs.getString("percorso");

                            File fileDoc = new File(percorso);
                            String testoItem = "📄 " + fileDoc.getName();

                            // Aggiungiamo alla vista e alla mappa segreta
                            documentiListView.getItems().add(testoItem);
                            mappaDocumenti.put(testoItem, percorso);
                        }
                    }

                    if (!haDocumenti) {
                        documentiListView.getItems().add("Nessun documento pubblico caricato.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    documentiListView.getItems().add("Errore nel caricamento documenti.");
                } finally {
                    try {
                        if (rsDocs != null && !rsDocs.isClosed()) rsDocs.getStatement().close();
                    } catch (Exception ignore) {}
                }

                // NUOVO: Aggiungiamo il listener per aprire il file con il DOPPIO CLIC
                documentiListView.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        String itemSelezionato = documentiListView.getSelectionModel().getSelectedItem();
                        // Controlla se la riga cliccata è davvero un documento mappato
                        if (itemSelezionato != null && mappaDocumenti.containsKey(itemSelezionato)) {
                            apriDocumentoConOS(mappaDocumenti.get(itemSelezionato));
                        }
                    }
                });
            }
        }
    }

    // ==========================================
    // SEQUENCE: Visualizza profili - Cerca Artista
    // ==========================================

    // 6. L'utente cliccaCerca() dentro CercaArtistaView
    @FXML
    void cliccaCerca(ActionEvent event) {
        // 5. L'utente inserisceDatiRicerca() dentro CercaArtistaView
        String datiRicerca = ricercaField.getText();

        if (datiRicerca == null || datiRicerca.trim().isEmpty()) {
            return; // Evita ricerche a vuoto
        }

        // 7. CercaArtistaView fa la passaDati a VisualizzaProfiliCtrl
        passaDati(event, datiRicerca);
    }

    private void passaDati(ActionEvent event, String dati) {
        ResultSet rs = null;
        try {
            // 8. VisualizzaProfiliCtrl fa una query alla DBMSBoundary chiamata queryDBMSCercaArtista()
            rs = DBMSboundary.getInstance().queryDBMSCercaArtista(dati);

            risultatiRicercaTemporanei.clear();
            boolean artistaTrovato = false;

            if (rs != null) {
                while (rs.next()) {
                    artistaTrovato = true;
                    // Estrae nome, cognome e nome d'arte come da direttiva
                    String cf = rs.getString("codiceFiscale");
                    String nomeDarte = rs.getString("nomeDarte");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");

                    // Formatta la stringa da mostrare nella lista (es. "Michelangelo (Michelangelo Buonarroti)")
                    risultatiRicercaTemporanei.add(new String[]{cf, "🎭 " + nomeDarte + " - (" + nome + " " + cognome + ")"});
                }
            }

            // 9. IF Nessun Artista trovato
            if (!artistaTrovato) {
                // 9.1 VisualizzaProfiliCtrl crea ErrorText(), l'utente CliccaOkay(), segue destroy
                ErrorText errorText = new ErrorText("Nessun artista trovato");
                errorText.okay();

                // VisualizzaProfiliCtrl invoca il metodo mostraCercaArtistaView()
                mostraCercaArtistaView(event);
            } else {
                // 10. ALTRIMENTI
                // crea ListaArtistiView
                Router.mostraListaArtistiView(event);
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
    // SEQUENCE: Visualizza profili - Filtra per dati artistici
    // ==========================================

    // 1. L'utente cliccaIconaFiltra() dentro CercaArtistaView
    @FXML
    void cliccaIconaFiltra(ActionEvent event) {
        // 2. CercaArtistaView crea VisualizzaProfiliCtrl (Già gestito dal framework)
        // 3. VisualizzaProfiliCtrl crea CampiFiltriForm
        Router.mostraCampiFiltriForm(event);
    }

    // 5. L'utente cliccaFiltra() dentro CampiFiltriForm
    @FXML
    void cliccaFiltra(ActionEvent event) {
        // 4. L'utente inserisciFiltri() dentro CampiFiltriForm
        String carriera = carrieraFiltroField.getText();
        String anniTesto = anniCarrieraFiltroField.getText();
        int anniDiCarriera = 0;

        try {
            if (anniTesto != null && !anniTesto.trim().isEmpty()) {
                anniDiCarriera = Integer.parseInt(anniTesto.trim());
            }
        } catch (NumberFormatException ex) {
            ErrorText error = new ErrorText("Inserisci un numero valido per gli anni.");
            error.okay();
            return;
        }

        // 6. CampiFiltriForm fa il passaDatiFiltri() al VisualizzaProfiliCtrl
        passaDatiFiltri(event, carriera, anniDiCarriera);
    }

    private void passaDatiFiltri(ActionEvent event, String carriera, int anniDiCarriera) {
        ResultSet rs = null;
        try {
            // 7. VisualizzaProfiliCtrl fa la queryDBMSFiltraArtisti() alla DBMSBoundary
            rs = DBMSboundary.getInstance().queryDBMSFiltraArtisti(carriera, anniDiCarriera);

            risultatiRicercaTemporanei.clear();
            boolean artistaTrovato = false;

            if (rs != null) {
                while (rs.next()) {
                    artistaTrovato = true;
                    // Estraiamo i campi anche dalla tabella carriera grazie alla JOIN
                    String cf = rs.getString("codiceFiscale");
                    String nomeDarte = rs.getString("nomeDarte");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String tipologia = rs.getString("tipologia");
                    int anni = rs.getInt("anni");

                    // Formatta la stringa per mostrare anche la professione
                    risultatiRicercaTemporanei.add(new String[]{cf, "🎭 " + nomeDarte + " - (" + nome + " " + cognome + ") | " + tipologia + " (" + anni + " anni)"});
                }
            }

            if (!artistaTrovato) {
                ErrorText errorText = new ErrorText("Nessun artista trovato con questi filtri.");
                errorText.okay();
                // Potremmo voler restare nel form, l'utente proverà altri filtri
            } else {
                // 8. VisualizzaProfiliCtrl crea una ListaArtistiView popolata dal risultato del filtraggio.
                Router.mostraListaArtistiView(event);
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
    // SEQUENCE: Visualizza profili - Visualizza
    // ==========================================

    // 1. L'utente CliccaVisualizza() sulla ListaArtistiView()
    @FXML
    void cliccaVisualizza(ActionEvent event) {
        // Estraiamo il Codice Fiscale "nascosto" nel bottone che è stato premuto
        Button btnPremuto = (Button) event.getSource();
        String codiceFiscaleDaCercare = (String) btnPremuto.getUserData();

        // 2. ListaArtistaView crea VisualizzaProfiliCtrl (Già fatto dal JavaFX)

        ResultSet rs = null;
        try {
            // 3. VisualizzaProfiloCtrl fa una queryDBMSProfiloArtista
            rs = DBMSboundary.getInstance().queryDBMSProfiloArtista(codiceFiscaleDaCercare);

            if (rs != null && rs.next()) {
                // Raccogliamo tutti i dati per popolare la ProfiloView
                String cf = rs.getString("codiceFiscale");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String sesso = rs.getString("sesso");
                String nomeDarte = rs.getString("nomeDarte");
                String email = rs.getString("email");
                String urlImmagine = rs.getString("urlImmagineProfilo");

                // Memorizziamo l'entità per fargliela leggere nella ProfiloView
                artistaDaVisualizzare = new Artista(cf, nome, cognome, null, sesso, nomeDarte, email, null, urlImmagine);

                // 4. VisualizzaProfiliCtrl crea ProfiloView.
                Router.mostraProfiloView(event);
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
    void mostraCercaArtistaView(ActionEvent event) {
        Router.mostraCercaArtistaView(event);
    }

    // Metodo di servizio per tornare indietro
    @FXML
    void tornaAlLogin(ActionEvent event) {
        Router.mostraAuthView(event);
    }

    /**
     * Metodo privato di servizio per chiamare l'OS e fargli aprire il file.
     */
    private void apriDocumentoConOS(String percorsoAssoluto) {
        try {
            File fileDaAprire = new File(percorsoAssoluto);
            if (fileDaAprire.exists()) {
                // Sfrutta la classe Desktop nativa di Java per delegare l'apertura a Windows/Mac
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileDaAprire);
                } else {
                    System.out.println("L'apertura nativa dei file non è supportata su questo sistema.");
                }
            } else {
                new ErrorText("Il file non è più presente nel percorso specificato.").okay();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Impossibile aprire il file. Controlla i permessi o i programmi predefiniti.").okay();
        }
    }
}