/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import static javafx.scene.input.MouseButton.PRIMARY;
import static javafx.scene.input.MouseEvent.MOUSE_DRAGGED;
import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;

/**
 *
 * @author Diarsid
 */
public class WindowMover {
    
    public interface AfterMoveCallback {

        void movedTo(double x, double y);
        
    }
    
    private final List<AfterMoveCallback> afterMoveCallbacks;
    private final AtomicReference<EventType<MouseEvent>> lastEvent;
    private final AtomicReference<MouseButton> buttonPressed;
    private Stage stage;
    private double x;
    private double y;
    
    public WindowMover() {  
        this.afterMoveCallbacks = new ArrayList<>();
        this.lastEvent = new AtomicReference<>();
        this.buttonPressed = new AtomicReference<>();
    }
    
    public void acceptStage(Stage stage) {
        this.stage = stage;
    }
    
    public void boundTo(Node node) {
        
        node.addEventHandler(MOUSE_PRESSED, (mouseEvent) -> {
            MouseButton button = mouseEvent.getButton();
            this.buttonPressed.set(button);
            if ( button.equals(PRIMARY) ) {
                this.x = this.stage.getX() - mouseEvent.getScreenX();
                this.y = this.stage.getY() - mouseEvent.getScreenY();
                this.lastEvent.set(MOUSE_PRESSED);
                mouseEvent.consume();
            }                 
        });
        
        node.addEventHandler(MOUSE_DRAGGED, (mouseEvent) -> {
            if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
                return;
            }
            
            this.stage.setX(mouseEvent.getScreenX() + this.x);
            this.stage.setY(mouseEvent.getScreenY() + this.y);
            mouseEvent.consume();
            this.lastEvent.set(MOUSE_DRAGGED);
        });
        
        node.addEventHandler(MOUSE_RELEASED, (mouseEvent) -> {
            if ( ! this.buttonPressed.get().equals(PRIMARY) ) {
                return;
            }
            
            if ( MOUSE_DRAGGED.equals(this.lastEvent.get()) ) {
                this.afterMoveCallbacks.forEach(callback -> {
                    callback.movedTo(this.stage.getX(), this.stage.getY()); 
                });
            }            
            mouseEvent.consume();
            this.lastEvent.set(MOUSE_RELEASED);
        });
    }
    
    public void afterMove(AfterMoveCallback afterMoveCallback) {
        this.afterMoveCallbacks.add(afterMoveCallback);
    }
}
