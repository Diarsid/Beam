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

/**
 *
 * @author Diarsid
 */
class BeamHiddenRootWindow {
    
    private Stage stage;

    BeamHiddenRootWindow() {
        Platform.runLater(() -> {
            this.stage = new Stage();
            this.stage.initStyle(StageStyle.UTILITY);
            this.stage.setMinWidth(0);
            this.stage.setMinHeight(0);
            this.stage.setMaxWidth(0);
            this.stage.setMaxHeight(0);
            this.stage.setX(0);
            this.stage.setY(0);
            
            Scene scene = new Scene(new Label());
            scene.setFill(Color.TRANSPARENT);
            this.stage.setScene(scene);
            this.stage.sizeToScene();
            this.stage.show();
        });
    }
    
    Stage hiddenStage() {
        return this.stage;
    }
    
}
