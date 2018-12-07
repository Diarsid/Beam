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
public interface MutableRectangleSize extends RectangleSize {

    boolean isEmpty();

    boolean isNotEmpty();

    void set(double width, double height);

    void set(RectangleSize otherSize);
    
    void setWidth(double width);
    
    void setHeight(double height);

    RectangleSize asImmutable();
    
}
