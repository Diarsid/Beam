/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.io.gui;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.console.ConsoleBlockingExecutor;
import diarsid.beam.core.base.control.io.base.console.ConsolePlatform;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Picture;

public interface Gui {
    
    MessagesGui messagesGui();
    
    TasksGui tasksGui();
    
    ValueFlow<Picture> capturePictureOnScreen(String imageName);
    
    ConsolePlatform guiConsolePlatformFor(ConsoleBlockingExecutor blockingExecutor)
            throws DataExtractionException;
        
    void exitAfterAllWindowsClosed();
}
