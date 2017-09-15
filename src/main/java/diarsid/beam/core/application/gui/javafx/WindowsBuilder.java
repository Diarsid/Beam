/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.application.gui.javafx;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;

/**
 *
 * @author Diarsid
 */
public interface WindowsBuilder {
    
    Runnable newMessageWindow(
            List<String> message,
            WindowResources resources, 
            WindowController controller);
    
    Runnable newErrorWindow(
            List<String> message,
            WindowResources resources, 
            WindowController controller);
    
    ReusableTaskWindow newTaskWindow(
            TaskMessage task, 
            WindowResources resources, 
            WindowController controller);
    
    Runnable newNotificationWindow(
            String period,
            List<TaskMessage> tasks, 
            WindowResources resources, 
            WindowController controller);
}
