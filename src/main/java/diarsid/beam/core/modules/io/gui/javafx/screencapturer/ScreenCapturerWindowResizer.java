/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.screencapturer;

import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import diarsid.beam.core.modules.io.gui.javafx.BorderDragType;

import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM_LEFT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BOTTOM_RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.TOP_RIGHT;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.defineBorderDragType;
import static diarsid.beam.core.modules.io.gui.javafx.screencapturer.RestrictedResizeMode.DECREASE;
import static diarsid.beam.core.modules.io.gui.javafx.screencapturer.RestrictedResizeMode.INCREASE;
import static diarsid.beam.core.modules.io.gui.javafx.screencapturer.RestrictedResizeMode.defineResizeMode;

/**
 *
 * @author Diarsid
 */
public class ScreenCapturerWindowResizer {
    
    private final int defaultWidth;
    private final int defaultHeight;    
    private final double widthToHeightRatio;
    private final double labelDefaultWidth;
    
    private Stage stage;
    private Pane pane;
    private Label label;
    private double initialMouseX;
    private double initialMouseY;
    private double initialPaneX;
    private double initialPaneY;
    private double initialPaneWidth;
    private double initialPaneHeight;
    private double initialLabelWidth;
    
    private BorderDragType drag;

    public ScreenCapturerWindowResizer(int defaultWidth, int defaultHeight) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.widthToHeightRatio = 110.0d / 80.0d;
        this.labelDefaultWidth = 45.0;
    }
    
    void acceptStage(Stage stage) {
        this.stage = stage;
    }
    
    void acceptPane(Pane pane) {
        this.pane = pane;     
        this.pane.setMinWidth(this.defaultWidth);
        this.pane.setMinHeight(this.defaultHeight);   
        this.toDefaultSize();
    }
    
    void acceptLabel(Label label) {
        this.label = label;
        this.label.setMinWidth(this.labelDefaultWidth);
    }
    
    void toDefaultSize() {
        this.label.setPrefWidth(this.labelDefaultWidth);
        this.pane.setPrefWidth(this.defaultWidth);
        this.pane.setPrefHeight(this.defaultHeight);        
        this.stage.sizeToScene();        
    }
    
    void mousePressed(MouseEvent mouseEvent) {
        this.initialMouseX = mouseEvent.getScreenX();
        this.initialMouseY = mouseEvent.getScreenY();
        Bounds capturePaneScreenCoordinates = pane.localToScreen(pane.getBoundsInLocal());
        this.initialPaneX = capturePaneScreenCoordinates.getMinX();
        this.initialPaneY = capturePaneScreenCoordinates.getMinY();
        this.initialPaneWidth = this.pane.getWidth();
        this.initialPaneHeight = this.pane.getHeight();     
        this.initialLabelWidth = this.label.getWidth();
        
        this.drag = defineBorderDragType(
                this.initialMouseX, this.initialMouseY, 
                this.initialPaneX, this.initialPaneY, 
                this.initialPaneWidth, this.initialPaneHeight,
                15.0, 15.0);
    }
    
    void mouseDragged(MouseEvent mouseEvent) {
        if ( this.drag.isNotOneOf(RIGHT, BOTTOM, BOTTOM_LEFT, BOTTOM_RIGHT, TOP_RIGHT) ) {
            return;
        }
        
        double deltaX = mouseEvent.getScreenX() - this.initialMouseX;
        double deltaY = mouseEvent.getScreenY() - this.initialMouseY;
        
        RestrictedResizeMode resize = defineResizeMode(deltaX, deltaY);
        
        double newWidth;
        double newHeight;
        
        double virtualPaneWidth = this.initialPaneWidth - 32;
        double virtualPaneHeight = this.initialPaneHeight - 10;
        
        switch ( resize ) {
            case INCREASE : {
                if ( deltaX > deltaY ) {
                    newWidth = virtualPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / this.widthToHeightRatio );
                } else {
                    newHeight = virtualPaneHeight + deltaY;
                    newWidth = newHeight * this.widthToHeightRatio;
                }   
                break;
            }    
            case DECREASE : {
                if ( deltaX < deltaY ) {
                    newWidth = virtualPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / this.widthToHeightRatio );
                } else {
                    newHeight = virtualPaneHeight + deltaY;
                    newWidth = newHeight * this.widthToHeightRatio;
                }   
                break;
            }    
            case INCREASE_BY_X : {
                newWidth = virtualPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / this.widthToHeightRatio );
                break;
            }    
            case INCREASE_BY_Y : {
                newHeight = virtualPaneHeight + deltaY;
                newWidth = newHeight * this.widthToHeightRatio;
                break;
            }    
            case DECREASE_BY_X : {
                newWidth = virtualPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / this.widthToHeightRatio );
                break;
            }    
            case DECREASE_BY_Y : {
                newHeight = virtualPaneHeight + deltaY;
                newWidth = newHeight * this.widthToHeightRatio;
                break;
            }    
            default : {
                return;
            }    
        }
        
        double actualWidthDelta = ( newWidth + 32.0d ) - this.initialPaneWidth;
        double labelNewWidth = this.initialLabelWidth + actualWidthDelta;
                
        this.pane.setPrefWidth(newWidth + 32.0d);
        this.pane.setPrefHeight(newHeight + 10.0d);
        
        if ( labelNewWidth < this.labelDefaultWidth ) {
            labelNewWidth = this.labelDefaultWidth;
        } 
        this.label.setPrefWidth(labelNewWidth);     
        this.label.autosize();

        this.stage.sizeToScene();
    }
}
