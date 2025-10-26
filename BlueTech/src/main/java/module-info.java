module br.com.squadtech.bluetech {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql;
    requires com.jfoenix;
    requires de.jensd.fx.glyphs.commons;
    requires de.jensd.fx.glyphs.controls;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jbcrypt;
    requires com.zaxxer.hikari;
    requires mysql.connector.j;
    requires jakarta.mail;

    opens br.com.squadtech.bluetech to javafx.fxml;
    exports br.com.squadtech.bluetech;

    opens br.com.squadtech.bluetech.controller.professorOrientador to javafx.fxml;
    exports br.com.squadtech.bluetech.controller.professorOrientador;

    opens br.com.squadtech.bluetech.controller.login to javafx.fxml;
    exports br.com.squadtech.bluetech.controller.login;

    opens br.com.squadtech.bluetech.controller.aluno to javafx.fxml;
    exports br.com.squadtech.bluetech.controller.aluno;

    opens br.com.squadtech.bluetech.controller.professorTG to javafx.fxml;
    exports br.com.squadtech.bluetech.controller.professorTG;


}
