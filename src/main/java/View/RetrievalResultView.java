package View;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RetrievalResultView {
    public static void display(String title, ArrayList<Pair<String, ArrayList<String>>> rankedDocuments) {
        Stage window = new Stage();
        window.setTitle(title);
        Parent root = null;
        try {
            root = FXMLLoader.load(AlertBox.class.getClassLoader().getResource("RetrievalResultView.fxml"));
        } catch (
                IOException e) {
            System.out.println("Couldn't open the \"RetrievalResultView.fxml\" fxml file");
            e.printStackTrace();
        }

        javafx.scene.control.TableView tableView = (javafx.scene.control.TableView) root.lookup("#resultTableView");

      String columnTitle = "";
        for (Pair<String,ArrayList<String>> pair : rankedDocuments){
           // TableColumn<String, String> tableColumn = new TableColumn(pair.getKey());
            if(columnTitle.equals(""))
            {
                columnTitle = pair.getKey();
            }
            else
            {
                columnTitle = columnTitle+" "+pair.getKey();

            }
            //tableColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            //tableView.getColumns().add(tableColumn);
            //tableView.getItems().addAll(pair);
//            ArrayList<String> documentsNumbers = pair.getValue();
//            tableView.getItems().add(documentsNumbers);


        }
        //Entries addition


        for (int i = 0; i < 50; i++) {
            ArrayList<Pair<String,String>> entry = new ArrayList<>();
            //Entry creation
            for (Pair<String,ArrayList<String>> pair : rankedDocuments){
                if(pair.getValue().size()>i){
                    columnTitle = columnTitle+" "+pair.getValue().get(i);
                }else
                {
                    columnTitle = columnTitle+" "+"NULL";
                }
            }

        }
        String [] allColumnTitle = columnTitle.split(" ");
        TestDataGenerator testDataGenerator = new TestDataGenerator();
        testDataGenerator.setLOREN(allColumnTitle);

        List<String> columnNames = testDataGenerator.getNext(rankedDocuments.size() );
        for (int j = 0; j < columnNames.size(); j++) {
            final int finalIdx = j;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(
                    columnNames.get(j)
            );
            column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(finalIdx)));
            tableView.getColumns().add(column);
        }

        for (int i = 0; i < 50; i++) {
            ObservableList<String> strings = FXCollections.observableArrayList(testDataGenerator.getNext(rankedDocuments.size()));
            tableView.getItems().add(
                    FXCollections.observableArrayList(
                            testDataGenerator.getNext(rankedDocuments.size())
                    )
            );
        }











        Button closeButton = (Button) root.lookup("#closeButton");
        closeButton.setOnAction(e-> window.close());


        Scene scene = new Scene(root);
        window.setScene(scene);
        window.showAndWait();

    }

    private static  List<String> getNext(int nWord , int curWord , String []allColumnTitle )
    {
        List<String> words = new ArrayList<>();

        for(int j = 0; j<nWord ; j++)
        {
            if(curWord == Integer.MAX_VALUE)
            {
                curWord = 0;
            }
            words.add(allColumnTitle [curWord % allColumnTitle.length] );
            curWord++;
        }
        return words;
    }



    private static class TestDataGenerator {
        private  String[] LOREM = null;

        private int curWord = 0;

        public void setLOREN(String [] LOREM)
        {
            this.LOREM = LOREM;
        }
        List<String> getNext(int nWords) {
            List<String> words = new ArrayList<>();

            for (int i = 0; i < nWords; i++) {
                if (curWord == Integer.MAX_VALUE) {
                    curWord = 0;
                }

                words.add(LOREM[curWord % LOREM.length]);
                curWord++;
            }

            return words;
        }
    }
}