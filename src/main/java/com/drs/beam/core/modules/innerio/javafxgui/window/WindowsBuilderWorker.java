/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.core.modules.innerio.javafxgui.window;

import java.util.List;

import com.drs.beam.core.modules.innerio.javafxgui.WindowController;
import com.drs.beam.core.modules.innerio.javafxgui.WindowSettingsProvider;
import com.drs.beam.core.modules.innerio.javafxgui.WindowsBuilder;
import com.drs.beam.core.modules.tasks.TaskMessage;

/**
 *
 * @author Diarsid
 */
public class WindowsBuilderWorker implements WindowsBuilder {
    
    public WindowsBuilderWorker() {
    }
    
    @Override
    public Runnable newMessageWindow(
            String[] message,
            WindowSettingsProvider provider, 
            WindowController controller) {
        
        return new PopupWindow(
            "Message", message, provider, controller);
    }
    
    @Override
    public Runnable newErrorWindow(
            String[] message,
            WindowSettingsProvider provider, 
            WindowController controller) {
        
        return new PopupWindow(
            "Error", message, provider, controller);
    }
    
    @Override
    public Runnable newTaskWindow(
            TaskMessage task, 
            WindowSettingsProvider provider, 
            WindowController controller) {
        
        return new TaskWindow(task, controller, provider);
    }
    
    @Override
    public Runnable newNotificationWindow(
            String period,
            List<TaskMessage> tasks, 
            WindowSettingsProvider provider, 
            WindowController controller) {
        
        return new TasksNotificationWindow(period, tasks, controller, provider);
    }
}
