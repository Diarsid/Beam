/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.io;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;

/*
 * 'Native' program`s output interface.
 *
 * Used to output messages from program through native output mechanism when external UI 
 * (e.g. console)is unavailable or output configuration was set not to use it.
 *
 * Is implemented with JavaFX or Swing technology which is choosed from config.xml.
 */
public interface Gui {
    
    void stopJavaFXPlatform();
    
    /*
     * Method used to show user`s tasks.
     */
    void showTask(TaskMessage task);
    
    /*
     * Method to show bunch of tasks to user.
     */
    void showTasks(String description, List<TaskMessage> tasks);
    
    /*
     * Used to show program`s messages, typically errors, warnings or notifications.
     */
    void showMessage(Message message); 
        
    /*
     * Signals to underlying GUI system that it should invoke a prorgam
     * cancelation after the last window is closed.
     */
    void exitAfterAllWindowsClosed();
}
