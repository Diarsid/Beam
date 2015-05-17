/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.io.jfxgui;

import com.drs.beam.io.jfxgui.window.ExceptionWindow;
import com.drs.beam.io.jfxgui.window.MessageWindow;
import com.drs.beam.io.jfxgui.window.TaskWindow;
import com.drs.beam.tasks.Task;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/*
 * Main class for JavaFX based GUI.
 * Used for 'JavaFX Application Thread' starting. 
 * Runs appropriate windows for task, message or exception.
 */
public class GuiEngine extends Application implements Gui{
    // Fields =============================================================================    
    
    // Constructors =======================================================================
    public GuiEngine(){
    }

    // Methods ============================================================================
    
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
    public void showMessage(String message){
        Platform.runLater(new MessageWindow(message));
    } 
    
    @Override
    public void showException(Exception e){
        Platform.runLater(new ExceptionWindow(e));
    }
}
