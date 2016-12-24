/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.control.io.base;

import java.util.List;

import diarsid.beam.core.modules.tasks.TimeMessage;

/**
 *
 * @author Diarsid
 */
public interface TimeScheduledIo {
    
    void showTask(TimeMessage task);  
    
    void showTasksNotification(String periodOfNotification, List<TimeMessage> tasks);
}
