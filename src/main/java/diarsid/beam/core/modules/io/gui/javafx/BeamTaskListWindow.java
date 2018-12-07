/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io.gui.javafx;

import java.util.List;
import java.util.StringJoiner;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import diarsid.beam.core.domain.entities.Task;

import static javafx.collections.FXCollections.observableArrayList;


/**
 *
 * @author Diarsid
 */
class BeamTaskListWindow extends BeamMessageWindow {
    
    private final GuiJavaFXResources resources;
    
    private String header;
    private List<Task> tasks;
    
    private ObservableList<HBox> taskList;
    
    BeamTaskListWindow(
            WindowManager windowManager, 
            GuiJavaFXResources resources) {
        super(resources, windowManager);
        this.resources = resources;
    }
    
    public final BeamTaskListWindow accept(String header, List<Task> tasks) {
        this.header = header;
        this.tasks = tasks;
        return this;
    }

    @Override
    protected void onBeamMessageWindowCLosed() {
        this.tasks.clear();
        this.tasks = null;
    }

    @Override
    protected void refreshBeamWindowState() {
        this.taskList.clear();
        super.setBarTitle(this.header);        
        this.tasks.forEach((task) -> {
            this.taskList.add(this.taskToHBox(task));
        });        
    }
    
    private HBox taskToHBox(Task task) {
        HBox taskBox = new HBox();
        taskBox.setAlignment(Pos.TOP_LEFT);

        Label taskTimeLabel = new Label();
        taskTimeLabel.setText(task.stringifyTime());
//        taskTimeLabel.setPadding(new Insets(0, 0, 8, 0));

        Label taskTextLabel = new Label(); 
//        taskTextLabel.setPadding(new Insets(0, 0, 0, 20));

        StringJoiner joiner = new StringJoiner("\n");
        for (String s : task.text()) {
            joiner.add(s);
        }                
        taskTextLabel.setText(joiner.toString());

        taskBox.getChildren().addAll(taskTimeLabel, taskTextLabel);
        
        return taskBox;
    }
    
    @Override
    protected Pane createBeamMessageWindowContentPane() {  
        HBox contentPane = new HBox();
        contentPane.setAlignment(Pos.CENTER);
        
        contentPane.getChildren().addAll(
                this.createPicture(), 
                this.createTasksListView());
        
        return contentPane;
    }
    
    private Node createPicture() {
        ImageView picture = new ImageView(this.resources.taskImage());
        return picture;
    }
    
    private ListView<HBox> createTasksListView() {
        ListView<HBox> listView = new ListView<>();
        this.taskList = observableArrayList();
        listView.setPrefWidth(400);
        
        listView.setPrefHeight(300);
        listView.setItems(this.taskList);
        
        return listView;
    }
}
