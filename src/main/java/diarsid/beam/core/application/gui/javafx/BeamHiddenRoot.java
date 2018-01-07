/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static java.lang.Double.MAX_VALUE;

/**
 *
 * @author Diarsid
 */
class BeamHiddenRoot {
    
    private final List<Stage> hiddenStages;

    BeamHiddenRoot() {
        this.hiddenStages = new ArrayList<>();
    }

    private Stage createHiddenStage() {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setMinWidth(0);
        stage.setMinHeight(0);
        stage.setMaxWidth(0);
        stage.setMaxHeight(0);
        stage.setX(MAX_VALUE);
        stage.setY(MAX_VALUE);
        
        Scene scene = new Scene(new Label());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
        
        return stage;
    }
    
    Stage newHiddenStage() {
        synchronized ( this.hiddenStages ) {
            Stage newHiddenStage = this.createHiddenStage();
            this.hiddenStages.add(newHiddenStage);
            return newHiddenStage;
        }        
    }
    
    void closeAllHiddenStages() {
        Platform.runLater(() -> this.hiddenStages.forEach(stage -> stage.close()));
    }
    
}
