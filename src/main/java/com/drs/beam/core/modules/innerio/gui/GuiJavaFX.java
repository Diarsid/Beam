/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.innerio.gui;

import com.drs.beam.core.modules.io.Gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.modules.innerio.gui.javafx.PopupWindow;
import com.drs.beam.core.modules.innerio.gui.javafx.TaskWindow;

/*
 * Main class for JavaFX based GUI.
 * Used as 'JavaFX Application Thread'. 
 * Runs appropriate windows for task, message or exception.
 */
public class GuiJavaFX extends Application implements Gui, Runnable{
    
    private static final GuiJavaFX gui;
    
    static {
        gui = new GuiJavaFX();
        new Thread(gui, "JavaFX Application Thread").start();
    }
        
    private final GuiWindowsController windowsController = new GuiWindowsController();
    
    private String taskIcon;
    private String messageIcon;
    private String errorIcon;
    private String taskImage;
    private String messageImage;
    private String errorImage;
    
    public static GuiJavaFX buildAndLaunchGui(String imagesLocation){
        gui.setImagesLocation(imagesLocation);
        return gui;
    }
    
    private void setImagesLocation(String imagesLocation){
        this.taskIcon = imagesLocation + "task_ico.jpeg";
        this.messageIcon = imagesLocation + "message_ico.jpeg";
        this.errorIcon = imagesLocation + "exception_ico.jpeg";
        this.taskImage = imagesLocation + "task.jpeg";
        this.messageImage = imagesLocation + "message.jpeg";
        this.errorImage = imagesLocation + "exception.jpeg";
    }
    
    @Override
    public void start(Stage stage) { 
        Platform.setImplicitExit(false);
    }
    
    @Override
    public void run(){
        Application.launch();
    }
    
    @Override
    public void showTask(Task task){
        TaskWindow window = new TaskWindow(
                task,
                this.taskImage,
                this.taskIcon,
                this.windowsController);
        Platform.runLater(window);
    }
    
    @Override
    public void showMessage(String[] message){
        PopupWindow window = new PopupWindow(
                "Message", 
                this.messageImage, 
                this.messageIcon, 
                message, 
                this.windowsController);
        Platform.runLater(window);
    } 
    
    @Override
    public void showError(String[] error){
        PopupWindow window = new PopupWindow(
                "Error", 
                this.errorImage, 
                this.errorIcon, 
                error, 
                this.windowsController);
        Platform.runLater(window);
    }
    
    @Override
    public void exitAfterAllWindowsClosed(){
        this.windowsController.setExitAfterAllWindowsClosed();
    }
    
    public GuiWindowsController getController(){
        return this.windowsController;
    }
}
