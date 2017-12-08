/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.screencapturer;

import static java.lang.Math.abs;

/**
 *
 * @author Diarsid
 */
enum RestrictedResizeMode {
    INCREASE,
    DECREASE,
    INCREASE_BY_X,
    INCREASE_BY_Y,
    DECREASE_BY_X,
    DECREASE_BY_Y;
    
    static RestrictedResizeMode defineResizeMode(double deltaX, double deltaY) {        
        if ( deltaX > 0 && deltaY > 0 ) {
            return INCREASE;
        } else if ( deltaX < 0 && deltaY < 0 ) {
            return DECREASE;
        } else if ( deltaX > 0 && deltaY < 0 ) {
            if ( deltaX > abs(deltaY) ) {
                return INCREASE_BY_X;
            } else {
                return DECREASE_BY_Y;
            }
        } else {
            if ( abs(deltaX) > deltaY ) {
                return DECREASE_BY_X;
            } else {
                return INCREASE_BY_Y;
            }
        }
    }
}
