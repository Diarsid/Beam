/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

import diarsid.support.objects.Possible;

/**
 *
 * @author Diarsid
 */
public interface MutableRectangle extends Rectangle {

    @Override
    MutableRectangleAnchor anchor();

    @Override
    MutableRectangleSize size();
    
    Possible<RectangleSize> minSize();
    
    Rectangle asImmutable();
    
    default boolean toMinSizeAbsolute() {
        if ( this.minSize().isPresent() ) {
            this.size().set(this.minSize().orThrow());
            return true;
        } else {
            return false;
        } 
    }
    
    default boolean toMinSizeIfSmaller() {        
        if ( this.minSize().isPresent() ) {
            MutableRectangleSize thisSize = this.size();
            RectangleSize thisMinSize = this.minSize().orThrow();
            if ( thisSize.width() < thisMinSize.width() ) {
                thisSize.setWidth(thisMinSize.width());
            }
            if ( thisSize.height() < thisMinSize.height() ) {
                thisSize.setHeight(thisMinSize.height());
            }
            return true;
        } else {
            return false;
        }       
    }
    
    default boolean isSmallerThanMinSize() {
        if ( this.minSize().isPresent() ) {
            RectangleSize minSize = this.minSize().orThrow();
            RectangleSize size = this.size();
            return 
                    size.width() < minSize.width() || 
                    size.height() < minSize.height();
        } else {
            return false;
        }
    }
    
    default boolean isBiggerThanMinSize() {
        if ( this.minSize().isPresent() ) {
            RectangleSize minSize = this.minSize().orThrow();
            RectangleSize size = this.size();
            return 
                    size.width() > minSize.width() || 
                    size.height() > minSize.height();
        } else {
            return false;
        }
    }
    
}
