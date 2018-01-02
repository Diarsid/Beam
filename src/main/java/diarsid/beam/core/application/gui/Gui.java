/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.application.gui;

public interface Gui {
    
    OutputMessagesGui messagesGui();
    
    OutputTasksGui tasksGui();
    
    InteractionGui interactionGui();
        
    void exitAfterAllWindowsClosed();
}
