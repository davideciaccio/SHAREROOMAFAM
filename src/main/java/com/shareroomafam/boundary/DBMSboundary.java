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
}