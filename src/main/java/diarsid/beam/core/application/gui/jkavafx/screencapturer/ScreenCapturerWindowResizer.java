/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static diarsid.beam.core.application.gui.jkavafx.screencapturer.BorderDragType.LEFT_DRAG;
import static diarsid.beam.core.application.gui.jkavafx.screencapturer.BorderDragType.TOP_DRAG;
import static diarsid.beam.core.application.gui.jkavafx.screencapturer.BorderDragType.defineBorderDragType;
import static diarsid.beam.core.application.gui.jkavafx.screencapturer.ResizeMode.DECREASE;
import static diarsid.beam.core.application.gui.jkavafx.screencapturer.ResizeMode.INCREASE;
import static diarsid.beam.core.application.gui.jkavafx.screencapturer.ResizeMode.defineResizeMode;

/**
 *
 * @author Diarsid
 */
public class ScreenCapturerWindowResizer {
    
    private final int defaultWidth;
    private final int defaultHeight;
    
    private Stage stage;
    private Pane pane;
    private Label label;
    
    private double widthToHeightRatio;
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
        this.label.setMinWidth(60);
    }
    
    void toDefaultSize() {
        this.label.setPrefWidth(60);
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
                this.initialPaneWidth, this.initialPaneHeight);
    }
    
    void mouseDragged(MouseEvent mouseEvent) {
        if ( this.drag.equals(LEFT_DRAG) || this.drag.equals(TOP_DRAG) ) {
            return;
        }
        
        double deltaX = mouseEvent.getScreenX() - this.initialMouseX;
        double deltaY = mouseEvent.getScreenY() - this.initialMouseY;
        
        ResizeMode resize = defineResizeMode(deltaX, deltaY);
        
        double newWidth;
        double newHeight;
        
        double virtualPaneWidth = this.initialPaneWidth - 32;
        double virtualPaneHeight = this.initialPaneHeight - 10;
        
        switch ( resize ) {
            case INCREASE : {
                if ( deltaX > deltaY ) {
                    newWidth = virtualPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                } else {
                    newHeight = virtualPaneHeight + deltaY;
                    newWidth = newHeight * widthToHeightRatio;
                }   
                break;
            }    
            case DECREASE : {
                if ( deltaX < deltaY ) {
                    newWidth = virtualPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                } else {
                    newHeight = virtualPaneHeight + deltaY;
                    newWidth = newHeight * widthToHeightRatio;
                }   
                break;
            }    
            case INCREASE_BY_X : {
                newWidth = virtualPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                break;
            }    
            case INCREASE_BY_Y : {
                newHeight = virtualPaneHeight + deltaY;
                newWidth = newHeight * widthToHeightRatio;
                break;
            }    
            case DECREASE_BY_X : {
                newWidth = virtualPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                break;
            }    
            case DECREASE_BY_Y : {
                newHeight = virtualPaneHeight + deltaY;
                newWidth = newHeight * widthToHeightRatio;
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
        
        if ( labelNewWidth < 60.0d ) {
            labelNewWidth = 60.0d;
        } 
        this.label.setPrefWidth(labelNewWidth);     
        this.label.autosize();

        this.stage.sizeToScene();
    }
}
