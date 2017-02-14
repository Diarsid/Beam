/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.io.javafxgui.window;

import java.util.List;

import diarsid.beam.core.modules.io.javafxgui.ReusableTaskWindow;
import diarsid.beam.core.modules.io.javafxgui.WindowController;
import diarsid.beam.core.modules.io.javafxgui.WindowResources;
import diarsid.beam.core.modules.io.javafxgui.WindowsBuilder;
import diarsid.beam.core.base.control.io.base.interaction.TimeMessage;

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
            TimeMessage task, 
            WindowResources resources, 
            WindowController controller) {
        
        return new TaskWindow(task, controller, resources);
    }
    
    @Override
    public Runnable newNotificationWindow(
            String period,
            List<TimeMessage> tasks, 
            WindowResources resources, 
            WindowController controller) {
        
        return new TasksNotificationWindow(period, tasks, controller, resources);
    }
}
