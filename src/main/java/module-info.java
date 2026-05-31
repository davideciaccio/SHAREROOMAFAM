module com.shareroomafam {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.mail;
    requires java.desktop;

    opens com.shareroomafam to javafx.fxml;
    opens com.shareroomafam.boundary to javafx.fxml;
    opens com.shareroomafam.control to javafx.fxml;

    exports com.shareroomafam;
    exports com.shareroomafam.boundary;
    exports com.shareroomafam.control;
    exports com.shareroomafam.entity;
}