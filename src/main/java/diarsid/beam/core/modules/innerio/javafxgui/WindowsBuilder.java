/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.innerio.javafxgui;

import java.util.List;

import diarsid.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
public interface WindowsBuilder {
    
    Runnable newMessageWindow(
            String[] message,
            WindowResourcesProvider provider, 
            WindowController controller);
    
    Runnable newErrorWindow(
            String[] message,
            WindowResourcesProvider provider, 
            WindowController controller);
    
    Runnable newTaskWindow(
            TaskMessage task, 
            WindowResourcesProvider provider, 
            WindowController controller);
    
    Runnable newNotificationWindow(
            String period,
            List<TaskMessage> tasks, 
            WindowResourcesProvider provider, 
            WindowController controller);
}
