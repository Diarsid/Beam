/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.gui.javafx.window;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;
import diarsid.beam.core.application.gui.javafx.ReusableTaskWindow;
import diarsid.beam.core.application.gui.javafx.WindowController;
import diarsid.beam.core.application.gui.javafx.WindowResources;
import diarsid.beam.core.application.gui.javafx.WindowsBuilder;

/**
 *
 * @author Diarsid
 */
public class WindowsBuilderWorker implements WindowsBuilder {
    
    public WindowsBuilderWorker() {
    }
    
    @Override
    public Runnable newMessageWindow(
            List<String> message,
            WindowResources resources, 
            WindowController controller) {
        
        return new PopupWindow(
            "Message", message, resources, controller);
    }
    
    @Override
    public Runnable newErrorWindow(
            List<String> message,
            WindowResources resources, 
            WindowController controller) {
        
        return new PopupWindow(
            "Error", message, resources, controller);
    }
    
    @Override
    public ReusableTaskWindow newTaskWindow(
            TaskMessage task, 
            WindowResources resources, 
            WindowController controller) {
        
        return new TaskWindow(task, controller, resources);
    }
    
    @Override
    public Runnable newNotificationWindow(
            String period,
            List<TaskMessage> tasks, 
            WindowResources resources, 
            WindowController controller) {
        
        return new TasksNotificationWindow(period, tasks, controller, resources);
    }
}
