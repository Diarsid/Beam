/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.javafxgui;

import java.util.List;

import diarsid.beam.core.modules.tasks.TimeMessage;

/**
 *
 * @author Diarsid
 */
public interface WindowsBuilder {
    
    Runnable newMessageWindow(
            String[] message,
            WindowResources resources, 
            WindowController controller);
    
    Runnable newErrorWindow(
            String[] message,
            WindowResources resources, 
            WindowController controller);
    
    ReusableTaskWindow newTaskWindow(
            TimeMessage task, 
            WindowResources resources, 
            WindowController controller);
    
    Runnable newNotificationWindow(
            String period,
            List<TimeMessage> tasks, 
            WindowResources resources, 
            WindowController controller);
}