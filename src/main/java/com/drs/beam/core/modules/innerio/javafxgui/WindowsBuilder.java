/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.innerio.javafxgui;

import com.drs.beam.core.modules.tasks.Task;

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
            Task task, 
            WindowSettingsProvider provider, 
            WindowController controller);
}
