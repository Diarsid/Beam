/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.io.gui;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.domain.entities.Task;

/**
 *
 * @author Diarsid
 */
public interface TasksGui {
    
    void show(Message message);
    
    void show(Task task);  
    
    void showAllSeparately(List<Task> tasks);  
    
    void showAllJointly(String header, List<Task> tasks);
    
}
