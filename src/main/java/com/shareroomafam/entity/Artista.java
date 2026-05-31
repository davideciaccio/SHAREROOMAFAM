package com.shareroomafam.entity;

import java.time.LocalDateTime;

//Classe artista entity
public class Artista {
    private String codiceFiscale;
    private String nome;
    private String cognome;
    private LocalDateTime dataDiNascita;
    private String sesso;
    private String nomeDarte;
    private String email;
    private String password;
    private String urlImmagineProfilo;

    // Costruttore
    public Artista(String codiceFiscale, String nome, String cognome, LocalDateTime dataDiNascita,
                   String sesso, String nomeDarte, String email, String password, String urlImmagineProfilo) {
        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.dataDiNascita = dataDiNascita;
        this.sesso = sesso;
        this.nomeDarte = nomeDarte;
        this.email = email;
        this.password = password;
        this.urlImmagineProfilo = urlImmagineProfilo;
    }

    // Metodi Get e Set
    public String getCodiceFiscale() { return codiceFiscale; }
    public void setCodiceFiscale(String codiceFiscale) { this.codiceFiscale = codiceFiscale; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public LocalDateTime getDataDiNascita() { return dataDiNascita; }
    public void setDataDiNascita(LocalDateTime dataDiNascita) { this.dataDiNascita = dataDiNascita; }
    public String getSesso() { return sesso; }
    public void setSesso(String sesso) { this.sesso = sesso; }
    public String getNomeDarte() { return nomeDarte; }
    public void setNomeDarte(String nomeDarte) { this.nomeDarte = nomeDarte; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getUrlImmagineProfilo() { return urlImmagineProfilo; }
    public void setUrlImmagineProfilo(String urlImmagineProfilo) { this.urlImmagineProfilo = urlImmagineProfilo; }

    //Metodo setDati
    public void setDati(String codiceFiscale, String nome, String cognome, java.time.LocalDateTime dataDiNascita, String sesso, String nomeDarte, String email, String password, String urlImmagineProfilo) {
        this.codiceFiscale = codiceFiscale;
        this.nome = nome;
        this.cognome = cognome;
        this.dataDiNascita = dataDiNascita;
        this.sesso = sesso;
        this.nomeDarte = nomeDarte;
        this.email = email;
        this.password = password;
        this.urlImmagineProfilo = urlImmagineProfilo;
    }

    // Metodo per impostare l'immagine di default
    public void setDefaultImageProfile() {
        this.urlImmagineProfilo = "src/main/resources/default_profile.png";
    }
}