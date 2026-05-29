package com.shareroomafam.entity;

//Classe carriera entity
public class Carriera {
    private int idCarriera;
    private String codiceFiscaleArtista;
    private String tipologia;
    private int anni;

    // Costruttore
    public Carriera(int idCarriera, String codiceFiscaleArtista, String tipologia, int anni) {
        this.idCarriera = idCarriera;
        this.codiceFiscaleArtista = codiceFiscaleArtista;
        this.tipologia = tipologia;
        this.anni = anni;
    }

    // Metodi get e set
    public int getIdCarriera() { return idCarriera; }
    public void setIdCarriera(int idCarriera) { this.idCarriera = idCarriera; }
    public String getCodiceFiscaleArtista() { return codiceFiscaleArtista; }
    public void setCodiceFiscaleArtista(String codiceFiscaleArtista) { this.codiceFiscaleArtista = codiceFiscaleArtista; }
    public String getTipologia() { return tipologia; }
    public void setTipologia(String tipologia) { this.tipologia = tipologia; }
    public int getAnni() { return anni; }
    public void setAnni(int anni) { this.anni = anni; }
}