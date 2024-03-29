package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class GUI extends Application {

    private GUIController controller;
    private MyModel model;
    private MyViewModel viewModel;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        model = new MyModel();

        viewModel =  new MyViewModel(model);
        model.addObserver(viewModel);

        primaryStage.setTitle("My Board Game");
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("GUI.fxml").openStream());
        Scene scene = new Scene(root , 400 , 330);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(330);
        primaryStage.setMinWidth(400);
        controller = fxmlLoader.getController();
        controller.setViewModel(viewModel);
        viewModel.addObserver(controller);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            controller.exitHandler(event);
        });

        primaryStage.show();
    }
}
