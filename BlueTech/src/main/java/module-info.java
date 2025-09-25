module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example to javafx.fxml;
    exports org.example;
    exports br.com.squadtech.bluetech;
    opens br.com.squadtech.bluetech to javafx.fxml;
    exports br.com.squadtech.bluetech.controller;
    opens br.com.squadtech.bluetech.controller to javafx.fxml;
}
