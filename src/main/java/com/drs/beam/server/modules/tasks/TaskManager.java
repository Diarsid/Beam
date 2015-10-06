/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.server.modules.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import com.drs.beam.server.entities.task.Task;
import com.drs.beam.server.entities.task.util.TaskTimeFormatter;
import com.drs.beam.server.entities.task.util.TaskVerifier;
import com.drs.beam.server.entities.task.util.exceptions.TaskTimeFormatInvalidException;
import com.drs.beam.server.entities.task.util.exceptions.TaskTimeInvalidException;
import com.drs.beam.server.modules.Modules;
import com.drs.beam.server.modules.data.DataManagerModule;
import com.drs.beam.server.modules.data.dao.tasks.TasksDao;
import com.drs.beam.server.modules.io.InnerIOModule;

/**
 * Pivotal program's class to operate with tasks.
 * Interacts with and logically connects program's database, input, output and time 
 * watching of active tasks. Defines the earliest task time for Timer to watch it's 
 * time to perform it first. Defines the sequence of actions which should be logically 
 * performed by program during it`s working with tasks, storing them to database, 
 * getting them according to different criteria and so on.
 * Is responsible for initial database reading when program starts it's work.
 */
public class TaskManager implements TaskManagerModule {
    // Fields =============================================================================
    private static TaskManager taskManager;    
    
    private final InnerIOModule ioEngine;
    private final TasksDao tasksDao;
    private final Object lock;
    private final TaskVerifier taskVerifier;
    private final TaskTimeFormatter formatter;
    
    // Time of expiration of the earliest task.
    // It is usually updated after every CRUD operation with tasks in data storage 
    // through refreshFirstTaskTime() method and observed by Timer instance 
    // to execute appropriate tasks in time.
    private LocalDateTime firstTaskTime;
    
    // Constructor ========================================================================
    private TaskManager(InnerIOModule io, DataManagerModule dataManager){
        this.ioEngine = io;
        this.tasksDao = dataManager.getTasksDao();
        this.lock = new Object();
        this.taskVerifier = new TaskVerifier();
        this.formatter = new TaskTimeFormatter();
        this.firstTaskTime = null;
    }

    // Methods ============================================================================
    
    public static void initAndRegister(InnerIOModule io, DataManagerModule dataManager){
        if (taskManager == null){
            taskManager = new TaskManager(io, dataManager);
            taskManager.beginWork();
            Modules.registerModule(TaskManagerModule.getModuleName(), taskManager);
        }
    }
         
    LocalDateTime getFirstTaskTime(){
        return this.firstTaskTime;
    }
    
    boolean isAnyTasks(){
        return (this.firstTaskTime != null);
    }
    
    private void refreshFirstTaskTime(){
        synchronized (this.lock){
            this.firstTaskTime = this.tasksDao.getFirstTaskTime();
        }                
    }
        
    /*
     * Method for initial database reading when program starts it's work 
     * after a period of it`s inactivity.
     */
    private void beginWork(){        
        ArrayList<Task> tasks = this.tasksDao.extractExpiredTasks(LocalDateTime.now());
        for(Task task : tasks){
            this.performTask(task);
        }
        this.refreshFirstTaskTime();
        (new Thread(new Timer(this), "Timer")).start();                  
    }
    
    private void performTask(Task task){
        // Implies there can be more than two task types in future and they can have
        // different ways to be executed.
        executing: switch(task.getType()){
            case (Task.USUAL_TASK) : {
                this.ioEngine.showTask(task);
                break executing;
            }
            case (Task.CALENDAR_EVENT) : {
                this.ioEngine.showTask(task);
                break executing;                
            }
            // case (Task.SOME_OTHER_TYPE) : { }
            default : {
                this.ioEngine.reportError("Unknown task`s type.");
            }
        } 
    }      
       

    
    // method to perform task, when it's time comes
    void performFirstTask(){
        ArrayList<Task> tasks = this.tasksDao.extractFirstTasks();
        this.refreshFirstTaskTime();
        for (Task task : tasks){
            this.performTask(task);
        }        
    }
    
    // Methods implements RmiTaskManagerInterface interface -----------------------------------------
    @Override
    public void createNewTask(String timeString, String[] task){
        try {
            LocalDateTime time = this.formatter.ofFormat(timeString, true);
            boolean taskValid = this.taskVerifier.verifyTaskOnForbiddenChars(task);
            if (taskValid){
                this.tasksDao.saveTask(new Task(Task.USUAL_TASK, time, task));                    
                this.refreshFirstTaskTime();
            } else {
                this.ioEngine.reportMessage("Text verifying: Forbidden characters '~}' was inputted!");
            }
        } catch (TaskTimeFormatInvalidException e){
            this.ioEngine.reportMessage("Time verifying: Unrecognizable time format.");
        } catch (TaskTimeInvalidException e){
            this.ioEngine.reportMessage("Time verifying: Given time is past. It must be future!");
        } catch (NumberFormatException e){
            this.ioEngine.reportMessage("Time verifying: Wrong characters have been inputted!");
        } catch (DateTimeParseException e){
            this.ioEngine.reportMessage("Time verifying: Wrong time format.");
        }    
    }
    
    @Override
    public String getFirstAlarmTime(){
        if (this.firstTaskTime != null){
            return this.formatter.outputTimePatternFormat(this.firstTaskTime);
        } else {
            return "there aren't tasks now.";
        }
    }
    
    @Override
    public ArrayList<Task> getFutureTasks(){
        // value 1 to reportInfo that we need only future tasks
        return this.tasksDao.getTasks(1);
    }
    
    @Override
    public ArrayList<Task> getPastTasks(){        
        // value -1 to reportInfo that we need only past tasks
        return this.tasksDao.getTasks(-1);
    }
    
    @Override
    public ArrayList<Task> getFirstTask(){        
        return this.tasksDao.getTasksByTime(this.firstTaskTime);
    }
    
    @Override
    public boolean deleteTaskByText(String text){         
        if (this.taskVerifier.verifyTextOnForbiddenChars(text)){
            boolean result = this.tasksDao.deleteTaskByText(text);
            this.refreshFirstTaskTime();
            return result;
        } else{
            this.ioEngine.reportError("Text verifying: Forbidden characters '~}' was inputted!");
            return false;
        }     
    }
    
    @Override
    public boolean removeAllTasks(){
        boolean result;
        result = this.tasksDao.deleteTasks(0);
        this.refreshFirstTaskTime();
        return result;
    }
    
    @Override
    public boolean removeAllFutureTasks(){
        boolean result;
        result = this.tasksDao.deleteTasks(1);
        this.refreshFirstTaskTime();
        return result;
    }
    
    @Override
    public boolean removeAllPastTasks(){
        boolean result;
        result = this.tasksDao.deleteTasks(-1);
        this.refreshFirstTaskTime();
        return result;
    }
}
