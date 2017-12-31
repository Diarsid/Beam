/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.application.gui;

import diarsid.beam.core.application.gui.javafx.GuiJavaFXResources;

public interface Gui {
    
    OutputMessagesGui messagesGui();
    
    OutputTasksGui tasksGui();
    
    InteractionGui interactionGui();
    
    GuiJavaFXResources resources();
        
    void exitAfterAllWindowsClosed();
}
