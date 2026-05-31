package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Documento;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GestisciDocumentiCtrl {

    // ListView che ospiterà i documenti da impostare (in DocumentiAggiuntiChecklist)
    @FXML private ListView<HBox> documentiChecklistView;

    // Lista statica per passare i file estratti dal FileSystem alla nuova finestra Checklist
    private static List<File> fileSelezionatiTemporanei = new ArrayList<>();

    @FXML
    public void initialize() {
        // Popoliamo la Checklist (Sequence passaggi 5 e 6: L'utente spunta/toglie la spunta)
        if (documentiChecklistView != null && !fileSelezionatiTemporanei.isEmpty()) {
            documentiChecklistView.getItems().clear();

            for (File file : fileSelezionatiTemporanei) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                // CheckBox con il nome del file. Selezionato = Visibile, Deselezionato = Privato.
                CheckBox checkBox = new CheckBox();
                checkBox.setSelected(true); // Default visibile, l'artista può deselezionarlo

                // Salviamo il percorso assoluto nel tag nascosto UserData per poterlo passare al DB
                checkBox.setUserData(file.getAbsolutePath());

                Label lblTesto = new Label(file.getName());

                row.getChildren().addAll(checkBox, lblTesto);
                documentiChecklistView.getItems().add(row);
            }
        }
    }


    // ==========================================
    // SEQUENCE: Gestione profilo – Gestisci documenti – Carica documenti
    // ==========================================

    // 1. L'artista cliccaAggiungiDocumenti() dentro GestisciDocumentiView
    @FXML
    void cliccaAggiungiDocumenti(ActionEvent event) {
        // 2. GestisciDocumentiVIew crea GestisciDocumentiCtrl (Gestito da JavaFX)

        if (GestioneProfiloCtrl.artistaLoggato == null) {
            new ErrorText("Errore di sessione. Riprova ad accedere.").okay();
            Router.mostraAuthView(event);
            return;
        }

        // 3. GestisciDocumentiCtrl invoca il metodo recuperaDocumenti()
        List<File> fileScelti = recuperaDocumenti(event);

        if (fileScelti != null && !fileScelti.isEmpty()) {
            fileSelezionatiTemporanei = new ArrayList<>(fileScelti);

            // 4. GestisciDocumentiCtrl crea una DocumentiAggiuntiChecklist.
            Router.mostraDocumentiAggiuntiChecklist(event);
        }
    }

    /**
     * Interagisce con il file system del sistema operativo per prelevare più file.
     */
    private List<File> recuperaDocumenti(ActionEvent event) {
        Window stage = ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona i Documenti da caricare");

        // Consentiamo la selezione multipla di file
        return fileChooser.showOpenMultipleDialog(stage);
    }


    // 7. L'artista cliccaConferma() su DocumentiAggiuntiChecklist
    @FXML
    void cliccaConferma(ActionEvent event) {

        // Raccogliamo i dati inseriti dall'utente (Passaggi 5 e 6 impliciti nella GUI)
        List<DocumentoSetup> datiDaPassare = new ArrayList<>();

        for (HBox row : documentiChecklistView.getItems()) {
            CheckBox cb = (CheckBox) row.getChildren().get(0);

            boolean visibile = cb.isSelected(); // Spuntato = visibile, non spuntato = privato
            String percorso = (String) cb.getUserData();

            datiDaPassare.add(new DocumentoSetup(visibile, percorso));
        }

        // 8. DocumentiAggiuntiChecklist fa il passaDati() alla GestisciDocumentiCtrl
        passaDati(event, datiDaPassare);
    }

    private void passaDati(ActionEvent event, List<DocumentoSetup> documenti) {
        try {
            String cf = GestioneProfiloCtrl.artistaLoggato.getCodiceFiscale();

            for (DocumentoSetup docSetup : documenti) {
                // 9. GestisciDocumentiCtrl fa una queryDBMSInsertDocumenti() dove inserisce i nuovi documenti
                DBMSboundary.getInstance().queryDBMSInsertDocumenti(cf, docSetup.visibile, docSetup.percorso);

                // 10. GestisciDocumentiCtrl crea una entity Documento (ID temporaneo 0)
                Documento nuovoDoc = new Documento(0, cf, false, "");

                // 11. GestisciDocumentiCtrl fa una setDatiDocumenti() sulla entity documento
                // Sfruttiamo i setter per allinearci al requisito di caricamento dati
                nuovoDoc.setVisibile(docSetup.visibile);
                nuovoDoc.setPercorso(docSetup.percorso);
            }

            // Puliamo la memoria temporanea
            fileSelezionatiTemporanei.clear();

            // 12. GestisciDocumentiCtrl crea Successfulltext, l'artista cliccaokay()
            SuccessfulText successText = new SuccessfulText("Documenti aggiunti correttamente!");
            successText.okay();

            // 13. GestisciDocumentiCtrl invoca il metodo mostraGestisciDocumentiView.
            mostraGestisciDocumentiView(event);

        } catch (Exception e) {
            e.printStackTrace();
            new ErrorText("Errore durante il salvataggio dei documenti nel database.").okay();
        }
    }


    // ==========================================
    // METODI GLOBALI / STUB
    // ==========================================

    @FXML
    void mostraGestisciDocumentiView(ActionEvent event) {
        Router.mostraGestisciDocumentiView(event);
    }

    @FXML
    void tornaAGestioneProfilo(ActionEvent event) {
        Router.mostraGestioneProfiloView(event);
    }

    // Stub pronti per i futuri Sequence Diagram del RAD
    @FXML void cliccaEliminaDocumenti(ActionEvent event) {}
    @FXML void cliccaCambiaStatoDocumenti(ActionEvent event) {}

    // ==========================================
    // CLASSE DI SUPPORTO INTERNA
    // ==========================================
    private static class DocumentoSetup {
        boolean visibile;
        String percorso;
        DocumentoSetup(boolean visibile, String percorso) {
            this.visibile = visibile;
            this.percorso = percorso;
        }
    }
}