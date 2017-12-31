/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.application.gui.javafx;

import java.util.StringJoiner;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import diarsid.beam.core.domain.entities.Task;

/**
 *
 * @author Diarsid
 */
class BeamTaskWindow extends BeamMessageWindow {
    
    private final GuiJavaFXResources resources;
    
    private Task task;
    private Label taskTimeLabel;
    private Label taskTextLabel;  
    
    BeamTaskWindow(
            WindowManager controller, 
            GuiJavaFXResources resources) {          
        super(resources, controller);   
        this.resources = resources;
    }

    @Override
    protected Pane createBeamMessageWindowContentPane() {
//        mainVBox.setPadding(new Insets(15, 15, 0, 15));
        
        HBox messageContentBox = new HBox(15);
        messageContentBox.setMinWidth(300);
        messageContentBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox taskTextBox = new VBox();
        taskTextBox.setAlignment(Pos.TOP_LEFT);   
        
        Node picture = new ImageView(this.resources.taskImage());        
        
        this.taskTimeLabel = new Label();
//        this.taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));
        
        this.taskTextLabel = new Label(); 
//        this.taskTextLabel.setPadding(new Insets(0, 0, 0, 20));
        
        taskTextBox.getChildren().addAll(this.taskTimeLabel, this.taskTextLabel);
        messageContentBox.getChildren().addAll(picture, taskTextBox);
        
        super.setBarTitle("Task");
        
        return messageContentBox;
    }
    
    public void acceptTask(Task task) {
        this.task = task;
    }

    @Override
    protected void refreshBeamWindowState() {
        StringJoiner joiner = new StringJoiner("\n");
        for (String s : task.text()) {
            joiner.add(s);
        }
        this.taskTextLabel.setText(joiner.toString());
        this.taskTimeLabel.setText(this.task.stringifyTime());
    }

    @Override
    protected void onBeamMessageWindowCLosed() {
        this.task = null;
    }
}
