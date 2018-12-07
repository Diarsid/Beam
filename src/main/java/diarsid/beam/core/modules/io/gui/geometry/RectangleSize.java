/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

/**
 *
 * @author Diarsid
 */
public interface RectangleSize extends Size {
    
    default boolean isOverallBiggerThan(RectangleSize other) {
        return this.width() > other.width() && 
               this.height() > other.height();
    }
    
    default boolean isOverallSmallerThan(RectangleSize other) {
        return this.width() < other.width() && 
               this.height() < other.height();
    }
    
}
