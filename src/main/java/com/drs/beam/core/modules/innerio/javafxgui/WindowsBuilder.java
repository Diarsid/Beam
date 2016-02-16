/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.innerio.javafxgui;

import java.util.List;

import com.drs.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
public interface WindowsBuilder {
    
    Runnable newMessageWindow(
            String[] message,
            WindowSettingsProvider provider, 
            WindowController controller);
    
    Runnable newErrorWindow(
            String[] message,
            WindowSettingsProvider provider, 
            WindowController controller);
    
    Runnable newTaskWindow(
            TaskMessage task, 
            WindowSettingsProvider provider, 
            WindowController controller);
    
    Runnable newNotificationWindow(
            String period,
            List<TaskMessage> tasks, 
            WindowSettingsProvider provider, 
            WindowController controller);
}
