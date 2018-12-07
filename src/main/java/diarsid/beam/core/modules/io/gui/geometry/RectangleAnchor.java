/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

import diarsid.beam.core.modules.io.gui.geometry.RealMutableRectangle.RealMutableRectangleAnchor;
import diarsid.beam.core.modules.io.gui.geometry.RealMutableRectangle.RealMutableRectangleSize;

/**
 *
 * @author Diarsid
 */
public interface RectangleAnchor extends Point {
    
    static RectangleAnchor anchor(double x, double y) {
        return new RealMutableRectangleAnchor(x, y);
    }
    
    static RectangleSize size(double width, double height) {
        return new RealMutableRectangleSize(width, height);
    }
    
    default boolean lesserThan(RectangleAnchor other) {
        return this.x() <= other.x() && this.y() <= other.y();
    }
    
}
