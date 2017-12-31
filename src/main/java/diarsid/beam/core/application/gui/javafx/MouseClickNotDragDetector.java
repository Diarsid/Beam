/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import static java.lang.System.currentTimeMillis;

import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.beam.core.base.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class MouseClickNotDragDetector {
    
    private final Node node;
    
    private Consumer<MouseEvent> onClickedNotDragged;
    private boolean wasDragged;
    private long timePressed;
    private long timeReleased;
    private long pressedDurationTreshold;

    private MouseClickNotDragDetector(Node node) {
        this.node = node;
        
        node.addEventHandler(MOUSE_PRESSED, (mouseEvent) -> {
            this.timePressed = currentTimeMillis();
        });
        
        node.addEventHandler(MOUSE_DRAGGED, (mouseEvent) -> {
            this.wasDragged = true;
        });
        
        node.addEventHandler(MOUSE_RELEASED, (mouseEvent) -> {
            this.timeReleased = currentTimeMillis();
            this.fireEventIfWasClickedNotDragged(mouseEvent);
            this.clear();
        });
        
        this.pressedDurationTreshold = 200;
    }
    
    static MouseClickNotDragDetector clickNotDragDetectingOn(Node node) {
        return new MouseClickNotDragDetector(node);
    }
    
    MouseClickNotDragDetector withPressedDurationTreshold(long durationTreshold) {
        this.pressedDurationTreshold = durationTreshold;
        return this;
    }
    
    MouseClickNotDragDetector setOnMouseClickedNotDragged(Consumer<MouseEvent> onClickedNotDragged) {
        this.onClickedNotDragged = onClickedNotDragged;
        return this;
    }
    
    private void clear() {
        this.wasDragged = false;
        this.timePressed = 0;
        this.timeReleased = 0;
    }
    
    private void fireEventIfWasClickedNotDragged(MouseEvent mouseEvent) {
        if ( this.wasDragged ) {
            debug("[CLICK-NOT-DRAG] dragged!");
            return;
        }
        if ( this.mousePressedDuration() > this.pressedDurationTreshold ) {
            debug("[CLICK-NOT-DRAG] pressed too long, not a click!");
            return;
        }
        debug("[CLICK-NOT-DRAG] click!");
        this.onClickedNotDragged.accept(mouseEvent);
    }
    
    private long mousePressedDuration() {
        return this.timeReleased - this.timePressed;
    }
}
