module org.example.snappfrontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires java.net.http;
    requires javafx.swing;


    opens org.example.snappfrontend to javafx.fxml, com.fasterxml.jackson.databind;
    opens org.example.snappfrontend.controllers to javafx.fxml, com.fasterxml.jackson.databind;
    opens org.example.snappfrontend.dto to com.fasterxml.jackson.databind;
    opens org.example.snappfrontend.models to com.fasterxml.jackson.databind, javafx.fxml;



    exports org.example.snappfrontend;
    exports org.example.snappfrontend.controllers;
    exports org.example.snappfrontend.dto;
    exports org.example.snappfrontend.utils;
    exports org.example.snappfrontend.http;
    exports org.example.snappfrontend.models;
}
