/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.StopableBeamModule;
import diarsid.beam.core.modules.tasks.TaskMessage;
import diarsid.beam.core.modules.tasks.TaskType;

/**
 * Is responsible for tasks management and execution.
 * 
 * @author Diarsid
 */
public interface TaskManagerModule extends StopableBeamModule {
    
    boolean createNewTask(
            TaskType type, 
            String time, 
            String[] task, 
            Set<Integer> days, 
            Set<Integer> hours);
    
    String getFirstAlarmTime();
    
    List<TaskMessage> getFutureTasks();
    List<TaskMessage> getPastTasks();
    List<TaskMessage> getFirstTask();
    
    List<TaskMessage> getActualReminders();
    List<TaskMessage> getActualEvents();
    
    boolean deleteTaskByText(String text);

    boolean removeAllTasks();
    boolean removeAllFutureTasks();
    boolean removeAllPastTasks();
    
    /*
    boolean suspendTask(String text);
    boolean activateSuspendedTask(String text);
    */
}
