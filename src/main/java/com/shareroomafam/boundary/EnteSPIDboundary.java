package com.shareroomafam.boundary;

public class EnteSPIDboundary {
    private static EnteSPIDboundary instance = null;

    private EnteSPIDboundary() {}

    public static synchronized EnteSPIDboundary getInstance() {
        if (instance == null) {
            instance = new EnteSPIDboundary();
        }
        return instance;
    }

    // 9. AuthCtrl fa una queryVerificaEsistenzaAccountSPID() all'EnteSPIDboundary
    public boolean queryVerificaEsistenzaAccountSPID(String ente, String email, String password) {
        System.out.println("🌐 [SIMULAZIONE SPID] Contatto l'ente " + ente + " per l'email: " + email);
        // Simulazione: Se la password è "spid123", l'accesso è consentito, altrimenti fallisce.
        if (email != null && !email.isEmpty() && "spid123".equals(password)) {
            return true;
        }
        return false;
    }
}