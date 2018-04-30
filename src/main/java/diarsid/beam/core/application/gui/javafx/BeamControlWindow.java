/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

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

import diarsid.beam.core.application.gui.javafx.console.JavaFXConsolePlatformWindow;
import diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenu;

import static java.util.Objects.nonNull;

import static javafx.scene.input.ContextMenuEvent.CONTEXT_MENU_REQUESTED;

import static diarsid.beam.core.Beam.beamRuntime;
import static diarsid.beam.core.application.gui.javafx.MouseClickNotDragDetector.smartClickDetectionOn;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDoIndependently;
import static diarsid.beam.core.base.util.JavaFXUtil.screenHeight;

/**
 *
 * @author Diarsid
 */
class BeamControlWindow {
    
    private final WindowMover windowMover;
    private final GuiJavaFXResources resources;
    private BeamContextMenu beamContextMenu;
    
    private Stage stage;
    private Pane outerPane;
    
    private JavaFXConsolePlatformWindow consoleWindow;

    BeamControlWindow(
            BeamHiddenRoot beamHiddenRootWindow,
            GuiJavaFXResources resources) {
        this.resources = resources;
        this.windowMover = new WindowMover();
        
        Platform.runLater(() -> {
            this.createStage(beamHiddenRootWindow);            
            this.setSizeAndPosition();
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
        this.consoleWindow = consoleWindow;
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
        if ( nonNull(this.consoleWindow) ) {
            if ( this.beamContextMenu.javaFxContextMenu().isShowing() ) {
                this.beamContextMenu.javaFxContextMenu().hide();
                event.consume();
            } else {
                this.consoleWindow.touched();
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

    private void setSizeAndPosition() {
//        this.stage.setMinWidth(40);
//        this.stage.setMinHeight(40);
//        this.stage.setMaxWidth(40);
//        this.stage.setMaxHeight(40);
        
        double screenHeight = screenHeight();
        
        double distance = screenHeight / 5;
        this.stage.setX(distance);
        this.stage.setY(screenHeight - distance);
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
        label.setMinHeight(20);
        label.setMinWidth(20);
        label.setMaxHeight(20);
        label.setMaxWidth(20);
        
        innerBox.getChildren().add(label);
        
        outerBox.getChildren().add(innerBox);
        
        this.outerPane = outerBox;
    }
    
    private void createControlContextMenu() {
        this.beamContextMenu = new BeamContextMenu();
        
        MenuItem menuItem = new MenuItem("exit");        
        menuItem.getStyleClass().add("console-menu-item");
        menuItem.setOnAction((event) -> {
            asyncDoIndependently(
                    "Async Beam termination Thread", 
                    () -> {
                        beamRuntime().exitBeamCoreNow();
                    });
        });
        menuItem.setGraphic(this.beamContextMenu.createStandardMenuItemGraphic());
        this.beamContextMenu.registerJavaFxItem(menuItem);
        
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
