/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.geometry;

import static diarsid.beam.core.modules.io.gui.geometry.RectangleAnchor.anchor;
import static diarsid.beam.core.modules.io.gui.geometry.RectangleAnchor.size;
import static diarsid.beam.core.modules.io.gui.geometry.Screen.screen;

/**
 *
 * @author Diarsid
 */
public class Geometry {
    
    public static void main(String[] args) {
        Screen screen = screen(10, 100, 100);
        
        MutableRectangle rectangle = new RealMutableNamedRectangle("a", anchor(110, 110), size(120, 60), size(100, 40));
        screen.fit(rectangle);
        
        System.out.println(rectangle.toString());
    }
}
