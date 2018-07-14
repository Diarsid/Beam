/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.util;

import javafx.stage.Screen;

/**
 *
 * @author Diarsid
 */
public class JavaFXUtil {
    
    private JavaFXUtil() {}
    
    public static double screenWidth() {
        return Screen.getPrimary().getVisualBounds().getWidth();      
    }
    
    public static double screenHeight() {
        return Screen.getPrimary().getVisualBounds().getHeight();
    }    
    
}
