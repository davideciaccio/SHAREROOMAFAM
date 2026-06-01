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

    public static void mostraGestioneDatiPersonaliView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/GestioneDatiPersonaliView.fxml", "ShareRoom AFAM - Gestione Dati Personali");
    }

    public static void mostraModificaPasswordForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaPasswordForm.fxml", "ShareRoom AFAM - Cambia Password");
    }

    public static void mostraModificaNomeArteForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaNomeArteForm.fxml", "ShareRoom AFAM - Modifica Nome d'Arte");
    }

    public static void mostraModificaCarrieraView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaCarrieraView.fxml", "ShareRoom AFAM - Modifica Carriera");
    }

    public static void mostraInserisciDatiCarrieraForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/InserisciDatiCarrieraForm.fxml", "ShareRoom AFAM - Aggiungi Carriera");
    }

    public static void mostraListaCarriereView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ListaCarriereView.fxml", "ShareRoom AFAM - Rimuovi Carriere");
    }

    public static void mostraModificaImmagineProfiloView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaImmagineProfiloView.fxml", "ShareRoom AFAM - Modifica Immagine Profilo");
    }

    public static void mostraGestisciDocumentiView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/GestisciDocumentiView.fxml", "ShareRoom AFAM - Gestisci Documenti");
    }

    public static void mostraDocumentiAggiuntiChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiAggiuntiChecklist.fxml", "ShareRoom AFAM - Checklist Documenti");
    }

    public static void mostraEliminaDocumentiChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/EliminaDocumentiChecklist.fxml", "ShareRoom AFAM - Elimina Documenti");
    }

    public static void mostraDocumentiCaricatiChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiCaricatiChecklist.fxml", "ShareRoom AFAM - Cambia Stato Documenti");
    }

    public static void mostraGestioneStanzeView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/GestioneStanzeView.fxml", "ShareRoom AFAM - Gestione Stanze");
    }
    public static void mostraInserisciNomeStanzaForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/InserisciNomeStanzaForm.fxml", "ShareRoom AFAM - Crea Stanza");
    }
    public static void mostraDocumentiChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiChecklist.fxml", "ShareRoom AFAM - Seleziona Documenti per Stanza");
    }
    public static void mostraDocumentiScaricabiliChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiScaricabiliChecklist.fxml", "ShareRoom AFAM - Permessi Download");
    }

    public static void mostraFinestraCopiaLinkView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/FinestraCopiaLinkView.fxml", "ShareRoom AFAM - Condividi Stanza");
    }

    public static void mostraListaVisualizzatoriView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ListaVisualizzatoriView.fxml", "ShareRoom AFAM - Lista Visualizzatori");
    }

    public static void mostraModificaStanzaView(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaStanzaView.fxml", "ShareRoom AFAM - Modifica Stanza");
    }

    public static void mostraModificaNomeForm(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaNomeForm.fxml", "ShareRoom AFAM - Modifica Nome Stanza");
    }

    public static void mostraDocumentiDaInserireChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiDaInserireChecklist.fxml", "ShareRoom AFAM - Aggiungi Documenti alla Stanza");
    }

    public static void mostraModificaDocumentiScaricabiliChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/ModificaDocumentiScaricabiliChecklist.fxml", "ShareRoom AFAM - Permessi Download Nuovi Documenti");
    }

    public static void mostraDocumentiDaRimuovereChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiDaRimuovereChecklist.fxml", "ShareRoom AFAM - Rimuovi Documenti");
    }

    public static void mostraDocumentiScaricabiliENonChecklist(ActionEvent event) {
        cambiaScena(event, "/com/shareroomafam/view/DocumentiScaricabiliENonChecklist.fxml", "ShareRoom AFAM - Permessi Download Documenti");
    }
}