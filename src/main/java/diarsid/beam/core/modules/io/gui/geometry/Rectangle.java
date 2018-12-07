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
public interface Rectangle {

    RectangleAnchor anchor();

    RectangleSize size();
    
    default Double oppositeAnchorX() {
        return this.anchor().x() + this.size().width();
    }
    
    default Double oppositeAnchorY() {
        return this.anchor().y() + this.size().height();
    }
    
    default boolean contains(Rectangle other) {
        return this.anchorLesserThanIn(other) && this.oppositeAnchorBiggerThanIn(other);
    }
    
    default boolean contains(RectangleAnchor other) {
        return 
                this.anchor().lesserThan(other) &&
                this.oppositeAnchorX() >= other.x() &&
                this.oppositeAnchorY() >= other.y();
    }
    
    default boolean anchorBiggerThanIn(Rectangle other) {
        return ! this.anchorLesserThanIn(other);
    }
    
    default boolean anchorLesserThanIn(Rectangle other) {
        return 
                this.anchor().x() <= other.anchor().x() &&
                this.anchor().y() <= other.anchor().y();
    }
    
    default boolean oppositeAnchorLesserThanIn(Rectangle other) {
        return ! this.oppositeAnchorBiggerThanIn(other);
    }
    
    default boolean oppositeAnchorBiggerThanIn(Rectangle other) {
        return 
                this.oppositeAnchorX() >= other.oppositeAnchorX() &&
                this.oppositeAnchorY() >= other.oppositeAnchorY();
    }
    
}
