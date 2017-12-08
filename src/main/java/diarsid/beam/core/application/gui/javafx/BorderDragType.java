/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import static diarsid.beam.core.application.gui.javafx.BorderDragType.BorderDragTypePlace.CORNER;
import static diarsid.beam.core.application.gui.javafx.BorderDragType.BorderDragTypePlace.SIDE;
import static diarsid.beam.core.base.util.MathUtil.isBetween;

/**
 *
 * @author Diarsid
 */
public enum BorderDragType {
    
    LEFT (SIDE),
    TOP (SIDE),
    RIGHT (SIDE),
    BOTTOM (SIDE),

    TOP_LEFT (CORNER),
    TOP_RIGHT (CORNER),
    BOTTOM_RIGHT (CORNER),
    BOTTOM_LEFT (CORNER),
    
    UNDEFINED_DRAG (null);
    
    static enum BorderDragTypePlace {
        SIDE,
        CORNER
    }
    
    private final BorderDragTypePlace place;
    
    private BorderDragType(BorderDragTypePlace place) {
        this.place = place;
    }
    
    public boolean isCorner() {
        return CORNER.equals(this.place);
    }
    
    public boolean isSide() {
        return SIDE.equals(this.place );
    }
    
    public boolean isNotOneOf(BorderDragType... others) {
        if ( this.equals(UNDEFINED_DRAG) ) {
            return true;
        }
        for (BorderDragType other : others) {
            if ( this.equals(other) ) {
                return false;
            }
        }
        return true;
    }

    public static BorderDragType defineBorderDragType(
                double initMouseX, double initMouseY, 
                double initPaneX, double initPaneY, 
                double initPaneWidth, double initPaneHeight,
                double borderWidth, double borderRadius) {
        if ( isBetween(
                initPaneX, 
                initMouseX, 
                initPaneX + borderWidth) ) {
            if ( isBetween(
                    initPaneY, 
                    initMouseY, 
                    initPaneY + borderRadius) ) {
                return TOP_LEFT;
            } else if ( isBetween(
                    initPaneY + initPaneHeight - borderRadius, 
                    initMouseY, 
                    initPaneY + initPaneHeight) ) {
                return BOTTOM_LEFT;
            } else {
                return LEFT;
            }            
        } else if ( isBetween(
                initPaneY, 
                initMouseY, 
                initPaneY + borderWidth) ) {
            return TOP;
        } else if ( isBetween(
                (initPaneX + initPaneWidth - borderWidth), 
                initMouseX, 
                (initPaneX + initPaneWidth)) ) {
            if ( isBetween(
                    initPaneY, 
                    initMouseY, 
                    initPaneY + borderRadius) ) {
                return TOP_RIGHT;
            } else if ( isBetween(
                    initPaneY + initPaneHeight - borderRadius, 
                    initMouseY, 
                    initPaneY + initPaneHeight) ) {
                return BOTTOM_RIGHT;
            } else {
                return RIGHT;
            }            
        } else if ( isBetween(
                (initPaneY + initPaneHeight - borderWidth), 
                initMouseY, 
                (initPaneY + initPaneHeight)) ) {
            return BOTTOM;
        } else {
            return UNDEFINED_DRAG;
        }
    }
}
