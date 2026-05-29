package com.shareroomafam.entity;

// Classe documento entity
public class Documento {
    private int idDocumento;
    private String codiceFiscaleArtista;
    private boolean visibile;
    private String percorso;

    // Costruttore
    public Documento(int idDocumento, String codiceFiscaleArtista, boolean visibile, String percorso) {
        this.idDocumento = idDocumento;
        this.codiceFiscaleArtista = codiceFiscaleArtista;
        this.visibile = visibile;
        this.percorso = percorso;
    }

    // Metodi get e set
    public int getIdDocumento() { return idDocumento; }
    public void setIdDocumento(int idDocumento) { this.idDocumento = idDocumento; }
    public String getCodiceFiscaleArtista() { return codiceFiscaleArtista; }
    public void setCodiceFiscaleArtista(String codiceFiscaleArtista) { this.codiceFiscaleArtista = codiceFiscaleArtista; }
    public boolean isVisibile() { return visibile; }
    public void setVisibile(boolean visibile) { this.visibile = visibile; }
    public String getPercorso() { return percorso; }
    public void setPercorso(String percorso) { this.percorso = percorso; }
}