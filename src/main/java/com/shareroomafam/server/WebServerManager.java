package com.shareroomafam.server;

import com.shareroomafam.boundary.DBMSboundary;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.util.Random;

public class WebServerManager {

    private static HttpServer server;

    public static void startServer() {
        try {
            // Avvia il server sulla porta 8080 del tuo computer
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            // Definiamo le "Rotte" a cui il server risponde
            server.createContext("/s/", new StanzaHandler()); // Mostra l'HTML della Stanza
            server.createContext("/file", new FileHandler()); // Invia i byte dei documenti
            server.createContext("/style.css", new CssHandler()); // Invia il file CSS

            server.setExecutor(null); // Usa l'esecutore di default
            server.start();
            System.out.println("🌐 Web Server Locale avviato con successo sulla porta 8080!");

        } catch (IOException e) {
            System.err.println("❌ Errore nell'avvio del Web Server: " + e.getMessage());
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Web Server Locale fermato.");
        }
    }


    // =========================================
    // HANDLER 1: Generatore form e pagina HTML
    // =========================================
    static class StanzaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod(); // Capisce se è GET o POST
            String path = exchange.getRequestURI().getPath();
            String linkCode = path.substring(path.lastIndexOf('/') + 1);

            if ("GET".equalsIgnoreCase(method)) {
                // L'utente ha appena cliccato il link: Mostriamo il form
                serveFormHTML(exchange, linkCode);
            } else if ("POST".equalsIgnoreCase(method)) {
                // L'utente ha cliccato "Accedi" nel form: Elaboriamo i dati e mostriamo la stanza
                elaboraDatiEMostraStanza(exchange, linkCode);
            }
        }

        // --- SCHERMATA 1: IL FORM DI ACCESSO OBBLIGATORIO ---
        private void serveFormHTML(HttpExchange exchange, String linkCode) throws IOException {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html lang='it'><head><meta charset='UTF-8'>");
            html.append("<title>Accesso Stanza</title>");
            html.append("<link rel='stylesheet' href='/style.css'>");
            html.append("</head><body>");

            html.append("<div class='container'>");
            html.append("<header><h1>🔒 Accesso Riservato</h1></header>");

            // Creiamo un form accattivante che usa le nostre classi CSS
            html.append("<div class='card' style='max-width: 400px; margin: 0 auto;'>");
            html.append("<p style='text-align:center; color:var(--text-muted);'>Inserisci i tuoi dati per visualizzare i documenti di questa stanza.</p>");

            // Il form invierà i dati tramite POST allo STESSO link
            html.append("<form method='POST' action='/s/").append(linkCode).append("'>");

            html.append("<label style='font-weight:bold;'>Nome:</label><br>");
            html.append("<input type='text' name='nome' required style='width:95%; padding:10px; margin: 5px 0 15px 0; border: 1px solid #ccc; border-radius:5px;'><br>");

            html.append("<label style='font-weight:bold;'>Cognome:</label><br>");
            html.append("<input type='text' name='cognome' required style='width:95%; padding:10px; margin: 5px 0 15px 0; border: 1px solid #ccc; border-radius:5px;'><br>");

            html.append("<label style='font-weight:bold;'>Email:</label><br>");
            html.append("<input type='email' name='email' required style='width:95%; padding:10px; margin: 5px 0 20px 0; border: 1px solid #ccc; border-radius:5px;'><br>");

            html.append("<button type='submit' class='btn download-btn' style='width:100%; cursor:pointer; padding:15px; font-size:16px;'>Accedi alla Stanza</button>");
            html.append("</form>");
            html.append("</div></div></body></html>");

            inviaRisposta(exchange, html.toString(), 200);
        }

        // --- SCHERMATA 2: SALVATAGGIO DATI E MOSTRA STANZA ---
        private void elaboraDatiEMostraStanza(HttpExchange exchange, String linkCode) throws IOException {
            // 1. Estrazione dei dati inviati dal Form
            java.io.InputStreamReader isr = new java.io.InputStreamReader(exchange.getRequestBody(), "utf-8");
            java.io.BufferedReader br = new java.io.BufferedReader(isr);
            String formData = br.readLine();

            // Decodifica i parametri URL (es: nome=Mario&cognome=Rossi)
            java.util.Map<String, String> parametri = new java.util.HashMap<>();
            if (formData != null) {
                String[] coppie = formData.split("&");
                for (String coppia : coppie) {
                    String[] chiaveValore = coppia.split("=");
                    if (chiaveValore.length == 2) {
                        parametri.put(java.net.URLDecoder.decode(chiaveValore[0], "UTF-8"), java.net.URLDecoder.decode(chiaveValore[1], "UTF-8"));
                    }
                }
            }

            String nome = parametri.get("nome");
            String cognome = parametri.get("cognome");
            String email = parametri.get("email");

            StringBuilder htmlResponse = new StringBuilder();
            htmlResponse.append("<!DOCTYPE html><html lang='it'><head><meta charset='UTF-8'>");
            htmlResponse.append("<title>ShareRoom AFAM - Stanza Privata</title>");
            htmlResponse.append("<link rel='stylesheet' href='/style.css'>");
            htmlResponse.append("</head><body>");

            ResultSet rsStanza = null;
            ResultSet rsDoc = null;

            try {
                // 2. Cerchiamo la Stanza
                rsStanza = DBMSboundary.getInstance().queryDBMSStanzaByLinkIdentifier(linkCode);

                if (rsStanza != null && rsStanza.next()) {
                    int idStanza = rsStanza.getInt("idStanza");
                    String nomeStanza = rsStanza.getString("nomeStanza");

                    // 3. SALVATAGGIO NEL DB (Addio Errore!)
                    // Inseriamo il Visualizzatore e otteniamo il suo VERO ID
                    int idNuovoVisualizzatore = DBMSboundary.getInstance().insertDBMSVisualizzatore(nome, cognome, email);
                    // Usiamo il VERO ID per loggare la visualizzazione
                    DBMSboundary.getInstance().insertDBMSVisualizzazione(idStanza, idNuovoVisualizzatore);

                    // 4. Costruiamo la pagina della stanza
                    htmlResponse.append("<div class='container'>");
                    htmlResponse.append("<header><h1>🏠 ").append(nomeStanza).append("</h1>");
                    htmlResponse.append("<p>Benvenuto, ").append(nome).append("!</p></header>");
                    htmlResponse.append("<div class='document-grid'>");

                    rsDoc = DBMSboundary.getInstance().queryDBMSListaDocumentiStanza(idStanza);
                    if (rsDoc != null) {
                        while (rsDoc.next()) {
                            int idDoc = rsDoc.getInt("idDocumento");
                            String percorso = rsDoc.getString("percorso");
                            boolean scaricabile = rsDoc.getBoolean("scaricabile");
                            String nomeFile = new File(percorso).getName();

                            htmlResponse.append("<div class='card'>");
                            htmlResponse.append("<h3>📄 ").append(nomeFile).append("</h3>");

                            if (scaricabile) {
                                htmlResponse.append("<iframe src='/file?id=").append(idDoc).append("'></iframe>");
                                htmlResponse.append("<a class='btn download-btn' href='/file?id=").append(idDoc).append("' download>⬇️ Scarica Documento</a>");
                            } else {
                                htmlResponse.append("<iframe src='/file?id=").append(idDoc).append("#toolbar=0'></iframe>");
                                htmlResponse.append("<p class='private-badge'>🔒 Sola lettura (Download disabilitato)</p>");
                            }
                            htmlResponse.append("</div>");
                        }
                    }
                    htmlResponse.append("</div></div>");
                } else {
                    htmlResponse.append("<div class='container'><h1>❌ Errore 404</h1><p>Stanza inesistente.</p></div>");
                }
            } catch (Exception e) {
                e.printStackTrace();
                htmlResponse.append("<div class='container'><h1>Errore Interno del Server 500</h1></div>");
            } finally {
                try {
                    if (rsStanza != null && !rsStanza.isClosed()) rsStanza.getStatement().close();
                    if (rsDoc != null && !rsDoc.isClosed()) rsDoc.getStatement().close();
                } catch (Exception ignore) {}
            }
            htmlResponse.append("</body></html>");
            inviaRisposta(exchange, htmlResponse.toString(), 200);
        }

        // --- HELPER per accorciare il codice di invio ---
        private void inviaRisposta(HttpExchange exchange, String html, int statusCode) throws IOException {
            byte[] responseBytes = html.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }


    // =========================================
    // HANDLER 2: Invio fisico dei file PDF/Immagini
    // =========================================
    static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery(); // es: "id=5"
            if (query != null && query.startsWith("id=")) {
                int idDocumento = Integer.parseInt(query.split("=")[1]);

                ResultSet rs = null;
                try {
                    rs = DBMSboundary.getInstance().queryDBMSDocumentoById(idDocumento);
                    if (rs != null && rs.next()) {
                        String percorso = rs.getString("percorso");
                        File file = new File(percorso);

                        if (file.exists()) {
                            // Cerca di capire se è PDF o JPG per dire al browser come aprirlo
                            String mimeType = Files.probeContentType(file.toPath());
                            if (mimeType == null) mimeType = "application/octet-stream";

                            exchange.getResponseHeaders().set("Content-Type", mimeType);
                            exchange.sendResponseHeaders(200, file.length());

                            OutputStream os = exchange.getResponseBody();
                            Files.copy(file.toPath(), os);
                            os.close();
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try { if (rs != null && !rs.isClosed()) rs.getStatement().close(); } catch (Exception ignore) {}
                }
            }
            // File non trovato
            String errorMsg = "File non trovato.";
            exchange.sendResponseHeaders(404, errorMsg.length());
            OutputStream os = exchange.getResponseBody();
            os.write(errorMsg.getBytes());
            os.close();
        }
    }

    // =========================================
    // HANDLER 3: Invio file CSS per il design
    // =========================================
    static class CssHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            File cssFile = new File("src/main/resources/web/style.css");
            if (cssFile.exists()) {
                exchange.getResponseHeaders().set("Content-Type", "text/css");
                exchange.sendResponseHeaders(200, cssFile.length());
                OutputStream os = exchange.getResponseBody();
                Files.copy(cssFile.toPath(), os);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
}