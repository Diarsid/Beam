/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui.javafx.console;

/**
 *
 * @author Diarsid
 */
interface ContextControlableConsole {
    
    void imitateCommandInput(String command);
    
    String text();
    
    int caretPosition();
    
    boolean isInDialog();
    
    boolean hasClearableContent();
    
    boolean hasNonDefaultSize();
    
    void clear();
    
    void hide();
    
    void toDefaultSize();
}
