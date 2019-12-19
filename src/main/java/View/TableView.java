package View;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.LinkedList;

public class TableView {
    /**
 * Displays a simple alert box.
 * @param title - String - the stage title
 * @param buttonText - String
 */
public static void display(String title, LinkedList<Pair<String,Integer>> dictionary, String buttonText) {
    Stage window = new Stage();
    window.setTitle(title);
    Parent root = null;
    try {
        root = FXMLLoader.load(AlertBox.class.getClassLoader().getResource("TableView.fxml"));
    } catch (
            IOException e) {
        System.out.println("Couldn't open the \"ExitConfirmBox.fxml\" fxml file");
        e.printStackTrace();
    }

    javafx.scene.control.TableView tableView = (javafx.scene.control.TableView) root.lookup("#tableView");

    TableColumn<String, Pair<String,String>> column1 = new TableColumn("Term");
    column1.setCellValueFactory(new PropertyValueFactory<>("key"));

    TableColumn<String, Pair<String,String>> column2 = new TableColumn("Cumulative frequency");
    column2.setCellValueFactory(new PropertyValueFactory<>("value"));

    tableView.getColumns().add(column1);
    tableView.getColumns().add(column2);

    for (Pair<String,Integer> pair: dictionary){
        tableView.getItems().add(pair);
    }

    Button closeButton = (Button) root.lookup("#closeButton");
    closeButton.setText(buttonText);
    closeButton.setOnAction(e-> window.close());

    Scene scene = new Scene(root);
    window.setScene(scene);
    window.showAndWait();
}
}

