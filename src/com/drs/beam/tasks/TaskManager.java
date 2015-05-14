/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.tasks;

import com.drs.beam.io.BeamIO;
import com.drs.beam.io.InnerIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.tasks.dao.TasksDao;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

    /*
    Pivotal program's class to operate with tasks.
    Interacts with and logically connects program's database, input, output and time watching of active tasks.
    Defines the earliest task's time for Timer to watch it's time to perform it first. Defines the sequence of actions
    which should be logically performed by program during it`s working with tasks.
    Is responsible for initial database reading when program starts it's work.
     */
public class TaskManager implements TaskManagerIF {
    // Fields =============================================================================
    private final InnerIOIF ioEngine;
    private final TasksDao  dao;
    private LocalDateTime   firstTaskTime = null;    
    
    // Constructor ========================================================================
    // Constructor provides necessary DBManager and Timer objects for this TaskManager
    // reads database when program starts
    public TaskManager(){
        this.ioEngine = BeamIO.getInnerIO();
        this.dao = TasksDao.getDao();
        this.initWork();
        new Timer(this);
    }

    // Methods ============================================================================

    /*
    * 
    */ 
    LocalDateTime getFirstTaskTime(){
        return firstTaskTime;
    }
    
    // checks whether program has any active tasks to watch their time
    public boolean isAnyTasks(){
        return (firstTaskTime != null);
    }
    
    private void refreshFirstTaskTime(){
        firstTaskTime = dao.getFirstTaskTime();        
    }
    
    /*
    * Method for initial database reading when program starts it's work.
    */    
    private void initWork(){
        if (!dao.isDBinitialized()){
            dao.initTasksTable();
        } else{
            // If there is any problems with databse, returns -1.
            int newId = dao.getLastId();
            // If there are any storing tasks get and perform all tasks whose time 
            // has been expired.
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
    
    // Characters sequence '~}' is used as a delimiter between strings
    // when string[] is saved into DB TEXT field.    
    private boolean verifyTaskOnForbiddenChars(String[] text) {
        for (String s : text){
            if (s.contains("~}"))
                return false;
        }
        return true;
    }

    private boolean verifyTextOnForbiddenChars(String text) {
        if (text.contains("~}")){
            return false;
        }
        return true;
    }
    
    private LocalDateTime ofFormat(String timeString, boolean mustBeFuture){
        LocalDateTime time = null;
        parsing: try{
            // get length of incoming string to define it's format
            switch (timeString.length()){
                case (16) : {
                    // time format: dd-MM-uuuu HH:mm
                    // full format
                    time = LocalDateTime.parse(
                            timeString,
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
                    break parsing;
                }
                case (5) : {
                    // time format: HH:MM
                    // specifies today's hours and minutes
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withHour(Integer.parseInt(timeString.substring(0,2)))
                            .withMinute(Integer.parseInt(timeString.substring(3,5)));
                    break parsing;
                }
                case (6) : {
                    // time format: +HH:MM
                    // specifies time in hours and minutes, which is added to current time-date like timer
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .plusHours(Integer.parseInt(timeString.substring(1,3)))
                            .plusMinutes(Integer.parseInt(timeString.substring(4,6)));
                    break parsing;
                }
                case (8) : {
                    // time format: dd HH:MM
                    // specifies hours, minutes and day of current month
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withHour(Integer.parseInt(timeString.substring(3,5)))
                            .withMinute(Integer.parseInt(timeString.substring(6,8)));
                    break parsing;
                }
                case (11) : {
                    // time format: dd-mm HH:MM
                    // specifies hours, minutes, day and month of current year
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withMonth(Integer.parseInt(timeString.substring(3,5)))
                            .withHour(Integer.parseInt(timeString.substring(6,8)))
                            .withMinute(Integer.parseInt(timeString.substring(9,11)));
                    break parsing;
                }
                default: {
                    ioEngine.informAboutError("Time verifying: Unrecognizable time format.");
                    break parsing;
                }
            }
        } catch (DateTimeParseException e){
            ioEngine.informAboutError("Time verifying: Wrong time format.");
            return null;
        } catch (NumberFormatException e){            
            ioEngine.informAboutError("Time verifying: Wrong characters have been inputted!");
            return null;
        }
        
        if (time == null){            
            return null;
        } else if (mustBeFuture && time.isBefore(LocalDateTime.now())){
            ioEngine.informAboutError("Time verifying: Given time is past. It must be future!");
            return null;
        } else {
            return time;
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
    
    // Methods implements TaskManagerIF interface -----------------------------------------
    @Override
    public void createNewTask(String timeString, String[] task) throws RemoteException{
        LocalDateTime time = ofFormat(timeString, true);
        if (time == null){
            ioEngine.informAboutError("Time veryfying: time parsing method has returned NULL");
            return;
        }
        if (verifyTaskOnForbiddenChars(task)){
            Task newTask = Task.newTask("task", time, task);        
            dao.saveTask(newTask);                    
            refreshFirstTaskTime();
        } else {
            ioEngine.informAboutError("Text verifying: Forbidden characters '~}' was inputted!");            
        }
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
        // value -1 to inform that we need only past tasks
        return dao.getTasks(-1);
    }
    
    @Override
    public ArrayList<Task> getFirstTask() throws RemoteException{        
        return dao.getTasksByTime(firstTaskTime);
    }
    
    @Override
    public boolean deleteTaskByText(String text) throws RemoteException{ 
        boolean result;
        if (verifyTextOnForbiddenChars(text)){
            result = dao.deleteTaskByText(text);
            refreshFirstTaskTime();
            return result;
        } else{
            ioEngine.informAboutError("Text verifying: Forbidden characters '~}' was inputted!");
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
