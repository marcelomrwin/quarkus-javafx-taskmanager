package com.github.marcelomrwin.javafx;

import java.io.IOException;
import java.net.URL;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    FXMLLoader fxmlLoader;

    @SneakyThrows
    public void start(@Observes @StartupScene Stage stage) {

        try {
            URL fxml = getClass().getResource("/GUI.fxml");
            Parent fxmlParent = fxmlLoader.load(fxml.openStream());
            stage.setScene(new Scene(fxmlParent));
//			stage.setScene(new Scene(fxmlParent, 200, 400));
            stage.setTitle("Task Manager Using Quarkus and JavaFX!");
            stage.show();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
