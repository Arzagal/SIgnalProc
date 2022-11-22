module com.example.signalproc {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.signalproc to javafx.fxml;
    //exports com.example.signalproc;
    exports com.example.signalproc.ui;
    opens com.example.signalproc.ui to javafx.fxml;
}