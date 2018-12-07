/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

import javafx.geometry.Bounds;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import diarsid.beam.core.modules.io.gui.javafx.BorderDragType;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM_LEFT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM_RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.TOP_RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.defineBorderDragType;

/**
 *
 * @author Diarsid
 */
class DragRegionResizer {
    
    interface AfterResizeCallback {

        void resizedTo(double newWidth, double newHeight);
        
    }
    
    private AfterResizeCallback afterResizeCallback;
    
    private Runnable sizeStageToScene;
    private Region region;
    
    private double initialMouseX;
    private double initialMouseY;
    
    private double initialRegionWidth;
    private double initialRegionHeight;
    
    private double newWidth;
    private double newHeight;
    
    private BorderDragType drag;

    DragRegionResizer() {
    }
    
    void acceptSizeToScene(Runnable sizeStageToScene) {
        this.sizeStageToScene = sizeStageToScene;
    }
    
    void afterResize(AfterResizeCallback callback) {
        this.afterResizeCallback = callback;
    }
    
    boolean hasNonDefaultSize() {
        boolean hasDefaultWidth = 
                this.region.getMinWidth() == this.region.getWidth();
        boolean hasDefaultHeight = 
                this.region.getMinHeight() == this.region.getHeight();
        
        return ! ( hasDefaultHeight && hasDefaultWidth );                
    }
    
    void toDefaultSize() {
        this.region.setPrefWidth(this.region.getMinWidth());
        this.region.setPrefHeight(this.region.getMinHeight());
        this.afterResizeCallback.resizedTo(this.region.getMinWidth(), this.region.getMinHeight());
        this.region.autosize();
        this.sizeStageToScene.run();
    }
    
    void makeResiziable(Region listenableRegion) {
        this.region = listenableRegion;
        
        this.region.addEventHandler(MOUSE_PRESSED, (mouseEvent) -> {
            if ( ! mouseEvent.getButton().equals(PRIMARY) ) {
                return;
            }
            this.doInitialMeasurements(mouseEvent);
        });
        
        this.region.addEventHandler(MOUSE_DRAGGED, (mouseEvent) -> {
            if ( ! mouseEvent.getButton().equals(PRIMARY) ) {
                return;
            }
            this.doResizeRegion(mouseEvent);
        });        
        
        this.region.addEventHandler(MOUSE_RELEASED, (mouseEvent) -> {
            if ( ! mouseEvent.getButton().equals(PRIMARY) ) {
                return;
            }
            this.afterResizeCallback.resizedTo(this.newWidth, this.newHeight);
        });
    }

    private void doInitialMeasurements(MouseEvent mouseEvent) {
        this.initialMouseX = mouseEvent.getScreenX();
        this.initialMouseY = mouseEvent.getScreenY();
        
        this.initialRegionWidth = this.region.getWidth();
        this.initialRegionHeight = this.region.getHeight();
        
        Bounds regionBounds = this.region.localToScreen(this.region.getBoundsInLocal());
        this.drag = defineBorderDragType(
                this.initialMouseX, this.initialMouseY,
                regionBounds.getMinX(), regionBounds.getMinY(),
                this.region.getWidth(),  this.region.getHeight(),
                10.0, 15.0);
    }

    private void doResizeRegion(MouseEvent mouseEvent) {
        if ( this.drag.isNotAllowedToResize() ) {
            return;
        } 
        
        double deltaX = mouseEvent.getScreenX() - this.initialMouseX;
        double deltaY = mouseEvent.getScreenY() - this.initialMouseY;
        
        this.newWidth = this.initialRegionWidth;
        this.newHeight = this.initialRegionHeight;
        
        switch ( this.drag ) {
            case BOTTOM_RIGHT : {
                this.newWidth = this.initialRegionWidth + deltaX;
                this.newHeight = this.initialRegionHeight + deltaY;
                break;
            }
            case RIGHT : 
            case TOP_RIGHT : {
                this.newWidth = this.initialRegionWidth + deltaX;
                break;
            }
            case BOTTOM : 
            case BOTTOM_LEFT : {
                this.newHeight = this.initialRegionHeight + deltaY;
                break;    
            }
            default : {
                return;
            }
        }
        
        if ( this.newWidth < this.region.getMinWidth() ) {
            this.newWidth = this.region.getMinWidth();
        }
        
        if ( this.newHeight < this.region.getMinHeight() ) {
            this.newHeight = this.region.getMinHeight();
        }
                
        this.region.setPrefWidth(this.newWidth);
        this.region.setPrefHeight(this.newHeight);
        
        this.region.autosize();
        this.sizeStageToScene.run();
    }
    
}
