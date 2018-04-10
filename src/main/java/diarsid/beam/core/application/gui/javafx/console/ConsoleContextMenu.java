/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.console;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

import static java.util.Collections.sort;

import static javafx.stage.WindowEvent.WINDOW_HIDDEN;
import static javafx.stage.WindowEvent.WINDOW_SHOWING;

/**
 *
 * @author Diarsid
 */
class ConsoleContextMenu {
    
    private final ContextMenu jfxContextMenu;
    private final ObservableList<MenuItem> items;
    
    private final ContextControlableConsole console;
    private final ConsoleContextMenuItemForSnippet snippetMenuItem;
    private final ConsoleContextMenuItemForClear clearMenuItem;
    private final ConsoleContextMenuItemForDefaultSize defaultSizeMenuItem;
    private final ConsoleContextMenuItemsComparator menuItemsComparator;
    
    ConsoleContextMenu(ContextControlableConsole console) {
        this.console = console;        
        this.jfxContextMenu = new ContextMenu();    
        this.items = this.jfxContextMenu.getItems();
        this.menuItemsComparator = new ConsoleContextMenuItemsComparator();
        
        this.jfxContextMenu.addEventHandler(WINDOW_SHOWING, (windowEvent) -> {
            this.onShow();
            if ( this.jfxContextMenu.getItems().isEmpty() ) {
                windowEvent.consume();
            }
        });
        
        this.jfxContextMenu.addEventHandler(WINDOW_HIDDEN, (windowEvent) -> {
            this.onHide();
        });
        
        MenuItem closeMenuItem = new MenuItem("close");        
        closeMenuItem.getStyleClass().add("console-menu-item");
        closeMenuItem.setGraphic(this.createStandardMenuItemGraphic());
        closeMenuItem.setOnAction(event -> {
            this.console.hide();
        });
        
        this.jfxContextMenu.getItems().addAll(closeMenuItem);
        
        this.snippetMenuItem = new ConsoleContextMenuItemForSnippet(console, this);  
        this.clearMenuItem = new ConsoleContextMenuItemForClear(console, this);
        this.defaultSizeMenuItem = new ConsoleContextMenuItemForDefaultSize(console, this);
    }
    
    final Node createStandardMenuItemGraphic() {
        Label point = new Label();
        point.getStyleClass().add("console-menu-item-point");
        return point;
    }
    
    void add(ConsoleContextMenuItem menuItem) {
        this.items.add(menuItem);
        sort(this.items, this.menuItemsComparator);
    }
    
    void remove(ConsoleContextMenuItem menuItem) {
        this.items.remove(menuItem);
    }
    
    boolean isPresent(ConsoleContextMenuItem menuItem) {
        return this.items.contains(menuItem);
    }
    
    private MenuItem createDefualtSizeMenuItem() {
        MenuItem defualtSize = new MenuItem("default size");
        defualtSize.setOnAction(event -> {
            this.console.toDefaultSize();
        });
        return defualtSize;
    }
    
    ContextMenu javaFxContextMenu() {
        return this.jfxContextMenu;
    }
    
    private void onShow() {
        this.snippetMenuItem.onContextMenuShow();
        this.clearMenuItem.onContextMenuShow();
        this.defaultSizeMenuItem.onContextMenuShow();
    }
    
    private void onHide() {
        this.snippetMenuItem.onContextMenuHide();
        this.clearMenuItem.onContextMenuHide();
        this.defaultSizeMenuItem.onContextMenuHide();
    }
}
