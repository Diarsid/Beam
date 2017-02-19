/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io.javafxgui.window;

import java.util.List;
import java.util.StringJoiner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import diarsid.beam.core.modules.io.javafxgui.WindowController;
import diarsid.beam.core.modules.io.javafxgui.WindowResources;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;


/**
 *
 * @author Diarsid
 */
class TasksNotificationWindow extends BeamWindow implements Runnable {
    
    private final List<TaskMessage> tasks;
    private final String period;
    
    TasksNotificationWindow(
            String period,
            List<TaskMessage> tasks,
            WindowController c, 
            WindowResources resources) {
        super(resources, c);
        this.period = period;
        this.tasks = tasks;
    }
    
    @Override
    public void run() {  
        prepareStage();
        setContent(createMainContent());
        setTitle("Task");
        setTaskIcon();
        showThis();
    }    
    
    private Pane createMainContent() {
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 13, 0, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);        
        
        Label picture = new Label("", new ImageView(getTaskImage()));  
          
        if ( this.tasks.isEmpty() ) {
            Label noTasksLabel = new Label();
            noTasksLabel.setText(
                    "There are no any tasks scheduled in this " + 
                            this.period + ".");
            noTasksLabel.setPadding(new Insets(20, 20, 20, 20));
            hBox.getChildren().addAll(picture, noTasksLabel);
            mainVBox.getChildren().addAll(hBox, newOkButton("Ok"));
        } else {
            Label titleLabel = new Label();
            titleLabel.setText("All tasks scheduled in this " + 
                    this.period + ":");
            titleLabel.setPadding(new Insets(5, 10, 5, 10));
            ListView<HBox> listView = new ListView<>();
            ObservableList<HBox> list = FXCollections.observableArrayList();
            listView.setPrefWidth(400);
            
            
            for (int i = 0; i < this.tasks.size(); i++) {

                HBox taskBox = new HBox();
                taskBox.setAlignment(Pos.TOP_LEFT);

                Label taskTimeLabel = new Label();
                taskTimeLabel.setText(this.tasks.get(i).time());
                taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));

                Label taskTextLabel = new Label(); 
                taskTextLabel.setPadding(new Insets(0, 0, 0, 20));

                StringJoiner joiner = new StringJoiner("\n");
                for(String s : this.tasks.get(i).text()){
                    joiner.add(s);
                }                
                taskTextLabel.setText(joiner.toString());

                taskBox.getChildren().addAll(taskTimeLabel, taskTextLabel);
                list.add(taskBox);

            }
            listView.setPrefHeight(300);
            listView.setItems(list);
            hBox.getChildren().addAll(picture, listView);
            mainVBox.getChildren().addAll(titleLabel, hBox, newOkButton("Ok"));
        }        
        
        return mainVBox;
    }
}
