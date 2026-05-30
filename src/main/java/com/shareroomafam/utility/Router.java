package com.shareroomafam.utility;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Classe globale per la gestione della navigazione tra le schermate (Viste).
 * Qui andranno inseriti tutti i metodi per spostarsi nell'applicazione.
 */
public class Router {

    // Metodo base privato riutilizzabile che fa il lavoro sporco per tutti
    private static void cambiaScena(ActionEvent event, String fxmlPath, String titolo) {
        try {
            Parent root = FXMLLoader.load(Router.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titolo);
            stage.show();
        } catch (IOException e) {
            System.err.println("Errore di routing verso: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // ==========================================
    // METODI GLOBALI DI NAVIGAZIONE
    // ==========================================

    public static void mostraAuthView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/AuthView.fxml", "ShareRoom AFAM - Login");
    }

    public static void mostraRegistrazione(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DatiAnagraficiForm.fxml", "ShareRoom AFAM - Registrazione");
    }

    public static void mostraInserisciCodiceForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/InserisciCodiceForm.fxml", "ShareRoom AFAM - Verifica 2FA");
    }

    public static void mostraHomePageArtistaView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/HomePageArtistaView.fxml", "ShareRoom AFAM - Homepage");
    }

    public static void mostraAccessoConSpidMenuView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/AccessoConSpidMenuView.fxml", "ShareRoom AFAM - Scegli Provider SPID");
    }

    public static void mostraAccessoConSPIDForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/AccessoConSPIDForm.fxml", "ShareRoom AFAM - Login SPID");
    }

    public static void mostraRecuperaPasswordForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/RecuperaPasswordForm.fxml", "ShareRoom AFAM - Recupero Password");
    }

    public static void mostraInserisciCodiceVerificaForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/InserisciCodiceVerificaForm.fxml", "ShareRoom AFAM - Inserisci Codice");
    }

    public static void mostraPasswordView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/MostraPasswordView.fxml", "ShareRoom AFAM - La tua Password");
    }

    public static void mostraCercaArtistaView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/CercaArtistaView.fxml", "ShareRoom AFAM - Cerca Artista");
    }

    public static void mostraListaArtistiView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ListaArtistiView.fxml", "ShareRoom AFAM - Risultati Ricerca");
    }

    public static void mostraCampiFiltriForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/CampiFiltriForm.fxml", "ShareRoom AFAM - Filtra Artisti");
    }

    public static void mostraProfiloView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ProfiloView.fxml", "ShareRoom AFAM - Profilo Artista");
    }

    public static void mostraGestioneProfiloView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/GestioneProfiloView.fxml", "ShareRoom AFAM - Gestione Profilo");
    }
}