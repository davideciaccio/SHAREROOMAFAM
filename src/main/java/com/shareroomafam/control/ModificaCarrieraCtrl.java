package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Carriera;
import com.shareroomafam.textmessage.ConfirmText;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ModificaCarrieraCtrl {

    // Caselle di testo del InserisciDatiCarrieraForm
    @FXML private TextField carrieraField;
    @FXML private TextField anniCarrieraField;

    // ListView per ListaCarriereView (Rimuovi Carriera)
    @FXML private ListView<HBox> carriereListView;

    // Lista in memoria per passare i dati estratti dal DB alla schermata ListaCarriereView
    private static List<Carriera> carriereTemporanee = new ArrayList<>();

    @FXML
    public void initialize() {
        // 5. ModificaCarrieraCtrl crea una nuova view, ListaCarriereView, questa view è popolata con tutte le carriere da rimuovere.
        if (carriereListView != null) {
            carriereListView.getItems().clear();

            for (Carriera c : carriereTemporanee) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                // Inseriamo un CheckBox per rispettare il punto 3 del sequence (L'artista seleziona le carriere)
                CheckBox checkBox = new CheckBox(c.getTipologia() + " (" + c.getAnni() + " anni)");

                // Nascondiamo l'ID della carriera nel checkbox per recuperarlo dopo
                checkBox.setUserData(c.getIdCarriera());

                row.getChildren().add(checkBox);
                carriereListView.getItems().add(row);
            }
        }
    }

    // ==========================================
    // SEQUENCE: Gestione profilo – Gestione dati personali - Modifica carriera – Aggiungi carriera
    // ==========================================

    // 1. L'artista cliccaAggiungiCarriera() dentro ModificaCarrieraView
    @FXML
    void cliccaAggiungiCarriera(ActionEvent event) {
        // 2. ModificaCarrieraView crea ModificaCarrieraCtrl (Gestito da JavaFX)

        // 3. ModificaCarrieraCtrl crea InserisciDatiCarrieraForm
        Router.mostraInserisciDatiCarrieraForm(event);
    }


    // 6. L'artista cliccaSalvaModifiche() dentro InserisciDatiCarrieraForm
    @FXML
    void cliccaSalvaModifiche(ActionEvent event) {
        // 4. L'artista inserisciCarriera() dentro InserisciDatiCarrieraForm
        String tipologiaCarriera = carrieraField.getText();

        // 5. L'artista inserisciAnniDiCarriera() dentro InserisciDatiCarrieraForm
        String anniCarrieraStr = anniCarrieraField.getText();

        int anniDiCarriera = 0;

        // Validazione dei campi
        if (tipologiaCarriera == null || tipologiaCarriera.trim().isEmpty()) {
            new ErrorText("La tipologia della carriera non può essere vuota.").okay();
            return;
        }

        try {
            anniDiCarriera = Integer.parseInt(anniCarrieraStr.trim());
        } catch (NumberFormatException e) {
            new ErrorText("Inserisci un numero valido per gli anni di carriera.").okay();
            return;
        }

        // 7. InserisciDatiCarrieraForm fa passaDati() a ModificaCarrieraCtrl
        passaDati(event, tipologiaCarriera, anniDiCarriera);
    }


    private void passaDati(ActionEvent event, String tipologia, int anni) {
        try {
            // Controllo della sessione loggata
            if (GestioneProfiloCtrl.artistaLoggato == null) {
                new ErrorText("Errore di sessione. Riprova ad accedere.").okay();
                Router.mostraAuthView(event);
                return;
            }

            String cfArtista = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 8. ModificaCarrieraCtrl crea una nuova Carriera Entity
            // Usiamo la tua Entity esistente. Passiamo 0 come ID temporaneo (ci pensa il DB) e stringhe vuote.
            Carriera nuovaCarriera = new Carriera(0, cfArtista, "", 0);

            // 9. ModificaCarrieraCtrl fa la setDati sulla nuova Carriera Entity
            // Sfruttiamo i metodi setter nativi della tua Entity per impostare i dati
            nuovaCarriera.setTipologia(tipologia);
            nuovaCarriera.setAnni(anni);

            // 10. ModificaCarrieraCtrl chiama un metodo insertDBMSCarriera() alla DBMSBoundary per inserire la nuova carriera dell'artista nel db.
            DBMSboundary.getInstance().insertDBMSCarriera(cfArtista, nuovaCarriera.getTipologia(), nuovaCarriera.getAnni());

            // 11. ModificaCarrieraCtrl crea Successful text, l'artista cliccaOkay, destroy.
            SuccessfulText successText = new SuccessfulText("Carriera aggiunta correttamente");
            successText.okay(); // Il destroy è implicito nel costrutto dell'alert

            // 12. ModificaCarrieraCtrl invoca il metodo mostraGestioneDatiPersonaliView
            Router.mostraGestioneDatiPersonaliView(event);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante il salvataggio nel database.").okay();
        }
    }


    // ==========================================
    // SEQUENCE: Gestione profilo - Gestione dati personali – Modifica carriera – Rimuovi carriera
    // ==========================================

    // 1. L'artista cliccaRimuoviCarriera() dentro ModificaCarrieraView
    @FXML
    void cliccaRimuoviCarriera(ActionEvent event) {
        // 2. ModificaCarrieraView crea ModificaCarrieraCtrl (Automatico)

        if (GestioneProfiloCtrl.artistaLoggato == null) {
            new ErrorText("Errore di sessione. Riprova ad accedere.").okay();
            Router.mostraAuthView(event);
            return;
        }

        ResultSet rs = null;
        try {
            // 3. ModificaCarrieraCtrl fa getCodiceFiscale() dall'ArtistaEntity
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            // 4. ModificaCarrieraCtrl fa una queryDBMSListaCarriere alla DBMSBoundary()
            rs = DBMSboundary.getInstance().queryDBMSListaCarriere(cf);

            carriereTemporanee.clear();
            if (rs != null) {
                while (rs.next()) {
                    int id = rs.getInt("idCarriera");
                    String tipo = rs.getString("tipologia");
                    int anni = rs.getInt("anni");

                    Carriera c = new Carriera(id, cf, tipo, anni);
                    carriereTemporanee.add(c);
                }
            }

            if (carriereTemporanee.isEmpty()) {
                new ErrorText("Non ci sono carriere da rimuovere.").okay();
                return;
            }

            // 5. ModificaCarrieraCtrl crea una nuova view, ListaCarriereView
            Router.mostraListaCarriereView(event);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.getStatement().close();
            } catch (Exception ignore) {}
        }
    }


    // 7. L'artista CliccaConferma() dentro listaCarriereVIew
    @FXML
    void cliccaConferma(ActionEvent event) {

        List<Integer> idDaRimuovere = new ArrayList<>();

        // 6. L'artista seleziona le carriere da rimuovere dentro ListaCarriereView
        // Estraiamo gli ID delle CheckBox che sono state spuntate
        for (HBox row : carriereListView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);
            if (cb.isSelected()) {
                idDaRimuovere.add((Integer) cb.getUserData());
            }
        }

        if (idDaRimuovere.isEmpty()) {
            new ErrorText("Seleziona almeno una carriera da rimuovere.").okay();
            return;
        }

        // 8. ModificaCarrieraCtrl crea un ConfirmText
        ConfirmText confirm = new ConfirmText("Sei sicuro di volere rimuovere queste carriere?");

        // L'artista cliccaSi().
        if (confirm.si()) {
            // 9. ListaCarriereView fa la passaDatiCarriere alla ModificaCarrieraCtrl
            passaDatiCarriere(event, idDaRimuovere);
        }
    }

    private void passaDatiCarriere(ActionEvent event, List<Integer> idDaRimuovere) {
        try {
            // 10. ModificaCarrieraCtrl fa una removeDBMSCarriereSelezionate() alla DBMSBoundary
            for (Integer id : idDaRimuovere) {
                DBMSboundary.getInstance().removeDBMSCarriereSelezionate(id);
            }

            // 11. Modifica CarrieraCtrl fa la destroy della/delle Entity Carriera.
            // Puliamo la lista delle entity carriere temporanee allocate in memoria
            carriereTemporanee.clear();
            System.out.println("🗑️ Entity Carriera rimosse dalla memoria locale.");

            // 12. ModificaCarrieraCtrl crea un successful text, L'artista cliccaOkay()
            SuccessfulText successText = new SuccessfulText("Carriere eliminate correttamente");
            successText.okay();

            // 13. ModificaCarrieraCtrl mostraGestioneDatiPersonaliView()
            Router.mostraGestioneDatiPersonaliView(event);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante l'eliminazione.").okay();
        }
    }


    // ==========================================
    // METODI GLOBALI / STUB
    // ==========================================

    @FXML
    void mostraModificaCarrieraView(ActionEvent event) {
        Router.mostraModificaCarrieraView(event);
    }

    @FXML
    void tornaAGestioneDatiPersonali(ActionEvent event) {
        Router.mostraGestioneDatiPersonaliView(event);
    }
}