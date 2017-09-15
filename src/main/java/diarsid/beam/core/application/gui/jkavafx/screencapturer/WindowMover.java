/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Diarsid
 */
public class WindowMover {
    
    private Stage stage;
    private double x;
    private double y;

    public WindowMover() {
    }
    
    void acceptStage(Stage stage) {
        this.stage = stage;
    }
    
    void onMousePressed(MouseEvent mouseEvent) {
        this.x = this.stage.getX() - mouseEvent.getScreenX();
        this.y = this.stage.getY() - mouseEvent.getScreenY();
    }
    
    void onMouseDragged(MouseEvent mouseEvent) {
        this.stage.setX(mouseEvent.getScreenX() + this.x);
        this.stage.setY(mouseEvent.getScreenY() + this.y);
    }
}
