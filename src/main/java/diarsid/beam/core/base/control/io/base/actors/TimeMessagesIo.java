/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.control.io.base.actors;

import diarsid.beam.core.base.control.io.base.interaction.TimeMessage;

import java.util.List;


/**
 *
 * @author Diarsid
 */
public interface TimeMessagesIo {
    
    void show(TimeMessage task);  
    
    void showAll(List<TimeMessage> tasks);  
    
    void showTasksNotification(String periodOfNotification, List<TimeMessage> tasks);
}
