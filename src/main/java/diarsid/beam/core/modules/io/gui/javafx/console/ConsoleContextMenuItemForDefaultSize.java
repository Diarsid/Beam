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
class ConsoleContextMenuItemForDefaultSize extends BeamConsoleContextMenuItem {

    ConsoleContextMenuItemForDefaultSize(
            ContextControlableConsole console, BeamContextMenu contextMenu) {
        super(console, contextMenu, 2);
        super.setText("default size");
        
        super.setOnAction(event -> {
            super.console().toDefaultSize();
        });        
    }

    @Override
    protected void onContextMenuShow() {
        if ( super.console().hasNonDefaultSize() ) {
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
