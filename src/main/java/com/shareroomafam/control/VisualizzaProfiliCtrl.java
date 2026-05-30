package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Utente;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class VisualizzaProfiliCtrl {

    // Campi FXML
    @FXML private TextField ricercaField;           // In CercaArtistaView
    @FXML private ListView<String> risultatiListView; // In ListaArtistiView

    // Variabili di stato
    private Utente utenteCorrente;

    // Lista statica per passare i dati dei risultati tra la schermata di ricerca e la schermata della lista
    private static List<String> risultatiRicercaTemporanei = new ArrayList<>();

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

        // Se ci troviamo nella ListaArtistiView, popoliamo la lista a schermo
        if (risultatiListView != null) {
            risultatiListView.getItems().addAll(risultatiRicercaTemporanei);
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
                    String nomeDarte = rs.getString("nomeDarte");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");

                    // Formatta la stringa da mostrare nella lista (es. "Michelangelo (Michelangelo Buonarroti)")
                    risultatiRicercaTemporanei.add("🎭 " + nomeDarte + " - (" + nome + " " + cognome + ")");
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

    // Metodo esplicito richiesto dal Sequence Diagram al punto 9.1
    @FXML
    void mostraCercaArtistaView(ActionEvent event) {
        Router.mostraCercaArtistaView(event);
    }

    // Metodo di servizio per tornare indietro
    @FXML
    void tornaAlLogin(ActionEvent event) {
        Router.mostraAuthView(event);
    }
}