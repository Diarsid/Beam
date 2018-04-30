/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenu;
import diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenuItem;

/**
 *
 * @author Diarsid
 */
abstract class BeamConsoleContextMenuItem extends BeamContextMenuItem {
    
    private final ContextControlableConsole console;

    BeamConsoleContextMenuItem(ContextControlableConsole console, BeamContextMenu contextMenu, int position) {
        super(contextMenu, position);
        this.console = console;
    }
    
    protected ContextControlableConsole console() {
        return console;
    }
    
}
