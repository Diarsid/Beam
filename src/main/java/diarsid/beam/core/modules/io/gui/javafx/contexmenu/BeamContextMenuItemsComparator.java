/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.contexmenu;

import java.util.Comparator;

import javafx.scene.control.MenuItem;

/**
 *
 * @author Diarsid
 */
public class BeamContextMenuItemsComparator implements Comparator<MenuItem> {

    @Override
    public int compare(MenuItem menuItem1, MenuItem menuItem2) {
        boolean is1BeamComponent = menuItem1 instanceof BeamContextMenuItem;
        boolean is2BeamComponent = menuItem2 instanceof BeamContextMenuItem;
        
        if ( is1BeamComponent && is2BeamComponent ) {
            BeamContextMenuItem beamMenuItem1 = (BeamContextMenuItem) menuItem1;
            BeamContextMenuItem beamMenuItem2 = (BeamContextMenuItem) menuItem2;
            
            return beamMenuItem1.compareTo(beamMenuItem2);
        } else if ( is1BeamComponent ) {
            return -1;
        } else if ( is2BeamComponent ) {
            return 1;
        } else {
            return 1;
        }
    }
    
}
