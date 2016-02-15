/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.innerio.javafxgui.window;

import java.util.List;
import java.util.StringJoiner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.drs.beam.core.modules.innerio.javafxgui.WindowController;
import com.drs.beam.core.modules.innerio.javafxgui.WindowSettingsProvider;
import com.drs.beam.core.modules.tasks.TaskMessage;


/**
 *
 * @author Diarsid
 */
class TasksNotificationWindow extends BeamWindow implements Runnable {
    
    private final List<TaskMessage> tasks;
    
    TasksNotificationWindow(
            List<TaskMessage> tasks,
            WindowController c, 
            WindowSettingsProvider p) {
        super(c, p);
        this.tasks = tasks;
    }
    
    @Override
    public void run() {  
        prepareStage();
        setContent(createMainContent());
        setTitle("Task");
        setIconUrl(settings().getTaskIconURL());
        showThis();
    }    
    
    private Pane createMainContent() {
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 15, 15, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setMaxHeight(800);
        hBox.setAlignment(Pos.CENTER_LEFT);        
        
        ImageView taskPic = 
                new ImageView(new Image("file:"+settings().getTaskImageURL()));        
        Label picture = new Label("", taskPic);  
        
        VBox allTasksBox = new VBox();        
        
        for (int i = 0; i < this.tasks.size(); i++) {
         
            VBox taskBox = new VBox();
            taskBox.setAlignment(Pos.TOP_LEFT);    

            Label taskTimeLabel = new Label();
            taskTimeLabel.setStyle(fontCSS());
            taskTimeLabel.setText(this.tasks.get(i).getTime());
            taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));

            Label taskTextLabel = new Label(); 
            taskTextLabel.setStyle(fontCSS());
            taskTextLabel.setPadding(new Insets(0, 0, 0, 20));

            StringJoiner joiner = new StringJoiner("\n");
            for(String s : this.tasks.get(i).getContent()){
                joiner.add(s);
            }
            taskTextLabel.setText(joiner.toString());

            taskBox.getChildren().addAll(taskTimeLabel, taskTextLabel);   
            
            allTasksBox.getChildren().add(taskBox);
            
        }
        
        hBox.getChildren().addAll(picture, allTasksBox);
        
        mainVBox.getChildren().addAll(hBox, newOkButton("Done"));
        
        return mainVBox;
    }
}
