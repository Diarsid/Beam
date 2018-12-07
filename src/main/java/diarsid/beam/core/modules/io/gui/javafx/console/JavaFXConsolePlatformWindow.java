/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

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

import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.modules.DataModule;
import diarsid.beam.core.modules.io.gui.javafx.GuiJavaFX;
import diarsid.beam.core.modules.io.gui.javafx.GuiJavaFXResources;
import diarsid.beam.core.modules.io.gui.javafx.WindowMover;
import diarsid.beam.core.modules.io.gui.javafx.WindowPersistableFrame;
import diarsid.beam.core.modules.io.gui.javafx.contexmenu.BeamContextMenu;
import diarsid.support.configuration.Configuration;

import static java.lang.Integer.max;

import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;
import static javafx.stage.WindowEvent.WINDOW_HIDDEN;
import static javafx.stage.WindowEvent.WINDOW_SHOWN;

import static diarsid.beam.core.base.control.io.base.actors.OuterIoEngineType.IN_MACHINE;
import static diarsid.beam.core.base.util.JavaFXUtil.setStageAtPoint;
import static diarsid.beam.core.modules.io.gui.javafx.contexmenu.BeamContextMenuItem.createStandardMenuItemGraphic;

/**
 *
 * @author Diarsid
 */
public class JavaFXConsolePlatformWindow 
        extends ConsolePlatform 
        implements ContextControlableConsole {
    
    private static final int MIN_CONSOLE_WIDTH = 500;
    private static final int MIN_CONSOLE_HEIGHT = 300;
    
    private final GuiJavaFXResources windowResources;
    private final WindowMover windowMover;
    private final DragRegionResizer windowResizer;
    private final Runnable runnableLaunch;
    private final AtomicBoolean isShown;
    private final ConsoleTextArea consoleTextArea;
    private final BeamContextMenu contextMenu;
    private final int prefereableHeight;
    private final int prefereableWidth;
    private final WindowPersistableFrame persistableFrame;
            
    private Stage stage;
    private Pane bar;
    private Pane mainArea;
    private TextArea textArea;
    
    private boolean ready;

    JavaFXConsolePlatformWindow(
            GuiJavaFXResources javaFxResources, 
            ConsoleWindowBlockingIO consoleBlockingIo,
            ConsoleBlockingExecutor blockingExecutor, 
            ConsoleTextArea consoleTextArea,
            WindowPersistableFrame persistableFrame) {
        super(consoleBlockingIo, blockingExecutor, IN_MACHINE);          
        this.persistableFrame = persistableFrame;
        this.windowResources = javaFxResources;
        this.consoleTextArea = consoleTextArea;
        
        this.windowMover = new WindowMover();        
        this.windowResizer = new DragRegionResizer();
        
        this.windowMover.afterMove((x, y) -> {
            this.persistableFrame.setAnchor(x, y);
        });
        this.windowResizer.afterResize((newWidth, newHeight) -> {
            this.persistableFrame.setSize(newWidth, newHeight);
        });
        
        this.ready = false;
        this.isShown = new AtomicBoolean(false);
        
        this.runnableLaunch = () -> {
            if ( this.ready ) {
                this.showAndThrowOnTop();
            } else {
                this.init();
                this.showAndThrowOnTop();
                this.onInitialShow();
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
        
        Configuration configuration = this.windowResources.configuration();
        
        int width = MIN_CONSOLE_WIDTH;
        if ( configuration.hasInt("ui.console.default.width") ) {
            int configuredWidth = configuration.asInt("ui.console.default.width");
            width = max(configuredWidth, MIN_CONSOLE_WIDTH);
        }
        this.prefereableWidth = width;    
        
        int height = MIN_CONSOLE_HEIGHT;
        if ( configuration.hasInt("ui.console.default.height") ) {
            int configuredHeight = configuration.asInt("ui.console.default.height");
            height = max(configuredHeight, MIN_CONSOLE_HEIGHT);
        }
        this.prefereableHeight = height;
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
            GuiJavaFX gui,
            GuiJavaFXResources resources, 
            ConsoleBlockingExecutor blockingExecutor) 
            throws DataExtractionException {
        ConsoleWindowBlockingIO consoleBlockingIo = new ConsoleWindowBlockingIO();
        ConsoleInputBufferFilter bufferFilter = new ConsoleInputBufferFilter();
        ConsoleInputPersistentBuffer consoleInputPersistentBuffer = 
                new ConsoleInputPersistentBuffer(50, dataModule.keyValues(), bufferFilter, gui);
        ConsoleTextArea consoleTextArea = new ConsoleTextArea(
                consoleBlockingIo, 
                consoleInputPersistentBuffer);
        JavaFXConsolePlatformWindow consoleWindow = new JavaFXConsolePlatformWindow(
                resources, 
                consoleBlockingIo, 
                blockingExecutor, 
                consoleTextArea, 
                gui.persistableFrameManager().get("JavaFX Beam Console Window"));
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
    
    private void onInitialShow() {
        if ( this.persistableFrame.isTransient() ) {
            this.persistableFrame.setAnchor(this.stage.getX(), this.stage.getY());
            this.persistableFrame.setSize(prefereableWidth, prefereableHeight);
        }
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
        consoleInnerBox.getStyleClass().add("console-inner-box");        
        consoleInnerBox.getChildren().addAll(this.bar, this.mainArea);
        
        this.bar.prefWidthProperty().bind(consoleInnerBox.widthProperty());
        
        this.windowResizer.makeResiziable(consoleInnerBox);     
        
        consoleInnerBox.setMinHeight(this.prefereableHeight);
        consoleInnerBox.setMinWidth(this.prefereableWidth);
        this.persistableFrame.setMinSize(this.prefereableWidth, this.prefereableHeight);
        
        this.mainArea.prefWidthProperty().bind(consoleInnerBox.widthProperty());
        this.mainArea.prefHeightProperty().bind(consoleInnerBox.heightProperty());
        this.textArea.prefWidthProperty().bind(consoleInnerBox.widthProperty());
        this.textArea.prefHeightProperty().bind(consoleInnerBox.heightProperty());
        
        if ( this.persistableFrame.isPersistent() ) {
            double newWidth = this.persistableFrame.size().width();
            double newHeight = this.persistableFrame.size().height();
            boolean needToUpdateFrame = false;
            
            if ( newWidth < this.prefereableWidth ) {
                newWidth = this.prefereableWidth;
                needToUpdateFrame = true;
            }
            
            if ( newHeight < this.prefereableHeight ) {
                newHeight = this.prefereableHeight;
                needToUpdateFrame = true;
            } 
            
            if ( needToUpdateFrame ) {
                this.persistableFrame.setSize(newWidth, newHeight);
            }
            consoleInnerBox.setPrefWidth(newWidth);
            consoleInnerBox.setPrefHeight(newHeight);
        } else {
            this.persistableFrame.setSize(this.prefereableWidth, this.prefereableHeight);
        }
        
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
        if ( this.persistableFrame.isPersistent() ) {
            setStageAtPoint(this.stage, persistableFrame.anchor());
        } else {
            this.stage.centerOnScreen();
        }        
        this.stage.setOnCloseRequest((windowEvent) -> {
            this.hideInternally();
        });
        this.windowMover.acceptStage(this.stage);
        this.windowResizer.acceptSizeToScene(() -> this.stage.sizeToScene());
        
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
        
        this.textArea = new TextArea();  
        
        this.textArea.getStyleClass().add("console-text-area"); 
        
        this.consoleTextArea.setTextArea(this.textArea);       
        this.textArea.setContextMenu(this.contextMenu.javaFxContextMenu());
        mainAreaVBox.getChildren().add(this.textArea);
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
        this.windowResizer.toDefaultSize();
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
