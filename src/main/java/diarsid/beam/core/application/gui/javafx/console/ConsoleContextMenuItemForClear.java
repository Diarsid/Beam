/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

/**
 *
 * @author Diarsid
 */
class ConsoleContextMenuItemForClear extends ConsoleContextMenuItem {

    public ConsoleContextMenuItemForClear(
            ContextControlableConsole console, ConsoleContextMenu contextMenu) {
        super(console, contextMenu, 1);
        super.setText("clear");
        
        super.setOnAction(event -> {
            super.console().clear();
        });        
    }

    @Override
    void onContextMenuShow() {
        if ( super.console().isInDialog() ) {
            return;
        }
        
        if ( super.console().hasClearableContent() ) {
            super.addItselfToMenu();
        }
    }

    @Override
    void onContextMenuHide() {
        if ( super.isPresentInMenu() ) {
            super.removeItselfFromMenu();
        }
    }
    
}
