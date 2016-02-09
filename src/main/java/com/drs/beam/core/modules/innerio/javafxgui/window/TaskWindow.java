/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.innerio.javafxgui.window;

import com.drs.beam.core.modules.innerio.javafxgui.WindowController;
import com.drs.beam.core.modules.innerio.javafxgui.WindowSettingsProvider;

import java.util.StringJoiner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
class TaskWindow extends BeamWindow implements Runnable {
    
    private final TaskMessage task;
    
    TaskWindow(
            TaskMessage task,
            WindowController controller,
            WindowSettingsProvider provider) { 
        
        super(controller, provider);
        this.task = task;
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
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox taskTextBox = new VBox();
        taskTextBox.setAlignment(Pos.TOP_LEFT);        
        
        ImageView taskPic = 
                new ImageView(new Image("file:"+settings().getTaskImageURL()));
        
        Label picture = new Label("", taskPic);        
        
        Label taskTimeLabel = new Label();
        taskTimeLabel.setStyle(fontCSS());
        taskTimeLabel.setText(task.getTime());
        taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));
        
        Label taskTextLabel = new Label(); 
        taskTextLabel.setStyle(fontCSS());
        taskTextLabel.setPadding(new Insets(0, 0, 0, 20));
        
        StringJoiner joiner = new StringJoiner("\n");
        for(String s : task.getContent()){
            joiner.add(s);
        }
        taskTextLabel.setText(joiner.toString());
        
        taskTextBox.getChildren().addAll(taskTimeLabel, taskTextLabel);
        hBox.getChildren().addAll(picture, taskTextBox);
        
        mainVBox.getChildren().addAll(hBox, newOkButton("Done"));
        
        return mainVBox;
    }
}
