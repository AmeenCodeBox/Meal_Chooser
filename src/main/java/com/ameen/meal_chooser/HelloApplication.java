package com.ameen.meal_chooser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.createDatabase();
//        DatabaseManager.addMeal("ورق عنب");

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 500, 400);
        stage.setTitle("غدانا اليوم \uD83C\uDFB2");
        stage.setScene(scene);

        Image appIcon = new Image(HelloApplication.class.getResourceAsStream("app-logo.png"), 16, 16, true, true);
        stage.getIcons().add(appIcon);

        stage.show();
    }
}
