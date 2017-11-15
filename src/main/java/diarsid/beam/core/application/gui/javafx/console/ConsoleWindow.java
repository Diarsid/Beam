/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.gui.javafx.WindowMover;
import diarsid.beam.core.application.gui.javafx.WindowResources;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;

/**
 *
 * @author Diarsid
 */
class ConsoleWindow 
        extends 
                ConsolePlatform
        implements 
                Runnable {
    
    private final WindowResources windowResources;
    private final WindowMover windowMover;
    private final ConsoleBlockingOutcome fromConsoleOutcome;
    private final ConsoleBlockingIncome intoConsoleIncome;
    private Stage stage;
    private Pane bar;
    private Pane mainArea;
    private boolean ready;

    ConsoleWindow(
            WindowResources windowResources, 
            ConsoleBlockingOutcome fromConsoleReader, 
            ConsoleBlockingIncome intoConsoleIncome,
            ConsoleBlockingExecutor blockingExecutor) {
        super(intoConsoleIncome, fromConsoleReader, blockingExecutor, IN_MACHINE);
        this.windowResources = windowResources;
        this.windowMover = new WindowMover();
        this.fromConsoleOutcome = fromConsoleReader;
        this.intoConsoleIncome = intoConsoleIncome;
        this.ready = false;
    }
    
    public static ConsolePlatform createAndLaunchJavaFXConsolePlatform(
            WindowResources windowResources, ConsoleBlockingExecutor blockingExecutor) {
        ConsoleBlockingOutcome fromConsole = new ConsoleBlockingOutcome();
        ConsoleBlockingIncome intoConsole = new ConsoleBlockingIncome();
        ConsoleWindow consoleWindow = new ConsoleWindow(
                windowResources, fromConsole, intoConsole, blockingExecutor);
        Platform.runLater(consoleWindow);
        return consoleWindow;
    }
    
    @Override
    public void run() {
        if ( this.ready ) {
            this.show();
        } else {
            this.initAndShow();
        }
    }
    
    private void show() {
        this.stage.show();
    }
    
    private void hide() {
        // TODO HIGH
    }
    
    private void initAndShow() {
        this.createStage();
        this.createBar();
        
        VBox mainVBox = new VBox();
        mainVBox.setStyle("-fx-background-color: transparent; ");
        mainVBox.setAlignment(Pos.BOTTOM_RIGHT);
//        mainVBox.getChildren().addAll(this.controlPane, this.screenCapturePane);
        
        Scene scene = new Scene(mainVBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.getPathToCssFile());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
        this.ready = true;
        
        this.show();
    }
    
    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setMinWidth(400);
        this.stage.setMinHeight(300);
//        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.windowMover.acceptStage(this.stage);
    }
    
    private void createBar() {
        
    }
    
    private void createMainArea() {
        
    }

    @Override
    public String name() {
        return "Native JavaFX Console Platform";
    }

    @Override
    public void stop() {
        this.hide();
    }

    @Override
    public void whenInitiatorAccepted() {
        // TODO ?
    }
}
