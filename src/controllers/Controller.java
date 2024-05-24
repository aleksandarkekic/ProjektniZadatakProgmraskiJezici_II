package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logger.Log;

import java.util.logging.Level;


public class Controller {
    @FXML
    private Label label1;
    @FXML
    private TextField txf1,txf2;
    @FXML
    private Button myButton;

    private boolean provjeraValidnostiIgraca() {
        int broj=Integer.parseInt(txf1.getText());
        if(broj>=2 && broj <=4) {
            MainController.brojIgraca=broj;
            return true;
        }
        return false;
    }

    private boolean provjeraValidnostiMatrice() {
        int broj=Integer.parseInt(txf2.getText());
        if(broj>=7 && broj <=10) {
            MainController.dimenzijeMatrice=broj;
            return true;
        }
        return false;
    }

    public void potvrdi(ActionEvent event) {
        try {
            if (!provjeraValidnostiIgraca())
                label1.setText("Pogresan unos igraca");
            else if (!provjeraValidnostiMatrice())
                label1.setText("Pogresan unos dimenzije");
            else {
                label1.setText("Uspjesan unos!");
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                Image icon=new Image("icons/game.png");
                stage.getIcons().add(icon);
                stage.show();
            }
        }catch (NumberFormatException e){
            label1.setText("Unosite samo brojeve!");
        }catch(Exception e){
            Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
        }

    }





}
