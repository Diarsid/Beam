/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.modules.tasks;

import com.drs.beam.modules.data.DataManager;
import com.drs.beam.modules.io.InnerIOInterface;
import com.drs.beam.modules.data.dao.tasks.TasksDao;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/*
 * Pivotal program's class to operate with tasks.
 * Interacts with and logically connects program's database, input, output and time 
 * watching of active tasks. Defines the earliest task's time for Timer to watch it's 
 * time to perform it first. Defines the sequence of actions which should be logically 
 * performed by program during it`s working with tasks, storing them to database, 
 * getting them according to different criteria and so on.
 * Is responsible for initial database reading when program starts it's work.
 */
public class TaskManager implements TaskManagerInterface {
    // Fields =============================================================================
    private static TaskManager taskManager;
    
    private final InnerIOInterface ioEngine;
    private final TasksDao dao;
    private LocalDateTime firstTaskTime = null;    
    
    // Constructor ========================================================================
    private TaskManager(InnerIOInterface io, DataManager dataManager){
        this.ioEngine = io;
        this.dao = dataManager.getTasksDAO();    
        initWork();
    }

    // Methods ============================================================================
    
    public static void init(InnerIOInterface io, DataManager dataManager){
        if (taskManager == null){
            taskManager = new TaskManager(io, dataManager);
        }
    }
    
    public static TaskManager getTaskManager(){
        return taskManager;
    }
     
    LocalDateTime getFirstTaskTime(){
        return firstTaskTime;
    }
    
    public boolean isAnyTasks(){
        return (firstTaskTime != null);
    }
    
    private void refreshFirstTaskTime(){
        firstTaskTime = dao.getFirstTaskTime();        
    }
        
    /*
     * Method for initial database reading when program starts it's work 
     * after a period of it`s inactivity.
     */
    private void initWork(){        
            // If there is any problems with databse, returns -1.
            int newId = dao.getLastId();
            // If there are any storing tasks it is neccesay to get and perform 
            // all tasks whose time has been expired while storing. 
            if ( newId >= 0){
                Task.setInitId(newId);
                ArrayList<Task> tasks = dao.extractExpiredTasks(LocalDateTime.now());
                for(Task task : tasks){
                    performTask(task);
                }
                refreshFirstTaskTime();
                new Timer(this);
            } else {
                ioEngine.informAboutError("Unknown problem: tasks ID < 0", true);
            }                    
    }
    
    private void performTask(Task task){
        // Implies there can be more than two task types in future and they can have
        // different ways to be executed.
        executing: 
        switch(task.getType()){
            case (Task.USUAL_TASK) : {
                ioEngine.showTask(task);
                break executing;
            }
            case (Task.CALENDAR_EVENT) : {
                ioEngine.showTask(task);
                break executing;                
            }
            // case (Task.SOME_OTHER_TYPE) : {
        } 
    }    
    
    /*
     * Characters sequence '~}' is used as a delimiter between strings
     * when task`s text in string[] is saved into DB TEXT field.    
     */
    private boolean verifyTaskOnForbiddenChars(String[] text) {
        for (String s : text){
            if (s.contains("~}")){
                ioEngine.informAboutError("Text verifying: Forbidden characters '~}' was inputted!", false);
                return false;
            }                                
        }
        return true;
    }
    
    /*
     * Characters sequence '~}' is used as a delimiter between task`s text strings that 
     * was saved into DB TEXT field. 
     */
    private boolean verifyTextOnForbiddenChars(String text) {
        if (text.contains("~}")){
            return false;
        }
        return true;
    }
    
    /*
     * Parses LocalDateTime according to appropriate format.
     * Boolean mustBeFuture == true means parsed time value 
     * cannot be before current moment of time.
     */    
    private LocalDateTime ofFormat(String timeString, boolean mustBeFuture){
        LocalDateTime time = null;
        parsing: try{
            // get length of incoming string to define it's format
            switch (timeString.length()){
                // time format: dd-MM-uuuu HH:mm - 16 chars
                // full format
                case (16) : {                    
                    time = LocalDateTime.parse(
                            timeString,
                            DateTimeFormatter.ofPattern(Task.DB_TIME_PATTERN));
                    break parsing;
                }
                // time format: +MM - 3 chars
                // specifies time in minutes, which is added to current time-date like timer
                case (3) : {                    
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .plusMinutes(Integer.parseInt(timeString.substring(1,3)));
                    break parsing;
                }
                // time format: HH:MM - 5 chars
                // specifies today's hours and minutes
                case (5) : {                    
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withHour(Integer.parseInt(timeString.substring(0,2)))
                            .withMinute(Integer.parseInt(timeString.substring(3,5)));
                    break parsing;
                }
                // time format: +HH:MM - 6 chars
                // specifies time in hours and minutes, which is added to current time-date like timer
                case (6) : {                    
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .plusHours(Integer.parseInt(timeString.substring(1,3)))
                            .plusMinutes(Integer.parseInt(timeString.substring(4,6)));
                    break parsing;
                }
                // time format: dd HH:MM - 8 chars
                // specifies hours, minutes and day of current month
                case (8) : {                    
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withHour(Integer.parseInt(timeString.substring(3,5)))
                            .withMinute(Integer.parseInt(timeString.substring(6,8)));
                    break parsing;
                }
                // time format: dd-mm HH:MM - 11 chars
                // specifies hours, minutes, day and month of current year
                case (11) : {                    
                    time = LocalDateTime.now().withSecond(00).withNano(000)
                            .withDayOfMonth(Integer.parseInt(timeString.substring(0,2)))
                            .withMonth(Integer.parseInt(timeString.substring(3,5)))
                            .withHour(Integer.parseInt(timeString.substring(6,8)))
                            .withMinute(Integer.parseInt(timeString.substring(9,11)));
                    break parsing;
                }
                default: {
                    ioEngine.informAboutError("Time verifying: Unrecognizable time format.",
                            false);
                    break parsing;
                }
            }
        } catch (DateTimeParseException e){
            ioEngine.informAboutError("Time verifying: Wrong time format.", false);
            return null;
        } catch (NumberFormatException e){            
            ioEngine.informAboutError("Time verifying: Wrong characters have been inputted!",
                    false);
            return null;
        }
        
        // If given time format is OK and time was parsed correctly, it is required to test
        // this time whether it is future.\
        //
        // If argument mustBeFuture is TRUE, method should return NULL instead of parsed 
        // time if it represents past date. If mustBeFuture is FALSE, it is allowed to return 
        // time which represents past date.
        if (time == null){            
            return null;
        } else if (mustBeFuture && time.isBefore(LocalDateTime.now())){
            ioEngine.informAboutError("Time verifying: Given time is past. It must be future!", false);
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
    
    // Methods implements TaskManagerInterface interface -----------------------------------------
    @Override
    public void createNewTask(String timeString, String[] task) throws RemoteException{
        LocalDateTime time = ofFormat(timeString, true);
        if (time != null && verifyTaskOnForbiddenChars(task)){
            Task newTask = Task.newTask("task", time, task);        
            dao.saveTask(newTask);                    
            refreshFirstTaskTime();
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
            ioEngine.informAboutError("Text verifying: Forbidden characters '~}' was inputted!", false);
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
