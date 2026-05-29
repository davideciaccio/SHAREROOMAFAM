package com.shareroomafam.control;

import com.shareroomafam.boundary.DBMSboundary;
import com.shareroomafam.entity.Artista;
import com.shareroomafam.textmessage.ErrorText;
import com.shareroomafam.textmessage.SuccessfulText;
import com.shareroomafam.utility.EmailSender;
import com.shareroomafam.utility.Router;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Random;

public class AuthCtrl {

    // --- VARIABILE DI STATO GLOBALE PER IL LOGIN 2FA ---
    private static String emailInAttesaDiVerifica;

    // ==========================================
    // CAMPI FXML
    // ==========================================

    // Campi AuthView (Login)
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Campi InserisciCodiceForm (2FA)
    @FXML private TextField codiceField;

    // Campi DatiAnagraficiForm (Registrazione estratto dal punto 3 del flusso)
    @FXML private TextField nomeField;
    @FXML private TextField cognomeField;
    @FXML private DatePicker dataNascitaPicker;
    @FXML private TextField sessoField; // Niente ComboBox, uso testo semplice
    @FXML private TextField cfField;
    @FXML private TextField nomeDarteField;
    @FXML private TextField carrieraField;
    @FXML private TextField anniCarrieraField;
    @FXML private TextField emailRegField; // Differenziato dal login
    @FXML private PasswordField passwordRegField; // Differenziato dal login


    // ==========================================
    // SEQUENCE: ESEGUI ACCESSO (LOGIN & 2FA)
    // ==========================================

    // 3. L'artista cliccaAccedi()
    @FXML
    void cliccaAccedi(ActionEvent event) {
        // 1. L'artista inserisce Email
        String email = emailField.getText();

        // 2. L'artista inserisce password
        String password = passwordField.getText();

        // 3.1 AuthView Crea un AuthCtrl e chiama il metodo passadati(email, password)
        passadati(event, email, password);
    }

    private void passadati(ActionEvent event, String email, String password) {
        ResultSet rs = null;
        try {
            // 3.2.1 L'AuthCtrl fa una queryDBMSverificaCredenziali() alla DBMSBoundary
            rs = DBMSboundary.getInstance().queryDBMSverificaCredenziali(email, password);

            // 4. IF Credenziali sono corrette
            if (rs != null && rs.next()) {

                emailInAttesaDiVerifica = email; // Salvataggio stato per il passaggio 2FA

                // 4.1 AuthCtrl chiama la funzione privata creaCodiceVerifica()
                String codiceVerifica = creaCodiceVerifica();
                System.out.println("DEBUG CODICE 2FA: " + codiceVerifica); // Utile per i test

                // 4.2. AuthCtrl fa la insertDBMScodice() alla DBMSBoundary
                DBMSboundary.getInstance().insertDBMScodice(email, codiceVerifica);

                // 4.3. AuthCtrl InviaEmail(codice di verifica)
                InviaEmail(email, codiceVerifica);

                // 4.4. AuthCtrl crea InserisciCodiceForm
                Router.mostraInserisciCodiceForm(event);

            } else {
                // 5. ELSE credenziali non sono corrette, ovvero else del punto 4
                // 5.1 AuthCtrl crea un errorText, l'artista cliccaOkay(), segue destroy errorText
                ErrorText errorText = new ErrorText("Credenziali errate");
                errorText.okay(); // Il destroy è gestito all'interno della classe ErrorText

                // 5.2 AuthCtrl mostrauAuthView()
                mostraAuthView(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // FIX DEFINITIVO: Chiusura ultra-sicura e silenziosa
            try {
                if (rs != null) {
                    java.sql.Statement stmt = rs.getStatement();
                    if (stmt != null) stmt.close();
                }
            } catch (Exception ignore) {
                // MySQL ha già chiuso il ResultSet in automatico, ignoriamo per non sporcare la console
            }
        }
    }

    private String creaCodiceVerifica() {
        // Funzione privata per generare il codice a 6 cifre
        return String.format("%06d", new Random().nextInt(999999));
    }

    private void InviaEmail(String email, String codice) {
        // Delega l'invio all'utility creata appositamente
        EmailSender.inviaCodice2FA(email, codice);
    }

    // 4.5 L'artista cliccaInvia() dentro InserisciCodiceForm
    @FXML
    void cliccaInvia(ActionEvent event) {
        // 4.5 L'artista inserisceCodice() dentro InserisciCodiceForm
        String codiceInserito = codiceField.getText();

        // 4.6 InserisciCodiceForm fa il passaDati ad AuthCtrl
        passaDati(event, codiceInserito);
    }

    private void passaDati(ActionEvent event, String codice) {
        ResultSet rs = null;
        try {
            // 4.7 AuthCtrl fa una query alla DBMS Boundary chiamata queryDBMSVerificaCodice()
            rs = DBMSboundary.getInstance().queryDBMSVerificaCodice(emailInAttesaDiVerifica, codice);

            if (rs != null && rs.next()) {
                // 4.8 ELSE codice corretto
                // AuthCtrl prende i dati dell'entityartista
                String cf = rs.getString("codiceFiscale");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String sesso = rs.getString("sesso");
                String nomeDarte = rs.getString("nomeDarte");
                String email = rs.getString("email");
                String password = rs.getString("password");

                Artista artista = new Artista(cf, nome, cognome, null, sesso, nomeDarte, email, password, null);
                System.out.println("Accesso completato per l'artista: " + artista.getNome() + " " + artista.getCognome());

                // e mostraHomePageArtistaView()
                mostraHomePageArtistaView(event);

                // Pulizia token per sicurezza dopo l'accesso
                DBMSboundary.getInstance().insertDBMScodice(emailInAttesaDiVerifica, null);
                emailInAttesaDiVerifica = null;

            } else {
                // 4.7.1 IF codice inserito errato
                // AuthCtrl crea ErrorText -> l'artista clicca ok -> segue destroy errorText
                ErrorText errorText = new ErrorText("Codice errato");
                errorText.okay();

                // 4.7.2 Authctrl mostraAuthView()
                mostraAuthView(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // FIX DEFINITIVO: Chiusura ultra-sicura e silenziosa
            try {
                if (rs != null) {
                    java.sql.Statement stmt = rs.getStatement();
                    if (stmt != null) stmt.close();
                }
            } catch (Exception ignore) {
                // MySQL ha già chiuso il ResultSet in automatico, ignoriamo per non sporcare la console
            }
        }
    }


    // ==========================================
    // SEQUENCE: REGISTRAZIONE
    // ==========================================

    // 1. L'utente clicca il pulsante Registrati su AuthView
    @FXML
    void apriRegistrazione(ActionEvent event) {
        try {
            // 1.1.1 AUTH CTRL CREA DATIANAGRAFICI FORM
            // (La logica fisica di creazione della vista è gestita dal Router globale)
            com.shareroomafam.utility.Router.mostraRegistrazione(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3. L'utente cliccConferma() in DatiAnagraficiForm
    @FXML
    void conferma(ActionEvent event) {
        // 3.1 Dati anagrafici form fa il passa dati all'authctrl
        String nome = nomeField.getText();
        String cognome = cognomeField.getText();
        LocalDate dataNascita = dataNascitaPicker.getValue();
        String sesso = sessoField.getText();
        String cf = cfField.getText();
        String nomeDarte = nomeDarteField.getText();
        String carriera = carrieraField.getText();
        int anniCarriera = Integer.parseInt(anniCarrieraField.getText());
        String email = emailRegField.getText();
        String password = passwordRegField.getText();

        try {
            // 4. AuthCtrl fa una query alla DBMSBoundary chiamata QueryDBMSVerificaRegistrazione
            ResultSet rs = DBMSboundary.getInstance().QueryDBMSVerificaRegistrazione(cf, email);
            boolean esisteGia = false;
            if (rs != null && rs.next()) {
                if (rs.getInt(1) > 0) esisteGia = true;
                rs.getStatement().close();
            }

            // 5. IF CODICE FISCALE IN USO O EMAIL IN USO O PASSWORD NON RISPETTA I CRITERI DI SICUREZZA
            boolean passwordNonSicura = password.length() < 8;

            if (esisteGia || passwordNonSicura) {
                // 5.1 AuthCtrl Create Error Text
                ErrorText errorText = new ErrorText("Registrazione fallita");
                // 5.2 L'utente clicca okay() segue destroy di errortext
                errorText.okay();
                // 6.3 Il sistema mostra a video il form “Dati anagrafici” (è già a video, quindi non fa nulla)

            } else {
                // 6. ELSE
                // 6.2 Viene creata un Artista entity
                Artista artista = new Artista(cf, nome, cognome, null, sesso, nomeDarte, email, password, null);

                // 6.3 Vengono settati i dati all'artista entity grazie al metodo SetDati
                artista.setDati(cf, nome, cognome, dataNascita.atStartOfDay(), sesso, nomeDarte, email, password, null);

                // 6.1 Authctrl invoca il metodo InsertDBMSCreaProfilo() alla DBMS Boundary
                DBMSboundary.getInstance().InsertDBMSCreaProfilo(cf, nome, cognome, java.sql.Date.valueOf(dataNascita), sesso, nomeDarte, email, password, carriera, anniCarriera);

                // 6.4 AuthControl genera un Successful text()
                SuccessfulText successText = new SuccessfulText("Registrazione effettuata correttamente");

                // 6.5 l'utente clicca okay() segue destroy
                successText.okay();

                // 7.4 Il sistema mostra a video la schermata di “Autenticazione”.
                com.shareroomafam.utility.Router.mostraAuthView(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ==========================================
    // METODI GLOBALI / UTILITY / STUB
    // ==========================================

    @FXML
    void mostraAuthView(ActionEvent event) {
        com.shareroomafam.utility.Router.mostraAuthView(event);
    }

    void mostraHomePageArtistaView(ActionEvent event) {
        com.shareroomafam.utility.Router.mostraHomePageArtistaView(event);
    }

    // Metodi vuoti per i pulsanti secondari non ancora implementati della AuthView
    @FXML void handleSpidLogin(ActionEvent event) {}
    @FXML void apriRecuperoPassword(ActionEvent event) {}
    @FXML void apriVisualizzaProfili(ActionEvent event) {}
}