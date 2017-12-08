/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.application.gui;

import java.util.List;

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.domain.entities.Picture;

public interface Gui {
    
    void showTask(TaskMessage task);
    
    void showTasks(String description, List<TaskMessage> tasks);
    
    void showMessage(Message message); 
        
    void exitAfterAllWindowsClosed();
    
    ValueFlow<Picture> capturePictureOnScreen(String imageName);
}
