package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {
 /*  public static void main(String[] args) {
        launch(args);
    }*/

     @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/forum.fxml"));
         Scene scene = new Scene(loader.load());
         primaryStage.setTitle("Application de Forum");
         primaryStage.setWidth(1080);  // Set consistent width
         primaryStage.setHeight(600);
         primaryStage.setScene(scene);
         primaryStage.show();
    }
/*@Override
 public void start(Stage primaryStage) throws Exception {
     FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/forum.fxml")); // Adjust path if needed
     Scene scene = new Scene(loader.load());
     primaryStage.setTitle("Application de Forum");
     primaryStage.setWidth(1080);  // Set consistent width
     primaryStage.setHeight(600);
     primaryStage.setScene(scene);
     primaryStage.show();
 }*/

    public static void main(String[] args) {
        launch(args);
    }
}
