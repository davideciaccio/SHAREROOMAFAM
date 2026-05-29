package com.shareroomafam.utility;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {

    // Inserisci qui l'email di sistema e la Password per App (se usi Gmail)
    private static final String EMAIL_MITTENTE = "shareroomafam@gmail.com";
    private static final String PASSWORD_MITTENTE = "taou nqev mrqb vpry";

    public static boolean inviaCodice2FA(String emailDestinatario, String codiceGenerato) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_MITTENTE, PASSWORD_MITTENTE);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_MITTENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinatario));
            message.setSubject("ShareRoomAfam - Il tuo codice di accesso");
            message.setText("Benvenuto su ShareRoomAfam!\n\nIl tuo codice per l'autenticazione a due fattori è: "
                    + codiceGenerato + "\n\nNon condividere questo codice con nessuno.");

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}