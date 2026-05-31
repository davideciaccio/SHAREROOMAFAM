package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Carriera;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ModificaCarrieraCtrl {

    // Caselle di testo del InserisciDatiCarrieraForm
    @FXML private TextField carrieraField;
    @FXML private TextField anniCarrieraField;


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

    // Stub pronto per il Sequence Diagram "Rimuovi Carriera"
    @FXML void cliccaRimuoviCarriera(ActionEvent event) {}
}