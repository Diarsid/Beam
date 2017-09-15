/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import javafx.geometry.Bounds;
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
public class WindowResizer {
    
    private final int defaultWidth;
    private final int defaultHeight;
    
    private Stage stage;
    private Pane pane;
    
    private double widthToHeightRatio;
    private double initialMouseX;
    private double initialMouseY;
    private double initialPaneX;
    private double initialPaneY;
    private double initialPaneWidth;
    private double initialPaneHeight;
    
    private BorderDragType drag;

    public WindowResizer(int defaultWidth, int defaultHeight) {
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
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
    
    void toDefaultSize() {
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
        this.widthToHeightRatio = this.pane.getWidth() / this.pane.getHeight();
        this.drag = defineBorderDragType(
                this.initialMouseX, this.initialMouseY, 
                this.initialPaneX, this.initialPaneY, 
                this.initialPaneWidth, this.initialPaneHeight);
//        System.out.printf("initialMouse: x: %s, y: %s", this.initialMouseX, this.initialMouseY);
//        System.out.println("");
//        System.out.printf("initialPane: x: %s, y: %s", this.initialPaneX, this.initialPaneY);
//        System.out.println("");
//        System.out.printf("initialPane: width: %s, height: %s", this.initialPaneWidth, this.initialPaneHeight);
//        System.out.println("");
//        System.out.printf("w2hRatio: %s", this.widthToHeightRatio);
//        System.out.println("");
//        System.out.println("drag: " + drag);
//        System.out.println("");
    }
    
    void mouseDragged(MouseEvent mouseEvent) {
        if ( this.drag.equals(LEFT_DRAG) || this.drag.equals(TOP_DRAG) ) {
            return;
        }
        
        double deltaX = mouseEvent.getScreenX() - this.initialMouseX;
        double deltaY = mouseEvent.getScreenY() - this.initialMouseY;
//        System.out.printf("delta: x: %s, y: %s", deltaX, deltaY);
//        System.out.println("");
        
        ResizeMode resize = defineResizeMode(deltaX, deltaY);
        
//        System.out.printf("resize mode: %s", resize);
//        System.out.println("");
        
        double newWidth;
        double newHeight;
        
        switch ( resize ) {
            case INCREASE : {
                if ( deltaX > deltaY ) {
                    newWidth = this.initialPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                } else {
                    newHeight = this.initialPaneHeight + deltaY;
                    newWidth = newHeight * widthToHeightRatio;
                }   
                break;
            }    
            case DECREASE : {
                if ( deltaX < deltaY ) {
                    newWidth = this.initialPaneWidth + deltaX;
                    newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                } else {
                    newHeight = this.initialPaneHeight + deltaY;
                    newWidth = newHeight * widthToHeightRatio;
                }   
                break;
            }    
            case INCREASE_BY_X : {
                newWidth = this.initialPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                break;
            }    
            case INCREASE_BY_Y : {
                newHeight = this.initialPaneHeight + deltaY;
                newWidth = newHeight * widthToHeightRatio;
                break;
            }    
            case DECREASE_BY_X : {
                newWidth = this.initialPaneWidth + deltaX;
                newHeight = newWidth * ( 1.0 / widthToHeightRatio );
                break;
            }    
            case DECREASE_BY_Y : {
                newHeight = this.initialPaneHeight + deltaY;
                newWidth = newHeight * widthToHeightRatio;
                break;
            }    
            default : {
                return;
            }    
        }
        
        
        this.pane.setPrefWidth(newWidth);
        this.pane.setPrefHeight(newHeight);
        
        this.stage.sizeToScene();
//        
//        System.out.println("");
    }
}
