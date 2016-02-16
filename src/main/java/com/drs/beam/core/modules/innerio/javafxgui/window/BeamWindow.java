package com.drs.beam.core.modules.innerio.javafxgui.window;

import com.drs.beam.core.modules.innerio.javafxgui.WindowController;
import com.drs.beam.core.modules.innerio.javafxgui.WindowPosition;
import com.drs.beam.core.modules.innerio.javafxgui.WindowResourcesProvider;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Diarsid
 */
abstract class BeamWindow {
    
    private Stage stage;
    private final WindowController controller;
    private final WindowResourcesProvider resources;
    
    BeamWindow(WindowController c, WindowResourcesProvider p) {
        this.controller = c;
        this.resources = p;
    }
    
    void prepareStage() {
        this.stage = new Stage();
        this.stage.initStyle(StageStyle.TRANSPARENT);
        this.stage.setAlwaysOnTop(true);
        this.stage.setMinWidth(300);
        this.stage.setMinHeight(200);
        this.stage.setResizable(false);
    }
        
    private void setActionOnClose() {
        this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                stage.close();
                controller.windowClosed();
            }
        });
    }
    
    private void setPosition() {
        WindowPosition position = controller.getNewWindowPosition();
        if ( position.getX() != 0 ) {
            this.stage.setX(position.getX());
            this.stage.setY(position.getY());
        }
    }    
        
    final void closeThis() {
        this.stage.close();
        this.controller.windowClosed();
    }
    
    final void setContent(Pane contentPane) {
        Pane main = new Pane();
        main.setId("main");
        main.getChildren().add(contentPane);
        Scene scene = new Scene(main);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(resources.getPathToCssFile());
        this.stage.setScene(scene);
        this.stage.sizeToScene();
    }
    
    final void showThis() {
        this.setActionOnClose();        
        this.setPosition();
        
        this.stage.show();
        this.controller.reportLastWindowPosition(stage.getX(), stage.getY());
    }
    
    final void setTitle(String title) {
        this.stage.setTitle(title);
    }
    
    final void setIconUrl(String iconUrl) {
        this.stage.getIcons().add(new Image("file:"+iconUrl));
    }
    
    DropShadow buttonShadow() {
        return this.resources.getButtonShadow();
    }
    
    WindowResourcesProvider settings() {
        return this.resources;
    }
    
    Button newOkButton(String text) {
        Button button = new Button(text);    
        button.setMinWidth(100);
        button.setMinHeight(30);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                closeThis();
            }
        });     
        
        button.setOnMouseEntered(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {                
                button.setEffect(resources.getButtonShadow());
            }
        });
        
        button.setOnMouseExited(new EventHandler<MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                button.setEffect(null);
            }
        });
        
        return button;
    }
}
