/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;

import java.util.function.Consumer;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import diarsid.support.objects.Possible;

import static java.lang.System.currentTimeMillis;

import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class MouseClickNotDragDetector {
    
    private final Possible<Consumer<MouseEvent>> onClickNotDrag;
    private final Possible<Consumer<MouseEvent>> onDoubleClick;
    private boolean wasDragged;
    private long timePressed;
    private long timeReleased;
    private long pressedDurationTreshold;

    MouseClickNotDragDetector() {
        this.onClickNotDrag = possibleButEmpty();
        this.onDoubleClick = possibleButEmpty();
    }

    private MouseClickNotDragDetector(Node node) {
        this.onClickNotDrag = possibleButEmpty();
        this.onDoubleClick = possibleButEmpty();
        
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
        this.onClickNotDrag.resetTo(onClickNotDrag);
        return this;
    }
    
    MouseClickNotDragDetector setOnMouseDoubleClick(
            Consumer<MouseEvent> onDoubleClick) {
        this.onDoubleClick.resetTo(onDoubleClick);
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
