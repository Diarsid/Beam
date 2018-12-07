/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

import diarsid.beam.core.modules.io.gui.javafx.contexmenu.BeamContextMenu;

/**
 *
 * @author Diarsid
 */
class ConsoleContextMenuItemForClear extends BeamConsoleContextMenuItem {

    ConsoleContextMenuItemForClear(
            ContextControlableConsole console, BeamContextMenu contextMenu) {
        super(console, contextMenu, 1);
        super.setText("clear");
        
        super.setOnAction(event -> {
            super.console().clear();
        });        
    }

    @Override
    protected void onContextMenuShow() {
        if ( super.console().isInDialog() ) {
            return;
        }
        
        if ( super.console().hasClearableContent() ) {
            super.addItselfToMenu();
        }
    }

    @Override
    protected void onContextMenuHide() {
        if ( super.isPresentInMenu() ) {
            super.removeItselfFromMenu();
        }
    }
    
}
