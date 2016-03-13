/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.innerio.javafxgui.window;

import java.util.StringJoiner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import diarsid.beam.core.modules.innerio.javafxgui.WindowController;
import diarsid.beam.core.modules.innerio.javafxgui.WindowResources;
import diarsid.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
class TaskWindow extends BeamWindow implements Runnable {
    
    private final TaskMessage task;
    
    TaskWindow(
            TaskMessage task,
            WindowController controller, 
            WindowResources resources) { 
        
        super(resources, controller);
        this.task = task;
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
        mainVBox.setPadding(new Insets(15, 15, 0, 15));
        mainVBox.setAlignment(Pos.TOP_CENTER);
        
        HBox hBox = new HBox(15);
        hBox.setMinWidth(300);
        hBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox taskTextBox = new VBox();
        taskTextBox.setAlignment(Pos.TOP_LEFT);   
        
        Label picture = new Label("", new ImageView(getTaskImage()));        
        
        Label taskTimeLabel = new Label();
        taskTimeLabel.setText(task.getTime());
        taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));
        
        Label taskTextLabel = new Label(); 
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
