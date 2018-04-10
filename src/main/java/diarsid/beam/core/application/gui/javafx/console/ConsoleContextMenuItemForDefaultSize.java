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
public class ConsoleContextMenuItemForDefaultSize extends ConsoleContextMenuItem {

    public ConsoleContextMenuItemForDefaultSize(
            ContextControlableConsole console, ConsoleContextMenu contextMenu) {
        super(console, contextMenu, 2);
        super.setText("default size");
        
        super.setOnAction(event -> {
            super.console().toDefaultSize();
        });        
    }

    @Override
    void onContextMenuShow() {
        if ( super.console().hasNonDefaultSize() ) {
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
