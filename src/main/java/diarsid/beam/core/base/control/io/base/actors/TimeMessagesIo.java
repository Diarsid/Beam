/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import diarsid.beam.core.base.control.io.base.interaction.TaskMessage;

import java.util.List;


/**
 *
 * @author Diarsid
 */
public interface TimeMessagesIo {
    
    void show(TaskMessage task);  
    
    void showAll(List<TaskMessage> tasks);  
    
    void showTasksNotification(String periodOfNotification, List<TaskMessage> tasks);
}