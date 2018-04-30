/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import javafx.scene.Node;
import javafx.scene.control.Label;

import diarsid.beam.core.application.gui.javafx.contexmenu.BeamContextMenu;
import diarsid.beam.core.base.control.io.base.console.snippet.ConsoleSnippetFinder;
import diarsid.beam.core.base.control.io.base.console.snippet.Snippet;

/**
 *
 * @author Diarsid
 */
class ConsoleContextMenuItemForSnippet extends BeamConsoleContextMenuItem {    
    
    private final ConsoleSnippetFinder snippetFinder;
    
    private Snippet snippet;

    ConsoleContextMenuItemForSnippet(
            ContextControlableConsole console, BeamContextMenu contextMenu) {   
        super(console, contextMenu, 0);
        
        this.snippetFinder = new ConsoleSnippetFinder();
        
        super.setOnAction((event) -> {
            super.console().imitateCommandInput(this.snippet.line());
        });    
    }
    
    @Override 
    protected Node createMenuItemGraphic() {
        char star = 9733;
        Label starLabel = new Label(Character.toString(star));
        return starLabel;
    }
    
    @Override
    protected void onContextMenuShow() {
        if ( super.console().isInDialog() ) {
            return;
        }

        this.snippet = this.snippetFinder
                .in(super.console().text())
                .goToLineAt(super.console().caretPosition())
                .defineLineSnippetType()
                .composeSnippet()
                .getSnippetAndReset();

        if ( this.snippet.type().isReinvokable() ) {
            super.setText(this.snippet.reinvokationTextWithLengthLimit(25));
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
