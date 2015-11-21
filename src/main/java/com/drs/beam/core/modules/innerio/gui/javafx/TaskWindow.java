/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.innerio.gui.javafx;

import java.util.StringJoiner;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.modules.innerio.gui.GuiWindowsController;

/**
 *
 * @author Diarsid
 */
public class TaskWindow implements Runnable{
    // Fields =============================================================================
    private final Task task;
    private final String mainImage;
    private final String iconImage;
    private final GuiWindowsController controller;
    
    // Constructors =======================================================================
    public TaskWindow(Task task, String mainImage, String iconImage, GuiWindowsController controller){    
        this.task = task;
        this.mainImage = mainImage;
        this.iconImage = iconImage;
        this.controller = controller;
    }

    // Methods ============================================================================
    @Override
    public void run() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                controller.minusOneActiveWindow();
            }
        });
        stage.setAlwaysOnTop(true);
        
        VBox mainVBox = new VBox(15); 
        mainVBox.setPadding(new Insets(15, 15, 15, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox taskTextBox = new VBox();
        taskTextBox.setAlignment(Pos.TOP_LEFT);        
        
        ImageView taskPic = new ImageView(new Image("file:"+this.mainImage));
        
        Label picture = new Label("", taskPic);        
        
        Label taskTimeLabel = new Label();
        taskTimeLabel.setFont(new Font(14.0));
        taskTimeLabel.setText(task.getTimeOutputString());
        
        Label taskTextLabel = new Label(); 
        taskTextLabel.setFont(new Font(14.0));
        taskTextLabel.setPadding(new Insets(0, 0, 0, 20));
        
        StringJoiner joiner = new StringJoiner("\n");
        for(String s : task.getContent()){
            joiner.add(s);
        }
        taskTextLabel.setText(joiner.toString());
        
        taskTextBox.getChildren().addAll(taskTimeLabel, taskTextLabel);
        hBox.getChildren().addAll(picture, taskTextBox);
        
        Button button = new Button("Ok");
        button.setFont(new Font(14.0));
        button.setMinWidth(100);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
                controller.minusOneActiveWindow();
            }
        });
        
        mainVBox.getChildren().addAll(hBox, button);
        
        Scene scene = new Scene(mainVBox);
        
        stage.setTitle("Task");
        stage.getIcons().add(new Image("file:"+this.iconImage));
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }    
}
