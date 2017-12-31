/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static diarsid.beam.core.application.gui.javafx.MouseClickNotDragDetector.clickNotDragDetectingOn;

/**
 *
 * @author Diarsid
 */
class BeamControlWindow {
    
    private final WindowMover windowMover;
    
    private Stage stage;
    private Label label;

    BeamControlWindow(
            BeamHiddenRootWindow beamHiddenRootWindow,
            GuiJavaFXResources resources) {
        this.windowMover = new WindowMover();
        
        Platform.runLater(() -> {
            this.stage = new Stage();
            this.stage.initStyle(StageStyle.TRANSPARENT);
            this.stage.setMinWidth(40);
            this.stage.setMinHeight(40);
            this.stage.setMaxWidth(40);
            this.stage.setMaxHeight(40);
            this.stage.centerOnScreen();
            this.stage.setAlwaysOnTop(true);

            this.label = new Label();
            this.label.setMinHeight(40);
            this.label.setMinWidth(40);
            this.label.setStyle("-fx-background-color: green; ");

            Scene scene = new Scene(this.label);
            scene.setFill(Color.TRANSPARENT);
            this.stage.setScene(scene);
            this.stage.sizeToScene();
            this.stage.initOwner(beamHiddenRootWindow.hiddenStage());            
            
            this.windowMover.acceptStage(this.stage);
            this.windowMover.boundTo(this.label);
            
            this.stage.setOnCloseRequest((event) -> {
                // do nothing
            });
            
            clickNotDragDetectingOn(this.label)
                    .withPressedDurationTreshold(150)
                    .setOnMouseClickedNotDragged((mouseEvent) -> {
                        // logic here
                    });
            
            this.stage.show();
        });
    }
    
}
