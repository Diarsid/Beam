/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import java.util.function.BiConsumer;

import javafx.stage.Screen;
import javafx.stage.Stage;

import diarsid.beam.core.modules.io.gui.geometry.Point;

/**
 *
 * @author Diarsid
 */
public class JavaFXUtil {
    
    private static final BiConsumer<Stage, Point> SET_RECTANGLE_TO_STAGE;
    
    static {
        SET_RECTANGLE_TO_STAGE = (stage, point) -> {
            stage.setX(point.x());
            stage.setY(point.y());
        };
    }
    
    private JavaFXUtil() {}
    
    public static double screenWidth() {
        return Screen.getPrimary().getVisualBounds().getWidth();      
    }
    
    public static double screenHeight() {
        return Screen.getPrimary().getVisualBounds().getHeight();
    } 
    
    public static void setStageAtPoint(Stage stage, Point point) {
        SET_RECTANGLE_TO_STAGE.accept(stage, point);
    }
    
}
