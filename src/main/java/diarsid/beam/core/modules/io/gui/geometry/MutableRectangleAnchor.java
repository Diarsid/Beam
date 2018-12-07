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
public interface MutableRectangleAnchor extends RectangleAnchor {

    boolean isEmpty();

    boolean isNotEmpty();

    void set(double x, double y);
    
    void set(Point point);
    
    void setX(double x);
    
    void setY(double y);

    RectangleAnchor asImmutable();
    
}
