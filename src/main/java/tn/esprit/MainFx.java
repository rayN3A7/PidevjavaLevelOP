package tn.esprit;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {
 /*   public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage Stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent root = loader.load();
        Scene sc = new Scene(root);
        Stage.setScene(sc);
        Stage.show();
    }
}*/
 @Override
 public void start(Stage primaryStage) throws Exception {
     FXMLLoader loader = new FXMLLoader(getClass().getResource("/QuestionForm.fxml")); // Adjust path if needed
     Scene scene = new Scene(loader.load());
     primaryStage.setTitle("Application de Forum");
     primaryStage.setWidth(1080);  // Set consistent width
     primaryStage.setHeight(600);
     primaryStage.setScene(scene);
     primaryStage.show();
 }

    public static void main(String[] args) {
        launch(args);
    }
}
