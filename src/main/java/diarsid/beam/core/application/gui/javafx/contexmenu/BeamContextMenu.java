/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx.contexmenu;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;

import static javafx.stage.WindowEvent.WINDOW_HIDDEN;
import static javafx.stage.WindowEvent.WINDOW_SHOWING;

/**
 *
 * @author Diarsid
 */
public class BeamContextMenu {
    
    private final ContextMenu jfxContextMenu;
    private final ObservableList<MenuItem> items;
    
    private final List<BeamContextMenuItem> beamContextMenuItems;
    private final BeamContextMenuItemsComparator menuItemsComparator;
    
    public BeamContextMenu() {
        this.jfxContextMenu = new ContextMenu();    
        this.items = this.jfxContextMenu.getItems();
        
        this.beamContextMenuItems = new ArrayList<>();
        this.menuItemsComparator = new BeamContextMenuItemsComparator();
        
        this.jfxContextMenu.addEventHandler(WINDOW_SHOWING, (windowEvent) -> {
            this.onShow();
            if ( this.jfxContextMenu.getItems().isEmpty() ) {
                windowEvent.consume();
            }
        });
        
        this.jfxContextMenu.addEventHandler(WINDOW_HIDDEN, (windowEvent) -> {
            this.onHide();
        });
    }
    
    public final void registerJavaFxItem(MenuItem menuItem) {
        this.jfxContextMenu.getItems().addAll(menuItem);        
    }
    
    public final void registerBeamItems(BeamContextMenuItem... items) {
        this.beamContextMenuItems.addAll(asList(items));
    }
    
    void add(BeamContextMenuItem menuItem) {
        this.items.add(menuItem);
        sort(this.items, this.menuItemsComparator);
    }
    
    void remove(BeamContextMenuItem menuItem) {
        this.items.remove(menuItem);
    }
    
    boolean isPresent(BeamContextMenuItem menuItem) {
        return this.items.contains(menuItem);
    }
    
    public ContextMenu javaFxContextMenu() {
        return this.jfxContextMenu;
    }
    
    private void onShow() {
        this.beamContextMenuItems.forEach(item -> item.onContextMenuShow());
    }
    
    private void onHide() {        
        this.beamContextMenuItems.forEach(item -> item.onContextMenuHide());
    }
}
