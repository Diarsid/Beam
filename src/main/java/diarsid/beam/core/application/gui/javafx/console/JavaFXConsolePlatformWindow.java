/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.gui.javafx.GuiJavaFXResources;
import diarsid.beam.core.application.gui.javafx.WindowMover;
import diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenu;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.modules.DataModule;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;
import static javafx.stage.WindowEvent.WINDOW_HIDDEN;
import static javafx.stage.WindowEvent.WINDOW_SHOWN;

import static diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenuItem.createStandardMenuItemGraphic;
import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;

/**
 *
 * @author Diarsid
 */
public class JavaFXConsolePlatformWindow 
        extends ConsolePlatform 
        implements ContextControlableConsole {
    
    private final GuiJavaFXResources windowResources;
    private final WindowMover windowMover;
    private final ConsoleWindowResizer windowResizer;
    private final ConsoleWindowBlockingIO blockingIo;
    private final ConsoleInputPersistentBuffer consoleInputBuffer;
    private final Runnable runnableLaunch;
    private final AtomicBoolean isShown;
    private final ConsoleTextArea consoleTextArea;
    private final BeamContextMenu contextMenu;
            
    private Stage stage;
    private Pane bar;
    private Pane mainArea;
    
    private boolean ready;

    JavaFXConsolePlatformWindow(
            GuiJavaFXResources javaFxResources, 
            ConsoleWindowBlockingIO consoleBlockingIo,
            ConsoleBlockingExecutor blockingExecutor,
            ConsoleInputPersistentBuffer consoleInputPersistentBuffer) {
        super(consoleBlockingIo, blockingExecutor, IN_MACHINE);
        this.windowResources = javaFxResources;
        this.windowMover = new WindowMover();
        this.windowResizer = new ConsoleWindowResizer();
        this.blockingIo = consoleBlockingIo;
        this.consoleInputBuffer = consoleInputPersistentBuffer;
        this.ready = false;
        this.isShown = new AtomicBoolean(false);
        
        this.runnableLaunch = () -> {
            if ( this.ready ) {
                this.showAndThrowOnTop();
            } else {
                this.init();
                this.showAndThrowOnTop();
            }
        };
        
        this.contextMenu = new BeamContextMenu(); 
        MenuItem closeMenuItem = new MenuItem("close");        
        closeMenuItem.getStyleClass().add("console-menu-item");
        closeMenuItem.setGraphic(createStandardMenuItemGraphic());
        closeMenuItem.setOnAction(event -> {
            this.hide();
        });
        this.contextMenu.registerJavaFxItem(closeMenuItem);
        this.contextMenu.registerBeamItems(
                new ConsoleContextMenuItemForSnippet(this, this.contextMenu), 
                new ConsoleContextMenuItemForClear(this, this.contextMenu),
                new ConsoleContextMenuItemForDefaultSize(this, this.contextMenu));
        this.consoleTextArea = new ConsoleTextArea(this);
    }
    
    final ConsoleWindowBlockingIO blockingIo() {
        return this.blockingIo;
    }
    
    final ConsoleInputPersistentBuffer inputBuffer() {
        return this.consoleInputBuffer;
    }
    
    public final void touched() {
        if ( this.isShown.get() ) {
            this.throwOnTop();
        } else {
            this.showAndThrowOnTop();
        }
    }
    
    private void throwOnTop() {
        this.stage.setAlwaysOnTop(true);
        this.stage.setAlwaysOnTop(false);
        this.consoleTextArea.requestFocus();
    }
    
    public static JavaFXConsolePlatformWindow createAndLaunchJavaFXConsolePlatform(
            DataModule dataModule, 
            GuiJavaFXResources resources, 
            ConsoleBlockingExecutor blockingExecutor) {
        ConsoleWindowBlockingIO consoleIo = new ConsoleWindowBlockingIO();
        ConsoleInputPersistentBuffer consoleInputPersistentBuffer = 
                new ConsoleInputPersistentBuffer(50, dataModule.keyValues());
        JavaFXConsolePlatformWindow consoleWindow = new JavaFXConsolePlatformWindow(
                resources, consoleIo, blockingExecutor, consoleInputPersistentBuffer);
        consoleWindow.launch();
        return consoleWindow;
    }
    
    private void launch() {
        Platform.runLater(this.runnableLaunch);
    }
    
    private void showAndThrowOnTop() {
        this.stage.show();
        this.throwOnTop();
    }
    
    private void init() {
        this.createStage();
        this.createBar();
        this.createMainArea();
        this.fillStageWithScene();
    }
    
    private void hideInternally() {
        this.stage.hide();
    }
    
    private void fillStageWithScene() {
        this.stage.setScene(this.createConsoleWindowScene());
        this.stage.sizeToScene();
        this.ready = true;
    }
    
    private Scene createConsoleWindowScene() {      
        VBox consoleInnerBox = new VBox();
        consoleInnerBox.setAlignment(TOP_CENTER);        
        consoleInnerBox.getChildren().addAll(this.bar, this.mainArea);
        consoleInnerBox.getStyleClass().add("console-inner-box");
        this.windowResizer.listen(consoleInnerBox);     
        
        VBox consoleOuterBox = new VBox();
        consoleOuterBox.getStyleClass().add("console-outer-box");        
        consoleOuterBox.setAlignment(TOP_CENTER);        
        consoleOuterBox.setEffect(this.windowResources.opacityBlackShadow());
        
        consoleOuterBox.getChildren().addAll(consoleInnerBox);
        
        Scene scene = new Scene(consoleOuterBox);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.windowResources.cssFilePath());
        
        return scene;
    }
    
    private void createStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setResizable(true);
        this.stage.centerOnScreen();
        this.stage.setOnCloseRequest((windowEvent) -> {
            this.hideInternally();
        });
        this.windowMover.acceptStage(this.stage);
        this.windowResizer.acceptStage(this.stage);
        
        this.stage.addEventHandler(WINDOW_HIDDEN, (windowEvent) -> {
            this.isShown.set(false);
        });
        
        this.stage.addEventHandler(WINDOW_SHOWN, (windowEvent) -> {            
            this.isShown.set(true);
        });
    }
    
    private void createBar() {
        HBox barBox = new HBox(5);
        barBox.getStyleClass().add("console-bar");
        barBox.setAlignment(CENTER_LEFT);
        barBox.getChildren().addAll(this.createBarPoint(), this.createBarLabel());
        barBox.setPadding(new Insets(0, 3, 3, 0));
        this.windowMover.boundTo(barBox);        
        this.bar = barBox;
    }
    
    private Label createBarLabel() {
        Label barHeader = new Label("Beam > Console");
        barHeader.getStyleClass().add("console-bar-header");
        return barHeader;
    }
    
    private Label createBarPoint() {
        Label point = new Label();
        
        point.setMaxHeight(14);
        point.setMinHeight(14);
        point.setMaxWidth(14);
        point.setMinWidth(14);
        point.setStyle(
                "-fx-background-color: white; " +
                "-fx-background-radius: 14px; " +
                "-fx-border-color: #FFDE00; " + 
                "-fx-border-width: 4px; " +
                "-fx-border-radius: 14px; ");
        
        return point;
    }
    
    private void createMainArea() {
        VBox mainAreaVBox = new VBox();
        mainAreaVBox.getStyleClass().add("console-main-area");
        TextArea textArea = this.consoleTextArea.jfxTextArea();                
        this.windowResizer.affect(textArea); 
        textArea.setContextMenu(this.contextMenu.javaFxContextMenu());
        mainAreaVBox.getChildren().add(textArea);
        this.mainArea = mainAreaVBox;
        this.consoleTextArea.startListenBlockingConsoleIncome();
    }

    @Override
    public String name() {
        return "JavaFX Console";
    }

    @Override
    public void whenStopped() {
        Platform.runLater(() -> {
            this.hideInternally(); 
        });
    }

    @Override
    public void whenInitiatorAccepted() {
        // TODO ?
    }

    @Override
    public boolean isActiveWhenClosed() {
        return true;
    }

    @Override
    public void imitateCommandInput(String command) {
        this.consoleTextArea.imitateCommandInput(command);
    }

    @Override
    public String text() {
        return this.consoleTextArea.text();
    }

    @Override
    public int caretPosition() {
        return this.consoleTextArea.caretPosition();
    }

    @Override
    public boolean isInDialog() {
        return super.isInteractionLasts();
    }

    @Override
    public void clear() {
        this.consoleTextArea.clear();   
    }
    
    @Override
    public void hide() {
        this.hideInternally();
    }
    
    @Override
    public void toDefaultSize() {
        this.windowResizer.affectableToDefaultSize();
    }

    @Override
    public boolean hasClearableContent() {
        return this.consoleTextArea.hasClearableContent();
    }

    @Override
    public boolean hasNonDefaultSize() {
        return this.windowResizer.hasNonDefaultSize();
    }
}
