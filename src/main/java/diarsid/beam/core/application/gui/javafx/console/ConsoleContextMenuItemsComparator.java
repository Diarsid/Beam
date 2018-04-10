/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import java.util.Comparator;

import javafx.scene.control.MenuItem;

/**
 *
 * @author Diarsid
 */
public class ConsoleContextMenuItemsComparator implements Comparator<MenuItem> {

    @Override
    public int compare(MenuItem menuItem1, MenuItem menuItem2) {
        boolean is1ConsoleComponent = menuItem1 instanceof ConsoleContextMenuItem;
        boolean is2ConsoleComponent = menuItem2 instanceof ConsoleContextMenuItem;
        
        if ( is1ConsoleComponent && is2ConsoleComponent ) {
            ConsoleContextMenuItem consoleMenuItem1 = (ConsoleContextMenuItem) menuItem1;
            ConsoleContextMenuItem consoleMenuItem2 = (ConsoleContextMenuItem) menuItem2;
            
            return consoleMenuItem1.compareTo(consoleMenuItem2);
        } else if ( is1ConsoleComponent ) {
            return -1;
        } else if ( is2ConsoleComponent ) {
            return 1;
        } else {
            return 1;
        }
    }
    
}
