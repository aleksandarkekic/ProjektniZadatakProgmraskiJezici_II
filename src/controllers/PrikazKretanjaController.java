package controllers;

import icons.Slika;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import logger.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class PrikazKretanjaController implements Initializable {

    public static ArrayList<Integer> putanja = new ArrayList<>();

    @FXML
    GridPane gridIstorija;

    public void initialize(URL url, ResourceBundle resourceBundle) {
        for (int i = 0; i < MainController.dimenzijeMatrice; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(-1);//podesavanje koliko procenata ce zauzimati
            gridIstorija.getColumnConstraints().add(columnConstraints);
        }
        for (int j = 0; j < MainController.dimenzijeMatrice; j++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(-1);
            gridIstorija.getRowConstraints().add(rowConstraints);
        }
        gridIstorija.setGridLinesVisible(true);

        for (int i = 0; i < MainController.dimenzijeMatrice; i++) {
            for (int j = 0; j < MainController.dimenzijeMatrice; j++) {
                try {
                    //FileInputStream imageStream = new FileInputStream(Slika.SLIKA_JEDAN);
                    // Image image = new Image(imageStream);
                    ImageView myImageView = new ImageView();
                    //myImageView.setImage(image);
                    myImageView.setFitWidth(50);
                    //myGridPane.getPrefWidth() / (double) 16
                    myImageView.setFitHeight(50);
                    //myGridPane.getPrefHeight() / (double) 16
                    gridIstorija.add(new TilePane(myImageView), i, j);
                } catch (Exception e) {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        }
        for (int i = 0; i < putanja.size() - 1; i += 2) {
            try {
                FileInputStream imageStream = new FileInputStream(Slika.SLIKA_RUPA);
                Image image = new Image(imageStream);
                ImageView myImageView = new ImageView();
                myImageView.setImage(image);
                myImageView.setFitWidth(gridIstorija.getPrefWidth() / (double) 7);
                myImageView.setFitHeight(gridIstorija.getPrefWidth() / (double) 7);
                this.gridIstorija.add(myImageView, putanja.get(i), putanja.get(i + 1));

            } catch (Exception e) {
                Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
            }
        }
    }
}