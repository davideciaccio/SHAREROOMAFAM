package com.shareroomafam.boundary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBMSboundary {

    // Istanza unica del Singleton
    private static DBMSboundary instance = null;

    // Parametri di configurazione del Database
    private static final String URL = "jdbc:mysql://localhost:3306/ShareRoomAfam?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // Sostituisci con il tuo utente DB
    private static final String PASSWORD = ""; // Sostituisci con la tua password DB

    private Connection connection = null;

    // Costruttore privato per impedire l'istanziazione diretta (Pattern Singleton)
    private DBMSboundary() {
        try {
            // Carica esplicitamente il driver JDBC di MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Errore: Driver MySQL JDBC non trovato!");
            e.printStackTrace();
        }
    }

    // Metodo pubblico per ottenere l'unica istanza della classe
    public static synchronized DBMSboundary getInstance() {
        if (instance == null) {
            instance = new DBMSboundary();
        }
        return instance;
    }

    /**
     * Apre e restituisce una connessione attiva verso il DBMS.
     * Se una connessione è già aperta ed è valida, la riutilizza.
     */
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Chiude la connessione corrente se aperta.
     * Utile da chiamare alla chiusura dell'applicazione.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connessione al database chiusa con successo.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la chiusura della connessione.");
            e.printStackTrace();
        }
    }

    /**
     * Metodo generico per eseguire query di tipo SELECT (Lettura dati).
     * Gestisce i parametri tramite PreparedStatement per prevenire SQL Injection.
     * * NOTA: Chi restituisce il ResultSet deve assicurarsi di chiudere lo Statement e la Connessione,
     * oppure (consigliato nei Controller) estrarre subito i dati convertendoli in oggetti Entity.
     */
    public ResultSet queryDBMS(String sql, Object... params) throws SQLException {
        Connection conn = this.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }

        return pstmt.executeQuery();
    }

    /**
     * Metodo generico per eseguire query di tipo INSERT, UPDATE, DELETE (Modifica dati).
     * Garantisce la tracciabilità delle modifiche restituendo il numero di righe impattate.
     */
    public int insertDBMS(String sql, Object... params) throws SQLException {
        try (Connection conn = this.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }

            int rowsAffected = pstmt.executeUpdate();

            // Se serve recuperare una chiave auto-incrementata generata dal DB (es. idStanza, idDocumento)
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Restituisce l'ID generato se presente, altrimenti il numero di righe modificate
                        return generatedKeys.getInt(1);
                    }
                }
            }

            return rowsAffected;
        }
    }

    // -- METODI PER LA REGISTRAZIONE --

    public ResultSet QueryDBMSVerificaRegistrazione(String cf, String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ARTISTA WHERE codiceFiscale = ? OR email = ?";
        return queryDBMS(sql, cf, email);
    }

    public int InsertDBMSCreaProfilo(String cf, String nome, String cognome, java.sql.Date dataDiNascita, String sesso, String nomeDarte, String email, String password, String tipologiaCarriera, int anniCarriera) throws SQLException {
        // 1. Inserisce l'artista
        String sqlArtista = "INSERT INTO ARTISTA (codiceFiscale, nome, cognome, dataDiNascita, sesso, nomeDarte, email, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        insertDBMS(sqlArtista, cf, nome, cognome, dataDiNascita, sesso, nomeDarte, email, password);

        // 2. Inserisce la carriera collegata (Come richiesto dal flusso degli eventi punto 3)
        String sqlCarriera = "INSERT INTO CARRIERA (codiceFiscale_artista, tipologia, anni) VALUES (?, ?, ?)";
        return insertDBMS(sqlCarriera, cf, tipologiaCarriera, anniCarriera);
    }

    // --- METODI PER SEQUENCE ESEGUI ACCESSO ---

    public ResultSet queryDBMSverificaCredenziali(String email, String password) throws SQLException {
        String sql = "SELECT * FROM ARTISTA WHERE email = ? AND password = ?";
        return queryDBMS(sql, email, password);
    }

    public int insertDBMScodice(String email, String codice) throws SQLException {
        String sql = "UPDATE ARTISTA SET codiceVerifica = ? WHERE email = ?";
        return insertDBMS(sql, codice, email); // Il metodo genrico insertDBMS va bene anche per UPDATE
    }

    public ResultSet queryDBMSVerificaCodice(String email, String codice) throws SQLException {
        String sql = "SELECT * FROM ARTISTA WHERE email = ? AND codiceVerifica = ?";
        return queryDBMS(sql, email, codice);
    }

    // 10. AuthCtrl fa una queryDBMSVerificaEsistenzaAccount() alla DBMS boundary
    public ResultSet queryDBMSVerificaEsistenzaAccount(String email) throws SQLException {
        String sql = "SELECT * FROM ARTISTA WHERE email = ?";
        return queryDBMS(sql, email);
    }

    // --- METODI PER SEQUENCE RECUPERA PASSWORD ---

    public ResultSet queryDBMSVerificaEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ARTISTA WHERE email = ?";
        return queryDBMS(sql, email);
    }

    public int insertDBMScodiceVerifica(String email, String codice) throws SQLException {
        // Esegue esattamente la stessa operazione del 2FA
        String sql = "UPDATE ARTISTA SET codiceVerifica = ? WHERE email = ?";
        return insertDBMS(sql, codice, email);
    }

    public ResultSet queryDBMSRecuperaPassword(String email) throws SQLException {
        String sql = "SELECT password FROM ARTISTA WHERE email = ?";
        return queryDBMS(sql, email);
    }

    // --- METODI PER SEQUENCE VISUALIZZA PROFILI (Cerca Artista) ---

    public ResultSet queryDBMSCercaArtista(String keyword) throws SQLException {
        // Cerca corrispondenze nel nome d'arte, nel nome o nel cognome
        String sql = "SELECT * FROM ARTISTA WHERE nomeDarte LIKE ? OR nome LIKE ? OR cognome LIKE ?";
        String searchPattern = "%" + keyword + "%";
        return queryDBMS(sql, searchPattern, searchPattern, searchPattern);
    }

    // --- METODI PER SEQUENCE VISUALIZZA PROFILI (Filtra per dati artistici) ---

    public ResultSet queryDBMSFiltraArtisti(String carriera, int anniDiCarriera) throws SQLException {
        // Unisce la tabella ARTISTA e CARRIERA per trovare le corrispondenze (cerca anni di carriera maggiori o uguali al filtro)
        String sql = "SELECT A.*, C.tipologia, C.anni FROM ARTISTA A JOIN CARRIERA C ON A.codiceFiscale = C.codiceFiscale_artista WHERE C.tipologia LIKE ? AND C.anni >= ?";
        String paramCarriera = "%" + (carriera != null ? carriera : "") + "%";
        return queryDBMS(sql, paramCarriera, anniDiCarriera);
    }

    // --- METODI PER SEQUENCE VISUALIZZA PROFILI (Visualizza) ---
    public ResultSet queryDBMSProfiloArtista(String codiceFiscale) throws SQLException {
        String sql = "SELECT * FROM ARTISTA WHERE codiceFiscale = ?";
        return queryDBMS(sql, codiceFiscale);
    }

    // --- METODI PER SEQUENCE GESTIONE PROFILO (Cancella Profilo) ---

    public int removeDBMSProfiloArtista(String codiceFiscale) throws SQLException {
        // Grazie al ON DELETE CASCADE nel DB, questa singola query rimuove a cascata anche Carriera, Documenti e Stanze
        String sql = "DELETE FROM ARTISTA WHERE codiceFiscale = ?";
        return insertDBMS(sql, codiceFiscale); // Usiamo insertDBMS perché gestisce l'esecuzione di query di modifica (INSERT/UPDATE/DELETE)
    }

    // --- METODI PER SEQUENCE GESTIONE DATI PERSONALI (Cambia Password) ---

    public ResultSet queryDBMSVerificaPassword(String codiceFiscale, String passwordDaVerificare) throws SQLException {
        // Cerca l'artista solo se la password inserita coincide con quella attualmente salvata nel DB
        String sql = "SELECT * FROM ARTISTA WHERE codiceFiscale = ? AND password = ?";
        return queryDBMS(sql, codiceFiscale, passwordDaVerificare);
    }

    public int updateDBMSPassword(String codiceFiscale, String nuovaPassword) throws SQLException {
        String sql = "UPDATE ARTISTA SET password = ? WHERE codiceFiscale = ?";
        return insertDBMS(sql, nuovaPassword, codiceFiscale);
    }

    // --- METODI PER SEQUENCE GESTIONE DATI PERSONALI (Modifica Nome d'Arte) ---

    public ResultSet queryDBMSVerificaNomeArte(String nomeDarte) throws SQLException {
        // Cerca se esiste già un artista con questo specifico nome d'arte
        String sql = "SELECT * FROM ARTISTA WHERE nomeDarte = ?";
        return queryDBMS(sql, nomeDarte);
    }

    public int updateDBMSNomeArte(String codiceFiscale, String nuovoNomeArte) throws SQLException {
        // Aggiorna il nome d'arte dell'artista corrente
        String sql = "UPDATE ARTISTA SET nomeDarte = ? WHERE codiceFiscale = ?";
        return insertDBMS(sql, nuovoNomeArte, codiceFiscale);
    }

    // --- METODI PER SEQUENCE MODIFICA CARRIERA (Aggiungi Carriera) ---

    public int insertDBMSCarriera(String codiceFiscale, String tipologia, int anni) throws SQLException {
        String sql = "INSERT INTO CARRIERA (codiceFiscale_artista, tipologia, anni) VALUES (?, ?, ?)";
        return insertDBMS(sql, codiceFiscale, tipologia, anni);
    }

    // --- METODI PER SEQUENCE MODIFICA CARRIERA (Rimuovi Carriera) ---

    public ResultSet queryDBMSListaCarriere(String codiceFiscale) throws SQLException {
        // Estrae tutte le carriere dell'artista loggato
        String sql = "SELECT * FROM CARRIERA WHERE codiceFiscale_artista = ?";
        return queryDBMS(sql, codiceFiscale);
    }

    public int removeDBMSCarriereSelezionate(int idCarriera) throws SQLException {
        // Elimina la carriera specifica in base all'ID univoco
        String sql = "DELETE FROM CARRIERA WHERE idCarriera = ?";
        return insertDBMS(sql, idCarriera);
    }

    // --- METODI PER SEQUENCE MODIFICA IMMAGINE PROFILO ---

    public int queryDBMSUpdateImmagineProfilo(String codiceFiscale, String urlImmagine) throws SQLException {
        // Aggiorna l'URL/percorso dell'immagine per lo specifico artista
        String sql = "UPDATE ARTISTA SET urlImmagineProfilo = ? WHERE codiceFiscale = ?";
        return insertDBMS(sql, urlImmagine, codiceFiscale); // Usiamo insertDBMS perché fa l'UPDATE
    }

    // --- METODO PER SEQUENCE MODIFICA IMMAGINE (Rimuovi Immagine / Default) ---

    public int updateDBMSDefaultImageProfile(String codiceFiscale) throws SQLException {
        // Inserisce il percorso dell'immagine di default nella tupla dell'artista
        String defaultPath = "src/main/resources/default_profile.png";
        String sql = "UPDATE ARTISTA SET urlImmagineProfilo = ? WHERE codiceFiscale = ?";
        return insertDBMS(sql, defaultPath, codiceFiscale);
    }

    // --- METODI PER SEQUENCE GESTISCI DOCUMENTI (Carica Documenti) ---

    public int queryDBMSInsertDocumenti(String codiceFiscale, boolean visibile, String percorso) throws SQLException {
        // Inserisce il nuovo documento nel database con il suo stato di visibilità
        String sql = "INSERT INTO DOCUMENTO (codiceFiscale_artista, visibile, percorso) VALUES (?, ?, ?)";
        return insertDBMS(sql, codiceFiscale, visibile, percorso);
    }

    // --- METODI PER VISUALIZZAZIONE DOCUMENTI PUBBLICI ---

    public ResultSet queryDBMSDocumentiVisibili(String codiceFiscale) throws SQLException {
        // Estrae solo i documenti dell'artista impostati come "visibili" (visibile = 1 in MySQL)
        String sql = "SELECT * FROM DOCUMENTO WHERE codiceFiscale_artista = ? AND visibile = 1";
        return queryDBMS(sql, codiceFiscale);
    }

    // --- METODI PER SEQUENCE GESTISCI DOCUMENTI (Elimina Documenti) ---

    public ResultSet queryDBMSListaDocumenti(String codiceFiscale) throws SQLException {
        // Ritorna la lista di tutti i documenti associati all'artista
        String sql = "SELECT * FROM DOCUMENTO WHERE codiceFiscale_artista = ?";
        return queryDBMS(sql, codiceFiscale);
    }

    public int queryDBMSremoveDocument(int idDocumento) throws SQLException {
        // Rimuove fisicamente il documento dal database
        String sql = "DELETE FROM DOCUMENTO WHERE idDocumento = ?";
        return insertDBMS(sql, idDocumento);
    }

    // --- METODI PER SEQUENCE GESTISCI DOCUMENTI (Cambia stato documenti) ---

    public int queryDBMSUpdateStatoDocumenti(int idDocumento, boolean visibile) throws SQLException {
        // Aggiorna lo stato "visibile" (1 o 0) del documento nel DB
        String sql = "UPDATE DOCUMENTO SET visibile = ? WHERE idDocumento = ?";
        return insertDBMS(sql, visibile, idDocumento);
    }

    // --- METODI PER SEQUENCE GESTIONE STANZE (Crea Stanza) ---

    public ResultSet queryDBMSVerificaNomeStanza(String codiceFiscale, String nomeStanza) throws SQLException {
        // Controlla se l'artista ha già una stanza con quel preciso nome
        String sql = "SELECT * FROM STANZA WHERE codiceFiscale_artista = ? AND nomeStanza = ?";
        return queryDBMS(sql, codiceFiscale, nomeStanza);
    }

    public int insertDBMSStanza(String codiceFiscale, String nomeStanza, String link) throws SQLException {
        // Inserisce la stanza e restituisce l'ID generato in automatico dal DBMS
        String sql = "INSERT INTO STANZA (codiceFiscale_artista, nomeStanza, link) VALUES (?, ?, ?)";
        return insertDBMS(sql, codiceFiscale, nomeStanza, link);
    }

    public void insertDBMSContieneDocumento(int idStanza, int idDocumento, boolean scaricabile) throws SQLException {
        // Inserisce il record nella tabella di relazione CONTIENE
        String sql = "INSERT INTO CONTIENE (idStanza, idDocumento, scaricabile) VALUES (?, ?, ?)";
        insertDBMS(sql, idStanza, idDocumento, scaricabile);
    }

    public ResultSet queryDBMSListaStanze(String codiceFiscale) throws SQLException {
        String sql = "SELECT * FROM STANZA WHERE codiceFiscale_artista = ?";
        return queryDBMS(sql, codiceFiscale);
    }

    // --- METODI PER SEQUENCE GESTIONE STANZE (Condividi Stanza) ---

    public ResultSet queryDBMSLinkStanza(int idStanza) throws SQLException {
        // Recupera il link univoco associato all'ID della stanza
        String sql = "SELECT link FROM STANZA WHERE idStanza = ?";
        return queryDBMS(sql, idStanza);
    }

    // --- METODI PER SEQUENCE GESTIONE STANZE (Monitoraggio Stanza) ---

    public ResultSet queryDBMSListaVisualizzatori(int idStanza) throws SQLException {
        // Estrae i dati della visualizzazione uniti ai dati anagrafici del visualizzatore
        String sql = "SELECT V.dataVisualizzazione, U.nomeVisualizzatore, U.cognomeVisualizzatore, U.emailVisualizzatore " +
                "FROM VISUALIZZAZIONE V " +
                "JOIN VISUALIZZATORE U ON V.idVisualizzatore = U.idVisualizzatore " +
                "WHERE V.idStanza = ? " +
                "ORDER BY V.dataVisualizzazione DESC";
        return queryDBMS(sql, idStanza);
    }

    // --- METODI PER SEQUENCE GESTIONE STANZE (Elimina Stanza) ---

    public ResultSet updateDBMSStanza(int idStanza, String codiceFiscale) throws SQLException {
        // 1. Elimina la stanza
        String deleteSql = "DELETE FROM STANZA WHERE idStanza = ?";
        insertDBMS(deleteSql, idStanza);

        // 2. Ritorna la lista delle stanze aggiornata
        String selectSql = "SELECT * FROM STANZA WHERE codiceFiscale_artista = ?";
        return queryDBMS(selectSql, codiceFiscale);
    }

    // --- METODI PER SEQUENCE MODIFICA STANZA ---

    public int updateDBMSNomeStanza(int idStanza, String nuovoNome) throws SQLException {
        // Aggiorna il nome della stanza specificata
        String sql = "UPDATE STANZA SET nomeStanza = ? WHERE idStanza = ?";
        return insertDBMS(sql, nuovoNome, idStanza);
    }

    // --- METODI PER SEQUENCE MODIFICA STANZA (Aggiungi Documenti) ---

    public ResultSet queryDocumentiNonInStanza(String codiceFiscale, int idStanza) throws SQLException {
        // Estrae tutti i documenti dell'artista che NON sono ancora collegati alla stanza in CONTIENE
        String sql = "SELECT * FROM DOCUMENTO WHERE codiceFiscale_artista = ? AND idDocumento NOT IN (SELECT idDocumento FROM CONTIENE WHERE idStanza = ?)";
        return queryDBMS(sql, codiceFiscale, idStanza);
    }

    public void insertDocumentiStanzaDBMS(int idStanza, int idDocumento, boolean scaricabile) throws SQLException {
        // Inserisce i nuovi documenti selezionati nella tabella CONTIENE
        String sql = "INSERT INTO CONTIENE (idStanza, idDocumento, scaricabile) VALUES (?, ?, ?)";
        insertDBMS(sql, idStanza, idDocumento, scaricabile);
    }

    // --- METODI PER SEQUENCE MODIFICA STANZA (Rimuovi Documenti & Cambia Stato) ---

    public ResultSet queryDBMSListaDocumentiStanza(int idStanza) throws SQLException {
        // Estrae i documenti PRESENTI nella stanza e il loro stato "scaricabile" dalla tabella CONTIENE
        String sql = "SELECT D.*, C.scaricabile FROM DOCUMENTO D JOIN CONTIENE C ON D.idDocumento = C.idDocumento WHERE C.idStanza = ?";
        return queryDBMS(sql, idStanza);
    }

    public int queryDBMSRemoveDocumentiStanza(int idStanza, int idDocumento) throws SQLException {
        // Rimuove il legame tra la stanza e il documento dalla tabella CONTIENE
        String sql = "DELETE FROM CONTIENE WHERE idStanza = ? AND idDocumento = ?";
        return insertDBMS(sql, idStanza, idDocumento);
    }

    public int queryDBMSUpdateScaricabiliENonScaricabiliDocumentiStanza(int idStanza, int idDocumento, boolean scaricabile) throws SQLException {
        // Aggiorna lo stato "scaricabile" del documento all'interno della specifica stanza
        String sql = "UPDATE CONTIENE SET scaricabile = ? WHERE idStanza = ? AND idDocumento = ?";
        return insertDBMS(sql, scaricabile, idStanza, idDocumento);
    }

    // --- METODI PER IL WEB SERVER LOCALE ---

    public ResultSet queryDBMSStanzaByLinkIdentifier(String urlCode) throws SQLException {
        String fullLink = "http://localhost:8080/s/" + urlCode;
        String sql = "SELECT * FROM STANZA WHERE link = ?";
        return queryDBMS(sql, fullLink);
    }

    public ResultSet queryDBMSDocumentoById(int idDocumento) throws SQLException {
        String sql = "SELECT * FROM DOCUMENTO WHERE idDocumento = ?";
        return queryDBMS(sql, idDocumento);
    }

    // NUOVO: Salva il visitatore e restituisce l'ID generato automaticamente!
    public int insertDBMSVisualizzatore(String nome, String cognome, String email) throws SQLException {
        String sql = "INSERT INTO VISUALIZZATORE (nomeVisualizzatore, cognomeVisualizzatore, emailVisualizzatore) VALUES (?, ?, ?)";

        // Scriviamo il PreparedStatement a mano per assicurarci di recuperare l'ID (RETURN_GENERATED_KEYS)
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, email);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Ritorna l'ID autoincrementato!
                } else {
                    throw new SQLException("Creazione visualizzatore fallita, nessun ID ottenuto.");
                }
            }
        }
    }

    // MODIFICATO: Usa l'idVisualizzatore invece dell'IdUtente
    public void insertDBMSVisualizzazione(int idStanza, int idVisualizzatore) throws SQLException {
        String sql = "INSERT INTO VISUALIZZAZIONE (idStanza, idVisualizzatore, dataVisualizzazione) VALUES (?, ?, CURRENT_TIMESTAMP)";
        insertDBMS(sql, idStanza, idVisualizzatore);
    }
}