/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.server.modules.io.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.modules.io.gui.javafx.PopupWindow;
import com.drs.beam.server.modules.io.gui.javafx.TaskWindow;

/*
 * Main class for JavaFX based GUI.
 * Used as 'JavaFX Application Thread'. 
 * Runs appropriate windows for task, message or exception.
 */
public class GuiJavaFX extends Application implements Gui, Runnable{
       
    private static final GuiJavaFX guiInstance;
    
    static {
        guiInstance = new GuiJavaFX();
        new Thread(guiInstance, "JavaFX Application Thread").start();
    }
    
    private final GuiWindowsController windowsController = new GuiWindowsController();
    private final String messageIcon = "message_ico.jpeg";
    private final String errorIcon = "exception_ico.jpeg";
    private final String messageImage = "message.jpeg";
    private final String errorImage = "exception.jpeg";
    
    static GuiJavaFX getJavaFXGui(){
        return GuiJavaFX.guiInstance;
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
        Platform.runLater(new TaskWindow(task));
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
