/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx;

import static diarsid.beam.core.base.util.MathUtil.isBetween;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BorderDragTypePlace.CORNER;
import static diarsid.beam.core.modules.io.gui.javafx.BorderDragType.BorderDragTypePlace.SIDE;

/**
 *
 * @author Diarsid
 */
public enum BorderDragType {
    
    LEFT (SIDE, false),
    TOP (SIDE, false),
    RIGHT (SIDE, true),
    BOTTOM (SIDE, true),

    TOP_LEFT (CORNER, false),
    TOP_RIGHT (CORNER, true),
    BOTTOM_RIGHT (CORNER, true),
    BOTTOM_LEFT (CORNER, true),
    
    UNDEFINED_DRAG (null, false);
    
    static enum BorderDragTypePlace {
        SIDE,
        CORNER
    }
    
    private final BorderDragTypePlace place;
    private final boolean allowedToResize;
    
    private BorderDragType(BorderDragTypePlace place, boolean allowedToResize) {
        this.place = place;
        this.allowedToResize = allowedToResize;
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
    
    public boolean isNotAllowedToResize() {
        return ! this.allowedToResize;
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
