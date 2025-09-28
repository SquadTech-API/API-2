module org.example {
    requires javafx.controls;
    requires javafx.fxml;

    exports br.com.squadtech.bluetech;
    exports br.com.squadtech.bluetech.controller.professorOrientador;

    opens br.com.squadtech.bluetech to javafx.fxml;
    opens br.com.squadtech.bluetech.controller.professorOrientador to javafx.fxml;
}
