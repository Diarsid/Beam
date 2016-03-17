/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.innerio.javafxgui.window;

import java.util.List;

import diarsid.beam.core.modules.innerio.javafxgui.ReusableTaskWindow;
import diarsid.beam.core.modules.innerio.javafxgui.WindowController;
import diarsid.beam.core.modules.innerio.javafxgui.WindowResources;
import diarsid.beam.core.modules.innerio.javafxgui.WindowsBuilder;
import diarsid.beam.core.modules.tasks.TaskMessage;

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
            WindowResources resources, 
            WindowController controller) {
        
        return new PopupWindow(
            "Message", message, resources, controller);
    }
    
    @Override
    public Runnable newErrorWindow(
            String[] message,
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
