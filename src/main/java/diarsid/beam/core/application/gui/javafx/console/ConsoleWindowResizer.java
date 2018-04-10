/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import diarsid.beam.core.application.gui.javafx.BorderDragType;

import static java.lang.String.format;

import static diarsid.beam.core.application.gui.javafx.BorderDragType.BOTTOM;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.BOTTOM_LEFT;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.BOTTOM_RIGHT;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.RIGHT;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.TOP_RIGHT;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.defineBorderDragType;

/**
 *
 * @author Diarsid
 */
class ConsoleWindowResizer {
    
    private Stage stage;
    private Region listenableRegion;
    private Region affectableRegion;
    
    private double initialMouseX;
    private double initialMouseY;
    private double initialAffectableRegionWidth;
    private double initialAffectableRegionHeight;
    
    private BorderDragType drag;

    ConsoleWindowResizer() {
    }
    
    void acceptStage(Stage stage) {
        this.stage = stage;
    }
    
    void affect(Region affectableRegion) {
        this.affectableRegion = affectableRegion;
    }
    
    boolean hasNonDefaultSize() {
        boolean hasDefaultWidth = this.affectableRegion.getMinWidth() == this.affectableRegion.getWidth();
        boolean hasDefaultHeight = this.affectableRegion.getMinHeight() == this.affectableRegion.getHeight();
        
        return ! ( hasDefaultHeight && hasDefaultWidth );                
    }
    
    void affectableToDefaultSize() {
        this.affectableRegion.setPrefWidth(this.affectableRegion.getMinWidth());
        this.affectableRegion.setPrefHeight(this.affectableRegion.getMinHeight());
        this.affectableRegion.autosize();
        this.stage.sizeToScene();
    }
    
    void listen(Region listenableRegion) {
        this.listenableRegion = listenableRegion;
        
        this.listenableRegion.setOnMousePressed(mouseEvent -> {
            this.doInitialMeasurements(mouseEvent);
        });
        
        this.listenableRegion.setOnMouseDragged(mouseEvent -> {
            this.doResize(mouseEvent);
        });        
    }

    private void doResize(MouseEvent mouseEvent) {
        if ( this.drag.isNotOneOf(RIGHT, BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT) ) {
            System.out.println("blocked " + this.drag.name());
            return;
        } else {
            System.out.println(this.drag.name());
        }
        
        double deltaX = mouseEvent.getScreenX() - this.initialMouseX;
        double deltaY = mouseEvent.getScreenY() - this.initialMouseY;
        
        double newWidth = this.initialAffectableRegionWidth;
        double newHeight = this.initialAffectableRegionHeight;
        
        switch ( this.drag ) {
            case BOTTOM_RIGHT : {
                newWidth = this.initialAffectableRegionWidth + deltaX;
                newHeight = this.initialAffectableRegionHeight + deltaY;
                break;
            }
            case RIGHT : 
            case TOP_RIGHT : {
                newWidth = this.initialAffectableRegionWidth + deltaX;
                break;
            }
            case BOTTOM : 
            case BOTTOM_LEFT : {
                newHeight = this.initialAffectableRegionHeight + deltaY;
                break;    
            }
            default : {
                return;
            }
        }
        
        this.affectableRegion.setPrefWidth(newWidth);
        this.affectableRegion.setPrefHeight(newHeight);
        System.out.println(format("newHeight:%s newWidth:%s", newHeight, newWidth));
        
        this.affectableRegion.autosize();
        this.stage.sizeToScene();
    }

    private void doInitialMeasurements(MouseEvent mouseEvent) {
        this.initialMouseX = mouseEvent.getScreenX();
        this.initialMouseY = mouseEvent.getScreenY();
        Bounds listenableRegionBounds =
                this.listenableRegion.localToScreen(this.listenableRegion.getBoundsInLocal());
        this.initialAffectableRegionWidth = this.affectableRegion.getWidth();
        this.initialAffectableRegionHeight = this.affectableRegion.getHeight();
        
        this.drag = defineBorderDragType(
                this.initialMouseX, this.initialMouseY,
                listenableRegionBounds.getMinX(), listenableRegionBounds.getMinY(),
                this.listenableRegion.getWidth(),  this.listenableRegion.getHeight(),
                10.0, 15.0);
    }
    
}
