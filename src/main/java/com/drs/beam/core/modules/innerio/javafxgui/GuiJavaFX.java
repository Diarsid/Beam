/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.core.modules.innerio.javafxgui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import com.drs.beam.core.modules.innerio.Gui;
import com.drs.beam.core.modules.innerio.javafxgui.window.WindowsBuilderWorker;
import com.drs.beam.core.modules.tasks.TaskMessage;

/*
 * Main class for JavaFX based GUI.
 * Used as 'JavaFX Application Thread'. 
 * Runs appropriate windows for task, message or exception.
 */
class GuiJavaFX extends Application implements Gui {
        
    private final WindowController windowsController;  
    private final WindowSettingsProvider settingsProvider;
    private final WindowsBuilder windowsBuilder;
    
    public GuiJavaFX(String imagesLocation) {
        this.windowsController = new WindowController();
        this.settingsProvider = new WindowSettingsProvider(imagesLocation);
        this.windowsBuilder = new WindowsBuilderWorker();
    }
    
    @Override
    public void start(Stage stage) {         
    }
    
    public void go() {
        launch();
    }
    
    @Override
    public void showTask(TaskMessage task) {
        Runnable window = this.windowsBuilder.newTaskWindow(
                task,                    
                this.settingsProvider,
                this.windowsController);
        Platform.runLater(window);
    }
    
    @Override
    public void showMessage(String[] message) {
        Runnable window = this.windowsBuilder.newMessageWindow(
                message, 
                this.settingsProvider,
                this.windowsController);
        Platform.runLater(window);
    } 
    
    @Override
    public void showError(String[] error) {
        Runnable window = this.windowsBuilder.newErrorWindow(
                error, 
                this.settingsProvider,
                this.windowsController);
        Platform.runLater(window);
    }
    
    @Override
    public void exitAfterAllWindowsClosed() {
        this.windowsController.setExitAfterAllWindowsClosed();
    }
}
