/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.modules.io.gui.swing;

import com.drs.beam.modules.io.gui.Gui;
import com.drs.beam.modules.io.gui.swing.ExceptionWindowSwing;
import com.drs.beam.modules.io.gui.swing.MessageWindowSwing;
import com.drs.beam.modules.io.gui.swing.TaskWindowSwing;
import com.drs.beam.modules.tasks.Task;

/*
 * Main class for running pgogram 'native' Swing GUI.
 * Creates and invokes appropriate Swing windows.
 */
public class GuiSwing implements Gui{
    
    @Override
    public void showTask(Task task){
        TaskWindowSwing window = new TaskWindowSwing();
        window.invoke(task);
    }
    
    @Override
    public void showMessage(String message, boolean isCritical){
        MessageWindowSwing window = new MessageWindowSwing();
        window.invoke(message, isCritical);
    } 
    
    @Override
    public void showException(Exception e, boolean isCritical){
        ExceptionWindowSwing window = new ExceptionWindowSwing();
        window.invoke(e, isCritical);
    }
}
