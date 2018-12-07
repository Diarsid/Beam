/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

import java.util.List;

import javafx.geometry.Rectangle2D;

import diarsid.support.configuration.Configuration;

import static java.lang.String.format;


/**
 *
 * @author Diarsid
 */
public interface Screen extends Rectangle {    
    
    public static Screen screen(int insetValue, double width, double height) {
        return new RealScreen(insetValue, width, height);
    }
    
    public static Screen screenUsingJavaFXScreenSize(Configuration configuration) {
        Rectangle2D screenRectangle = javafx.stage.Screen.getPrimary().getVisualBounds();
        
        List<Integer> insets = configuration.asInts("ui.screen.insets");
        
        switch ( insets.size() ) {
            case 0 : {
                return new RealScreen(0, screenRectangle.getWidth(), screenRectangle.getHeight());
            }
            case 1 : {
                return new RealScreen(
                        insets.get(0), 
                        screenRectangle.getWidth(), screenRectangle.getHeight());
            }
            case 4 : {
                return new RealScreen(
                        insets.get(0), insets.get(1), insets.get(2), insets.get(3), 
                        screenRectangle.getWidth(), screenRectangle.getHeight());
            }
            default : { 
                String message = format(
                        "'%s' option cannot have %s parameters!", 
                        "ui.screen.insets", insets.size());
                throw new IllegalArgumentException(message);
            }
        }        
    }
    
    boolean fit(MutableRectangle mutableRectangle);
    
}
