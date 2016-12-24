/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.io.javafxgui.window;

import java.util.StringJoiner;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import diarsid.beam.core.modules.io.javafxgui.ReusableTaskWindow;
import diarsid.beam.core.modules.io.javafxgui.WindowController;
import diarsid.beam.core.modules.io.javafxgui.WindowResources;
import diarsid.beam.core.modules.tasks.TimeMessage;

/**
 *
 * @author Diarsid
 */
class TaskWindow extends BeamWindow implements ReusableTaskWindow {
    
    private Label taskTimeLabel;
    private Label taskTextLabel;    
    private String taskText;
    private String taskTime;    
    private boolean alreadyInitAndCanBeReused;
    
    TaskWindow(
            TimeMessage task,
            WindowController controller, 
            WindowResources resources) { 
        
        super(resources, controller);
        this.taskTime = task.getTime();
        StringJoiner joiner = new StringJoiner("\n");
        for(String s : task.getContent()){
            joiner.add(s);
        }
        this.taskText = joiner.toString();
        this.alreadyInitAndCanBeReused = false;
    }
    
    @Override
    public void run() {  
        if ( ! this.alreadyInitAndCanBeReused ) {
            this.initWindowObject();
        } else {
            this.reuseThisWindowObject();
        }
    }
    
    /* 
     * This method does not perform in Java FX Application Thread, 
     * thus it cannot work with any Java FX API and nodes.
     * 
     * It sets only this object String private fields which will be 
     * used later to set new values for Java FX Labels by
     * reuseThisWindowObject() invocation. 
     */
    @Override
    public void reuseWithNewTask(TimeMessage task) {
        this.taskTime = task.getTime();
        StringJoiner joiner = new StringJoiner("\n");
        for(String s : task.getContent()){
            joiner.add(s);
        }
        this.taskText = joiner.toString();
    }
    
    private void initWindowObject() {
        prepareStage();
        setContent(createMainContent());
        setTitle("Task");
        setTaskIcon();
        showThis();
        this.alreadyInitAndCanBeReused = true;        
    }
    
    private void reuseThisWindowObject() {
        this.taskTextLabel.setText(this.taskText);
        this.taskTimeLabel.setText(this.taskTime);
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
        
        this.taskTimeLabel = new Label();
        this.taskTimeLabel.setText(this.taskTime);
        this.taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));
        
        this.taskTextLabel = new Label(); 
        this.taskTextLabel.setPadding(new Insets(0, 0, 0, 20));
        this.taskTextLabel.setText(this.taskText);
        
        taskTextBox.getChildren().addAll(this.taskTimeLabel, this.taskTextLabel);
        hBox.getChildren().addAll(picture, taskTextBox);
        
        mainVBox.getChildren().addAll(hBox, newOkButton("Done"));
        
        return mainVBox;
    }
}