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
class BeamHiddenRoot {
    
    private Stage manageableWindowsStage;
    private Stage independentWindowsStage;

    BeamHiddenRoot() {
        Platform.runLater(() -> {
            this.manageableWindowsStage = this.createHiddenStage();
            this.independentWindowsStage = this.createHiddenStage();
        });
    }

    private Stage createHiddenStage() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(0);
        stage.setMaxHeight(0);
        stage.setX(0);
        stage.setY(0);
        
        Scene scene = new Scene(new Label());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        
        return stage;
    }
    
    Stage hiddenStageForIndependentWindows() {
        return this.independentWindowsStage;
    }
    
    Stage hiddenStageForManageableWindows() {
        return this.manageableWindowsStage;
    }
    
}
