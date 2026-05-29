package com.shareroomafam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Definiamo il percorso del file FXML
        // NOTA: Il percorso inizia con "/" che indica la radice della cartella resources
        URL fxmlLocation = getClass().getResource("/com/shareroomafam/view/AuthView.fxml");

        if (fxmlLocation == null) {
            System.err.println("ERRORE FATALE: Impossibile trovare il file AuthView.fxml!");
            System.err.println("Assicurati che si trovi in: src/main/resources/com/shareroomafam/view/");
            System.exit(1);
        }

        // 2. Carichiamo l'interfaccia grafica
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();

        // 3. Creiamo la scena e la impostiamo sulla finestra principale (Stage)
        Scene scene = new Scene(root);

        primaryStage.setTitle("ShareRoom AFAM - Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Blocchiamo il ridimensionamento per mantenere il layout pulito
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.out.println("⏳ Avvio dell'applicazione ShareRoom AFAM...");
        // Il comando launch avvia il ciclo di vita di JavaFX (che chiama il metodo start)
        launch(args);
    }
}