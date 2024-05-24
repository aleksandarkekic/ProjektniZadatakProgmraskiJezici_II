package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import logger.Log;

import java.awt.*;
import java.io.File;
import java.util.logging.Level;
public class PrikazRezultataController
{
    @FXML
    ListView<String> myListView;
    public void initialize()
    {
        File file = new File(MainController.PUTANJA_REZULTATI);
        String [] files = file.list();
        myListView.getItems().addAll(files);
        myListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        myListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
            {
                String fajl = myListView.getSelectionModel().getSelectedItem();
                try
                {
                    File file = new File(MainController.PUTANJA_REZULTATI+File.separator+fajl);
                    Desktop desktop = Desktop.getDesktop();
                    if(file.exists())
                    {
                        desktop.open(file);
                    }
                }
                catch(Exception e)
                {
                    Log.logger.log(Level.WARNING,e.fillInStackTrace().toString(),e);
                }
            }
        });
    }
}