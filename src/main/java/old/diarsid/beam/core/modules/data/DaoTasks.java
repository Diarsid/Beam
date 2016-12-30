/*
 * project: Beam
 * author: Diarsid
 */
package old.diarsid.beam.core.modules.data;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import diarsid.beam.core.modules.tasks.Task;
import diarsid.beam.core.modules.tasks.TimeMessage;

/**
 *
 * @author Diarsid
 */
public interface DaoTasks { 
    
    String DB_TIME_PATTERN = "uuuu-MM-dd HH:mm";
    
    LocalDateTime addTask(Task task); 
    
    LocalDateTime deleteTaskByText(String text); 
    
    LocalDateTime deleteAllTasks();
    LocalDateTime deleteActualTasks();
    LocalDateTime deleteNonActualTasks();
        
    List<Task> getFirstTasks(); 
    List<Task> getExpiredTasks(LocalDateTime fromNow); 
    LocalDateTime updateTasksAndGetNextFirstTime(List<Task> tasksToUpdate);
   
    LocalDateTime getFirstTaskTime(); 
    
    List<TimeMessage> getAllTasks();
    List<TimeMessage> getNonActualTasks();
    List<TimeMessage> getActualTasks();
    
    List<TimeMessage> getActualEvents();
    List<TimeMessage> getActualReminders();
    
    List<TimeMessage> getTasksByTime(LocalDateTime firstTaskTime); 
    
    List<TimeMessage> getCalendarTasksBetweenDates(LocalDateTime from, LocalDateTime to);
}
