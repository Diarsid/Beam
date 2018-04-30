/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import diarsid.beam.core.base.util.Possible;

import static java.lang.System.currentTimeMillis;

import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.beam.core.base.util.Possible.possible;

/**
 *
 * @author Diarsid
 */
class MouseClickNotDragDetector {
    
    private Possible<Consumer<MouseEvent>> onClickNotDrag;
    private Possible<Consumer<MouseEvent>> onDoubleClick;
    private boolean wasDragged;
    private long timePressed;
    private long timeReleased;
    private long pressedDurationTreshold;

    MouseClickNotDragDetector() {
    }

    private MouseClickNotDragDetector(Node node) {
        
        node.addEventHandler(MOUSE_PRESSED, (mouseEvent) -> {
            this.timePressed = currentTimeMillis();
        });
        
        node.addEventHandler(MOUSE_DRAGGED, (mouseEvent) -> {
            this.wasDragged = true;
        });
        
        node.addEventHandler(MOUSE_RELEASED, (mouseEvent) -> {
            this.timeReleased = currentTimeMillis();
            this.testOnClickConditionsAndPropagate(mouseEvent);
            this.clear();
        });
        
        this.pressedDurationTreshold = 200;
    }
    
    static MouseClickNotDragDetector smartClickDetectionOn(Node node) {
        return new MouseClickNotDragDetector(node);
    }
    
    MouseClickNotDragDetector withPressedDurationTreshold(long durationTreshold) {
        this.pressedDurationTreshold = durationTreshold;
        return this;
    }
    
    MouseClickNotDragDetector setOnMouseClickNotDrag(
            Consumer<MouseEvent> onClickNotDrag) {
        this.onClickNotDrag = possible(onClickNotDrag);
        return this;
    }
    
    MouseClickNotDragDetector setOnMouseDoubleClick(
            Consumer<MouseEvent> onDoubleClick) {
        this.onDoubleClick = possible(onDoubleClick);
        return this;
    }
    
    private void testOnClickConditionsAndPropagate(MouseEvent mouseEvent) {
        if ( this.wasClickedNotDragged() && this.onClickNotDrag.isPresent() ) {
            this.onClickNotDrag.orThrow().accept(mouseEvent);
        } else if ( this.wasDoubleClicked() && this.onDoubleClick.isPresent() ) {
            this.onDoubleClick.orThrow().accept(mouseEvent);
        } else {
            mouseEvent.consume();
        }
    }
    
    private void clear() {
        this.wasDragged = false;
        this.timePressed = 0;
        this.timeReleased = 0;
    }
    
    private boolean wasClickedNotDragged() {
        if ( this.wasDragged ) {
            return false;
        }
        if ( this.mousePressedDuration() > this.pressedDurationTreshold ) {
            return false;
        }
        return true;
    }
    
    private boolean wasDoubleClicked() {
        return false;
    }
    
    private long mousePressedDuration() {
        return this.timeReleased - this.timePressed;
    }
}
