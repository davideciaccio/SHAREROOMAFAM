CREATE DATABASE IF NOT EXISTS ShareRoomAfam;
USE ShareRoomAfam;

-- Tabella ARTISTA
CREATE TABLE ARTISTA (
                         codiceFiscale CHAR(16) NOT NULL PRIMARY KEY,
                         nome VARCHAR(255) NOT NULL,
                         cognome VARCHAR(255) NOT NULL,
                         dataDiNascita DATETIME NOT NULL,
                         sesso VARCHAR(255) NOT NULL,
                         nomeDarte VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         urlImmagineProfilo VARCHAR(2048) DEFAULT NULL,
                         codiceVerifica VARCHAR(10) DEFAULT NULL
);

-- Tabella CARRIERA (con chiave esterna su ARTISTA)
CREATE TABLE CARRIERA (
                          idCarriera INT AUTO_INCREMENT PRIMARY KEY,
                          codiceFiscale_artista CHAR(16) NOT NULL,
                          tipologia VARCHAR(255) NOT NULL,
                          anni INT NOT NULL,
                          FOREIGN KEY (codiceFiscale_artista) REFERENCES ARTISTA(codiceFiscale) ON DELETE CASCADE
);

-- Tabella UTENTE (per il caso d'uso dell'App "Visualizza Profili")
CREATE TABLE UTENTE (
                        idUtente INT NOT NULL PRIMARY KEY
);

-- Tabella STANZA
CREATE TABLE STANZA (
                        idStanza INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                        codiceFiscale_artista CHAR(16) NOT NULL,
                        nomeStanza VARCHAR(255) NOT NULL,
                        link VARCHAR(2048) NOT NULL,
                        FOREIGN KEY (codiceFiscale_artista) REFERENCES ARTISTA(codiceFiscale) ON DELETE CASCADE
);

-- Tabella DOCUMENTO
CREATE TABLE DOCUMENTO (
                           idDocumento INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
                           codiceFiscale_artista CHAR(16) NOT NULL,
                           visibile BOOLEAN NOT NULL,
                           percorso VARCHAR(2048) NOT NULL,
                           FOREIGN KEY (codiceFiscale_artista) REFERENCES ARTISTA(codiceFiscale) ON DELETE CASCADE
);

-- Tabella CONTIENE (Relazione molti-a-molti tra STANZA e DOCUMENTO)
CREATE TABLE CONTIENE (
                          scaricabile BOOLEAN NOT NULL,
                          idStanza INT NOT NULL,
                          idDocumento INT NOT NULL,
                          PRIMARY KEY (idStanza, idDocumento),
                          FOREIGN KEY (idStanza) REFERENCES STANZA(idStanza) ON DELETE CASCADE,
                          FOREIGN KEY (idDocumento) REFERENCES DOCUMENTO(idDocumento) ON DELETE CASCADE
);

-- Tabella VISUALIZZATORE (Per l'ospite esterno che accede dal Web Server)
CREATE TABLE VISUALIZZATORE (
                                idVisualizzatore INT AUTO_INCREMENT PRIMARY KEY,
                                nomeVisualizzatore VARCHAR(50) NOT NULL,
                                cognomeVisualizzatore VARCHAR(50) NOT NULL,
                                emailVisualizzatore VARCHAR(100) NOT NULL
);

-- Tabella VISUALIZZAZIONE (Collega la Stanza al Visualizzatore Web per il Monitoraggio)
CREATE TABLE VISUALIZZAZIONE (
                                 idVisualizzazione INT AUTO_INCREMENT PRIMARY KEY,
                                 idStanza INT NOT NULL,
                                 idVisualizzatore INT NOT NULL,
                                 dataVisualizzazione DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (idStanza) REFERENCES STANZA(idStanza) ON DELETE CASCADE,
                                 FOREIGN KEY (idVisualizzatore) REFERENCES VISUALIZZATORE(idVisualizzatore) ON DELETE CASCADE
);