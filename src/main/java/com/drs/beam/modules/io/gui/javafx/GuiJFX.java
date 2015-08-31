/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.io.gui.javafx;

import com.drs.beam.modules.io.gui.Gui;
import com.drs.beam.modules.io.gui.javafx.ExceptionWindowFX;
import com.drs.beam.modules.io.gui.javafx.MessageWindowFX;
import com.drs.beam.modules.io.gui.javafx.TaskWindowFX;
import com.drs.beam.modules.tasks.Task;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/*
 * Main class for JavaFX based GUI.
 * Used as 'JavaFX Application Thread'. 
 * Runs appropriate windows for task, message or exception.
 */
public class GuiJFX extends Application implements Gui, Runnable{
    
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
        Platform.runLater(new TaskWindowFX(task)); 
    }
    
    @Override
    public void showMessage(String message, boolean isCritical){
        Platform.runLater(new MessageWindowFX(message, isCritical));
    } 
    
    @Override
    public void showException(Exception e, boolean isCritical){
        Platform.runLater(new ExceptionWindowFX(e, isCritical));
    }
}
