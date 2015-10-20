/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.io.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.drs.beam.core.entities.Task;
import com.drs.beam.core.modules.io.gui.javafx.PopupWindow;
import com.drs.beam.core.modules.io.gui.javafx.TaskWindow;

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
    private final String taskIcon = Gui.IMAGES_LOCATION+"task_ico.jpeg";
    private final String messageIcon = Gui.IMAGES_LOCATION+"message_ico.jpeg";
    private final String errorIcon = Gui.IMAGES_LOCATION+"exception_ico.jpeg";
    private final String taskImage = Gui.IMAGES_LOCATION+"task.jpeg";
    private final String messageImage = Gui.IMAGES_LOCATION+"message.jpeg";
    private final String errorImage = Gui.IMAGES_LOCATION+"exception.jpeg";
    
    public static GuiJavaFX buildAndLaunchGui(){
        return gui;
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
