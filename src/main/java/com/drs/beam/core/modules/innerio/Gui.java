/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.innerio;

import com.drs.beam.core.modules.tasks.TaskMessage;

/*
 * 'Native' program`s output interface.
 *
 * Used to output messages from program through native output mechanism when external UI 
 * (e.g. console)is unavailable or output configuration was set not to use it.
 *
 * Is implemented with JavaFX or Swing technology which is choosed from config.xml.
 */
public interface Gui{
    
    /*
     * Method used to show user`s tasks.
     */
    public void showTask(TaskMessage task);
    
    /*
     * Used to show program`s messages, typically errors, warnings or notifications.
     * Param isCritical defines whether program should be closed after this event.
     */
    public void showMessage(String[] message); 
    
    /*
     * Used to show program`s occured exceptions.
     * Param isCritical defines whether program should be closed after this event.
     */
    public void showError(String[] error);
    
    public void exitAfterAllWindowsClosed();
}
