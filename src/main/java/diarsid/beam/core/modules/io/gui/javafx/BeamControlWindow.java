/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import diarsid.beam.core.application.starter.Launcher;
import diarsid.beam.core.modules.io.gui.javafx.console.JavaFXConsolePlatformWindow;
import diarsid.beam.core.modules.io.gui.javafx.contexmenu.BeamContextMenu;
import diarsid.support.objects.Possible;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import static javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED;

import static diarsid.beam.core.Beam.beamRuntime;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.JavaFXUtil.screenHeight;
import static diarsid.beam.core.base.util.JavaFXUtil.setStageAtPoint;
import static diarsid.beam.core.modules.io.gui.javafx.MouseClickNotDragDetector.smartClickDetectionOn;
import static diarsid.beam.core.modules.io.gui.javafx.contexmenu.BeamContextMenuItem.createStandardMenuItemGraphic;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class BeamControlWindow {
    
    private static final int CONTOL_WINDOW_SIZE;
    
    static {
        CONTOL_WINDOW_SIZE = 20;
    }
    
    private final WindowMover windowMover;
    private final GuiJavaFXResources resources;
    private final Possible<Boolean> showConsoleOnClick;
    private final Possible<JavaFXConsolePlatformWindow> consoleWindow;
    private final Runnable asyncLaunchSystemConsole;
    private BeamContextMenu beamContextMenu;
    
    private Stage stage;
    private Pane outerPane;

    BeamControlWindow(
            WindowPersistableFrame persistableFrame,
            BeamHiddenRoot beamHiddenRootWindow,
            GuiJavaFXResources resources,
            Launcher launcher) {
        this.resources = resources;
        this.windowMover = new WindowMover();
        this.windowMover.afterMove((x, y) -> {
            persistableFrame.setAnchor(x, y);
        });
        this.showConsoleOnClick = this.resources
                .configuration()
                .possibleBoolean("ui.console.showOnControlClick")
                .orDefault(true);
        this.consoleWindow = possibleButEmpty();
        
        this.asyncLaunchSystemConsole = () -> {
            asyncDo(() -> launcher.executeSysConsoleScript());
        };
        
        if ( persistableFrame.isTransient() ) {
            persistableFrame.setSize(CONTOL_WINDOW_SIZE, CONTOL_WINDOW_SIZE);
        }
        
        Platform.runLater(() -> {
            this.createStage(beamHiddenRootWindow);            
            this.setStagePosition(persistableFrame);
            this.createContent();
            this.createControlContextMenu();
            this.setScene();
            this.setStageMoveable();
            this.setLifecycleCallbacks();
            this.stage.show();
        });
    }
    
    void setAlwaysOnTop() {
        this.stage.setAlwaysOnTop(true);
    }
    
    void setConsoleWindow(JavaFXConsolePlatformWindow consoleWindow) {
        this.consoleWindow.resetTo(consoleWindow);
    }

    private void setLifecycleCallbacks() {
        this.stage.setOnCloseRequest((event) -> {
            // do nothing
        });
        
        smartClickDetectionOn(this.outerPane)
                .withPressedDurationTreshold(150)
                .setOnMouseClickNotDrag((mouseEvent) -> {
                    this.dispatchByClickedButton(mouseEvent);
                })
                .setOnMouseDoubleClick((mouseEvent) -> {
                    
                });
    }

    private void dispatchByClickedButton(MouseEvent mouseEvent) {
        switch ( mouseEvent.getButton() ) {
            case PRIMARY : {
                this.onLeftMouseClicked(mouseEvent);
                break;
            }
            case SECONDARY : {
                this.onRightMouseClicked(mouseEvent);
                break;
            }
            case MIDDLE :
            case NONE :
            default : {
                
            }
        }
    }
    
    private void onLeftMouseClicked(MouseEvent event) {
        if ( this.beamContextMenu.javaFxContextMenu().isShowing() ) {
            this.beamContextMenu.javaFxContextMenu().hide();
            event.consume();
        } else {
            if ( this.consoleWindow.isPresent() && this.showConsoleOnClick.equalTo(TRUE) ) {
                this.consoleWindow.orThrow().touched();
            }
        }
    }
    
    private void onRightMouseClicked(MouseEvent mouseEvent) {
        
    }

    private void createStage(BeamHiddenRoot beamHiddenRoot) {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.initOwner(beamHiddenRoot.newHiddenStage());
    }

    private void setStagePosition(WindowPersistableFrame persistableFrame) {
        if ( persistableFrame.isPersistent() ) {
            setStageAtPoint(this.stage, persistableFrame.anchor());
        } else {
            double screenHeight = screenHeight();        
            double distance = screenHeight / 5;
            
            double x = distance;
            double y = screenHeight - distance;
            this.stage.setX(x);
            this.stage.setY(y);
            persistableFrame.setAnchor(x, y);
        }
    }

    private void createContent() {
        VBox outerBox = new VBox();
        outerBox.getStyleClass().add("control-window-outer-box");
        
        VBox innerBox = new VBox();
        innerBox.getStyleClass().add("control-window-inner-box");
        innerBox.setAlignment(Pos.CENTER);
        
        Label label = new Label();
        label.setStyle(
                "-fx-background-color: yellow; -fx-background-radius: 10px; " +
                "-fx-border-color: orange; -fx-border-radius: 10px; " +
                "-fx-border-width: 4px;");
        label.setMinHeight(CONTOL_WINDOW_SIZE);
        label.setMinWidth(CONTOL_WINDOW_SIZE);
        label.setMaxHeight(CONTOL_WINDOW_SIZE);
        label.setMaxWidth(CONTOL_WINDOW_SIZE);
        
        innerBox.getChildren().add(label);
        
        outerBox.getChildren().add(innerBox);
        
        this.outerPane = outerBox;
    }
    
    private void createControlContextMenu() {
        this.beamContextMenu = new BeamContextMenu();
        
        MenuItem exitItem = new MenuItem("exit");        
        exitItem.getStyleClass().add("console-menu-item");
        exitItem.setOnAction((event) -> {
            asyncDoIndependently(
                    "Async Beam termination Thread", 
                    () -> {
                        beamRuntime().exitBeamCoreNow();
                    });
        });
        exitItem.setGraphic(createStandardMenuItemGraphic());
        
        if ( this.showConsoleOnClick.equalTo(FALSE) ) {
            MenuItem javaFxConsoleItem = new MenuItem("JavaFX console");
            javaFxConsoleItem.getStyleClass().add("console-menu-item");
            javaFxConsoleItem.setOnAction((event) -> {
                if ( this.consoleWindow.isPresent() ) {
                    this.consoleWindow.orThrow().touched();
                }
            });
            javaFxConsoleItem.setGraphic(createStandardMenuItemGraphic());
            this.beamContextMenu.registerJavaFxItem(javaFxConsoleItem);
        }
        
        MenuItem systemConsoleItem = new MenuItem("System console");        
        systemConsoleItem.getStyleClass().add("console-menu-item");
        systemConsoleItem.setOnAction((event) -> {
            this.asyncLaunchSystemConsole.run();
        });
        systemConsoleItem.setGraphic(createStandardMenuItemGraphic());
        
        this.beamContextMenu.registerJavaFxItem(systemConsoleItem);
        this.beamContextMenu.registerJavaFxItem(exitItem);
        
        ContextMenu javaFxContextMenu = this.beamContextMenu.javaFxContextMenu();
        
        this.outerPane.addEventHandler(CONTEXT_MENU_REQUESTED, (event) -> {
            javaFxContextMenu.hide();
            javaFxContextMenu.show(
                    this.outerPane, 
                    event.getScreenX(), 
                    event.getScreenY());
            event.consume();
        });        
    }

    private void setScene() {
        Scene scene = new Scene(this.outerPane);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(this.resources.cssFilePath());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
    }

    private void setStageMoveable() {
        this.windowMover.acceptStage(this.stage);
        this.windowMover.boundTo(this.outerPane);
    }
    
}
