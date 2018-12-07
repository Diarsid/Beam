/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.contexmenu;

import java.util.HashSet;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

import diarsid.beam.core.base.exceptions.WorkflowBrokenException;

import static java.lang.String.format;

/**
 *
 * @author Diarsid
 */
public abstract class BeamContextMenuItem 
        extends MenuItem 
        implements Comparable<BeamContextMenuItem> {
    
    private static final Set<Integer> USED_POSITIONS;
    
    static {
        USED_POSITIONS = new HashSet<>();
    }
    
    private final int position;
    private final BeamContextMenu contextMenu;

    protected BeamContextMenuItem(BeamContextMenu contextMenu, int position) {
        super();
        checkIfPositionIsFree(position);
        this.position = position;
        this.contextMenu = contextMenu;
        super.getStyleClass().add("console-menu-item");
        super.setGraphic(this.createMenuItemGraphic());
    }
    
    protected Node createMenuItemGraphic() {        
        return createStandardMenuItemGraphic();
    }
    
    public static Node createStandardMenuItemGraphic() {
        Label graphic = new Label();
        graphic.getStyleClass().add("console-menu-item-graphic");
        return graphic;
    }

    private void checkIfPositionIsFree(int position) throws WorkflowBrokenException {
        synchronized ( USED_POSITIONS ) {
            if ( USED_POSITIONS.contains(position) ) {
                throw new WorkflowBrokenException(
                        format("Cannot use position %s in console context menu!", position));
            } else {
                USED_POSITIONS.add(position);
            }
        }
    }
    
    protected final void addItselfToMenu() {
        this.contextMenu.add(this);
    }
    
    protected final boolean isPresentInMenu() {
        return this.contextMenu.isPresent(this);
    }
    
    protected final void removeItselfFromMenu() {
        this.contextMenu.remove(this);
    }
    
    protected BeamContextMenu menu() {
        return this.contextMenu;
    }
    
    protected final int prefferedPosition() {
        return this.position;
    }
    
    protected abstract void onContextMenuShow();
            
    protected abstract void onContextMenuHide(); 

    @Override
    public int compareTo(BeamContextMenuItem other) {
        if ( this.position < other.position) {
            return -1;
        } else if ( this.position > other.position ) {
            return 1;
        } else {
            return 0;
        }
    }
}
