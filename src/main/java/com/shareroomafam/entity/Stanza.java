package com.shareroomafam.entity;
// Classe stanza entity
public class Stanza {
    private int idStanza;
    private String codiceFiscaleArtista;
    private String nomeStanza;
    private String link;

    // Costruttore
    public Stanza(int idStanza, String codiceFiscaleArtista, String nomeStanza, String link) {
        this.idStanza = idStanza;
        this.codiceFiscaleArtista = codiceFiscaleArtista;
        this.nomeStanza = nomeStanza;
        this.link = link;
    }

    // Metodi get e set
    public int getIdStanza() { return idStanza; }
    public void setIdStanza(int idStanza) { this.idStanza = idStanza; }
    public String getCodiceFiscaleArtista() { return codiceFiscaleArtista; }
    public void setCodiceFiscaleArtista(String codiceFiscaleArtista) { this.codiceFiscaleArtista = codiceFiscaleArtista; }
    public String getNomeStanza() { return nomeStanza; }
    public void setNomeStanza(String nomeStanza) { this.nomeStanza = nomeStanza; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}