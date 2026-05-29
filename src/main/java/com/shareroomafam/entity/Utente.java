package com.shareroomafam.entity;

//Classe utente entity
public class Utente {
    private int idUtente;

    // Costruttore
    public Utente(int idUtente) {
        this.idUtente = idUtente;
    }

    // Metodi get e set
    public int getIdUtente() { return idUtente; }
    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }
}