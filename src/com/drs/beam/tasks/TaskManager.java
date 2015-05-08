package com.drs.beam.tasks;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.tasks.dao.TasksDao;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Diarsid
 * Date: 02.09.14
 * Time: 0:47
 * To change this template use File | Settings | File Templates.
 */
    /*
    Pivotal program's class to operate with tasks.
    Interacts with and logically connects program's database, input, output and time watching of active tasks.
    Defines the earliest task's time for Timer to watch it's time to perform it first. Defines the sequence of actions
    which should be logically performed by program during it`s working with tasks.
    Is responsible for initial database reading when program starts it's work.
     */
public class TaskManager implements TaskManagerIF {
    // Fields =============================================================================
    private final InnerIOIF     ioEngine;
    private final InputVerifier  verifier;
    private final TasksDao      dao;
    
    private LocalDateTime firstTaskTime = null;
    
    // temporary often used fields
    private Task            tempTask;
    private LocalDateTime    tempTime = null;
    
    // Constructor ========================================================================
    // Constructor provides necessary DBManager and Timer objects for this TaskManager
    // reads database when program starts
    public TaskManager(){
        this.ioEngine = BeamIO.getInnerIO();
        this.verifier = new InputVerifier();
        this.dao = TasksDao.getDao();
        this.initWork();
        new Timer(this);
    }

    // Methods ============================================================================

    // getters
    LocalDateTime getFirstTaskTime(){
        return firstTaskTime;
    }
    
    private void refreshFirstTaskTime(){
        firstTaskTime = dao.getFirstTaskTime();        
    }
    // initial database reading when program starts it's work
    private void initWork(){
        if (!dao.isDBinitialized()){
            dao.initTasksTable();
        } else{
            int newId = dao.getLastId();
            if ( newId >= 0){
                Task.setInitId(newId);
                ArrayList<Task> tasks = dao.extractExpiredTasks(LocalDateTime.now());
                for(Task task : tasks){
                    performTask(task);
                }
                refreshFirstTaskTime();
            } else {
                System.exit(1);                
            }
            
        }            
    }
    private void performTask(Task task){
        executing: switch(task.getType()){
            case (Task.USUAL_TASK) : {
                ioEngine.showTask(task);
                break executing;
            }
            case (Task.CALENDAR_EVENT) : {
                ioEngine.showTask(task);
                break executing;                
            }
        } 
    }
    // method to perform task, when it's time comes
    void performFirstTask(){
        ArrayList<Task> tasks = dao.extractFirstTasks();
        refreshFirstTaskTime();
        for (Task task : tasks){
            performTask(task);
        }        
    }
    
    @Override
    public void createNewTask(String time, String[] task) throws RemoteException{
        try{
            tempTime = verifier.verifyTimeFormat(time, true);
            verifier.verifyTask(task);
        }catch (VerifyFailureException e){
            ioEngine.informAboutError(e.getVerifyMessage());
            return;
        }
        tempTask = Task.newTask("task", tempTime, task);        
        dao.saveTask(tempTask);                    
        refreshFirstTaskTime();
        
        tempTask = null;
        tempTime = null;
    }

    // checks whether program has any active tasks to watch their time
    public boolean isAnyTasks(){
        return (firstTaskTime != null);
    }
    
    @Override
    public String getFirstAlarmTime() throws RemoteException{
        if (isAnyTasks()){
            return firstTaskTime.format(DateTimeFormatter.ofPattern(Task.OUTPUT_TIME_PATTERN));
        } else {
            return "there aren't tasks now.";
        }
    }
    
    @Override
    public ArrayList<Task> getFutureTasks() throws RemoteException{
        // value 1 to inform that we need only future tasks
        return dao.getTasks(1);
    }
    
    @Override
    public ArrayList<Task> getPastTasks() throws RemoteException{        
        // value -1 to inform that we need only future tasks
        return dao.getTasks(-1);
    }
    
    @Override
    public ArrayList<Task> getFirstTask() throws RemoteException{        
        return dao.getTasksByTime(firstTaskTime);
    }
    
    @Override
    public boolean deleteTaskByText(String text) throws RemoteException{        
        try {
            boolean result;
            verifier.verifyText(text);
            result = dao.deleteTaskByText(text);
            refreshFirstTaskTime();
            return result;
        } catch (VerifyFailureException e) {
            ioEngine.informAboutError(e.getVerifyMessage());
            return false;
        }        
    }
    
    @Override
    public boolean removeAllTasks() throws RemoteException{
        boolean result;
        result = dao.deleteTasks(0);
        refreshFirstTaskTime();
        return result;
    }
    
    @Override
    public boolean removeAllFutureTasks() throws RemoteException{
        boolean result;
        result = dao.deleteTasks(1);
        refreshFirstTaskTime();
        return result;
    }
    
    @Override
    public boolean removeAllPastTasks() throws RemoteException{
        boolean result;
        result = dao.deleteTasks(-1);
        refreshFirstTaskTime();
        return result;
    }
}
