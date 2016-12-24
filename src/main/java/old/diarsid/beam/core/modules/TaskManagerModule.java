/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package old.diarsid.beam.core.modules;

import java.util.List;
import java.util.Set;

import diarsid.beam.core.modules.tasks.TimeMessage;
import diarsid.beam.core.modules.tasks.TaskType;
import diarsid.beam.core.StoppableBeamModule;

/**
 * Is responsible for tasks management and execution.
 * 
 * @author Diarsid
 */
public interface TaskManagerModule extends StoppableBeamModule {
    
    boolean createNewTask(
            TaskType type, 
            String time, 
            String[] task, 
            Set<Integer> days, 
            Set<Integer> hours);
    
    String getFirstAlarmTime();
    
    List<TimeMessage> getFutureTasks();
    List<TimeMessage> getPastTasks();
    List<TimeMessage> getFirstTask();
    
    List<TimeMessage> getActualReminders();
    List<TimeMessage> getActualEvents();
    
    boolean deleteTaskByText(String text);

    boolean removeAllTasks();
    boolean removeAllFutureTasks();
    boolean removeAllPastTasks();
    
    /*
    boolean suspendTask(String text);
    boolean activateSuspendedTask(String text);
    */
}
