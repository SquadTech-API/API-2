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
    requires de.jensd.fx.glyphs.materialicons;
    requires flexmark;
    requires flexmark.util.ast;
    requires flexmark.util.builder;
    requires flexmark.util.data;
    requires flexmark.util.sequence;
    requires javafx.web;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires jakarta.mail;
    requires javafx.base;
    requires javafx.media;

    // ABERTURAS PARA JAVAFX
    opens br.com.squadtech.bluetech to javafx.fxml;
    opens br.com.squadtech.bluetech.dao to javafx.base;
    opens br.com.squadtech.bluetech.model to javafx.base;

    // EXPORTS PRINCIPAIS
    exports br.com.squadtech.bluetech;

    // CONTROLLERS
    opens br.com.squadtech.bluetech.controller.professorOrientador to javafx.fxml;

    exports br.com.squadtech.bluetech.controller.professorOrientador;

    opens br.com.squadtech.bluetech.controller.login to javafx.fxml;

    exports br.com.squadtech.bluetech.controller.login;

    opens br.com.squadtech.bluetech.controller.aluno to javafx.fxml;

    exports br.com.squadtech.bluetech.controller.aluno;

    opens br.com.squadtech.bluetech.controller.professorTG to javafx.fxml;

    exports br.com.squadtech.bluetech.controller.professorTG;

    // 🔥 ADMIN - LINHAS ADICIONADAS
    opens br.com.squadtech.bluetech.controller.admin to javafx.fxml;

    exports br.com.squadtech.bluetech.controller.admin;

    // UTIL
    exports br.com.squadtech.bluetech.util;
}