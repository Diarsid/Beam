/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.jkavafx.screencapturer;

/**
 *
 * @author Diarsid
 */
public enum BorderDragType {
    LEFT_DRAG,
    TOP_DRAG,
    RIGHT_DRAG,
    BOTTOM_DRAG;

    static BorderDragType defineBorderDragType(
                double initialMouseX, double initialMouseY, 
                double initialPaneX, double initialPaneY, 
                double initialPaneWidth, double initialPaneHeight) {
        if ( initialMouseX > (initialPaneX - 15.0) 
             && initialMouseX < (initialPaneX + 15.0) ) {
            return LEFT_DRAG;
        } else if ( initialMouseY > (initialPaneY - 15.0) 
                    && initialMouseY < (initialPaneY + 15.0) ) {
            return TOP_DRAG;
        } else if ( initialMouseX > (initialPaneX + initialPaneWidth - 15.0) 
                    && initialMouseX < (initialPaneX + initialPaneWidth + 15.0) ) {
            return RIGHT_DRAG;
        } else {
            return BOTTOM_DRAG;
        }
    }
}
