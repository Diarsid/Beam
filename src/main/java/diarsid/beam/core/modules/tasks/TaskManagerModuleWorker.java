/*
 * project: Beam
 * author: Diarsid
 */
package diarsid.beam.core.modules.tasks;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import diarsid.beam.core.exceptions.ModuleInitializationException;
import diarsid.beam.core.modules.IoInnerModule;
import diarsid.beam.core.modules.TaskManagerModule;
import diarsid.beam.core.modules.data.DaoTasks;
import diarsid.beam.core.modules.tasks.exceptions.TaskTimeFormatInvalidException;
import diarsid.beam.core.modules.tasks.exceptions.TaskTimeInvalidException;
import diarsid.beam.core.modules.tasks.exceptions.TaskTypeInvalidException;

import static diarsid.beam.core.modules.tasks.TaskType.DAILY;
import static diarsid.beam.core.modules.tasks.TaskType.HOURLY;

/**
 * Pivotal program's class intended to operate with tasks.
 * 
 * Interacts with and logically connects program's database, the way 
 * tasks are executed, scheduling of every next time of tasks execution.
 * 
 * Defines the logical sequence of actions that should be 
 * performed while tasks are executed, updated and other types of 
 * operation with tasks are performed.
 * 
 * Initially reads tasks from database when program starts it's work.
 * 
 * @author Diarsid
 */
class TaskManagerModuleWorker implements TaskManagerModule {
    
    private final IoInnerModule ioEngine;
    private final DaoTasks tasksDao;    
    private final TaskTimeFormatter formatter;    
    private final ScheduledThreadPoolExecutor scheduler;        
    private final Object taskExecutionLock;
    private final Object notificationLock;
    
    // contains the reference to Runnable scheduled in
    // this.scheduler for now and represents execution 
    // the earliest task.
    // Is refreshed every time when task or tasks are 
    // being executed.
    // Can be null, if there are no any tasks.
    private ScheduledFuture currentExecution;
    
    // contains  the reference to Runnable scheduled in
    // this.scheduler for now and represents time 
    // of user notification about weekly and monthly tasks;
    // Is refreshed every time when notification are 
    // being executed.
    private ScheduledFuture currentNotification;
    
    TaskManagerModuleWorker(
            IoInnerModule io,
            DaoTasks tasks,
            TaskTimeFormatter formatter,
            Object executionLock,
            Object notificationLock,
            ScheduledThreadPoolExecutor sheduler) {
        
        this.ioEngine = io;
        this.tasksDao = tasks;        
        this.formatter = formatter;
        this.scheduler = sheduler;
        this.taskExecutionLock = executionLock;
        this.notificationLock = notificationLock;
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.currentExecution = null;
        this.currentNotification = null;
    }
    
    @Override
    public void stopModule() {        
        this.scheduler.shutdown();
    }
    
    // TO DELETE
    LocalDateTime getFirstTaskTime() {
        return this.tasksDao.getFirstTaskTime();
    }
        
    /*
     * Method for initial database reading when program starts it's work 
     * after a period of it's inactivity.
     */
    void beginWork() {
        synchronized (this.taskExecutionLock) {
            LocalDateTime firstScheduled = this.tasksDao.getFirstTaskTime();
            if ( firstScheduled == null ) {
                throw new ModuleInitializationException("FirstTime obtained from " + 
                        "database during TaskModuleManagerWorker::beginWork() " + 
                        "is NULL.");
            }
            // If firstScheduled obtained from database is LocalDateTime.MIN
            // it means that there are no any active tasks to schedule them
            // for execution.
            if ( ! LocalDateTime.MIN.equals(firstScheduled) ) {
                long inactivePeriod = Duration.between(
                        firstScheduled, LocalDateTime.now())
                        .toMinutes();
                
                if ( inactivePeriod <= 30 ) {
                    this.processObtainedTasksAndUpdateTimer(
                            this.tasksDao.getExpiredTasks(LocalDateTime.now()));
                } else if ( (30 < inactivePeriod) && (inactivePeriod <= 60*24) ) {
                    // If lag is longer than one hour but no
                    // longer than day, do not show hourly 
                    // tasks that was expired while program
                    // or system was inactive, just update them.
                    this.processObtainedTasksWithoutHourlyAndUpdateTimer(
                            this.tasksDao.getExpiredTasks(LocalDateTime.now()));
                } else {
                    // if lag is longer than one day, do not 
                    // show both hourly and daily tasks that 
                    // was expired while program or system was
                    // inactive, just update them.
                    this.processObtainedTasksWithoutHourlyDailyAndUpdateTimer(
                            this.tasksDao.getExpiredTasks(LocalDateTime.now()));
                }
            }
        }
        synchronized (this.notificationLock) {
            this.scheduleNextRegularTasksSurvey();
            LocalDateTime thisWeekBeginning =  LocalDateTime.now()
                    .withHour(12)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .with(DayOfWeek.MONDAY);
            this.notifyUserAboutThisWeekTasks(thisWeekBeginning);
        }        
    }
    
    /**
     * Program notifies its user about every non-hourly and 
     * non-daily tasks that have been scheduled earlier.
     * It notifies user about upcoming tasks in two cases - 
     * at every Monday and at every 1-st day of month.
     * 
     * This method schedules new time when user should be notified  
     * about tasks that will be executed during upcoming month or 
     * week which has just begun.
     * 
     * Set time of new notification to next Monday, 12:00:00:000 or
     * to next month's first day, 12:00:00:000, depending on which
     * time is earlier.
     */
    private void scheduleNextRegularTasksSurvey() {
        synchronized (this.notificationLock) {
            // if there is older notification have been scheduled
            // clear them.
            if ( this.currentNotification != null && !this.currentNotification.isDone() ) {
                this.currentNotification.cancel(false);
                this.currentNotification = null;
            }
            
            // get time of next Monday, 12:00:00:000
            LocalDateTime nextWeekBeginning = LocalDateTime.now()
                    .withHour(12)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .plusWeeks(1)
                    .with(DayOfWeek.MONDAY);
            
            // get time of next month's first day, 12:00:00:000
            LocalDateTime nextMonthBeginning = LocalDateTime.now()
                    .withHour(12)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0)
                    .plusMonths(1)
                    .withDayOfMonth(1);
            // what happens earlier - begining of the next week or 
            // of the next month            
            
            Runnable nextNotification;
            LocalDateTime nextNotificationTime;
            if ( nextWeekBeginning.isBefore(nextMonthBeginning) ) {
                nextNotificationTime = nextWeekBeginning;
                nextNotification = new Runnable() {
                            @Override
                            public void run() {
                                notifyUserAboutThisWeekTasks(nextWeekBeginning);
                            }
                        };
            } else {
                nextNotificationTime = nextMonthBeginning;
                nextNotification = new Runnable() {
                            @Override
                            public void run() {
                                notifyUserAboutThisMonthTasks(nextMonthBeginning);
                            }
                        };
            }
            this.currentNotification = this.scheduler.schedule( 
                    nextNotification,
                    this.getMillisFromNowToTime(nextNotificationTime), 
                    TimeUnit.MILLISECONDS);
        }
    }
    
    private void notifyUserAboutThisMonthTasks(LocalDateTime monthBeginning) {
        synchronized (this.notificationLock) {
            List<TaskMessage> tasks = this.tasksDao.getCalendarTasksBetweenDates(
                    monthBeginning, monthBeginning.plusMonths(1));            
            this.ioEngine.showTasksNotification("month", tasks);
            this.scheduleNextRegularTasksSurvey();
        }
    }
    
    private void notifyUserAboutThisWeekTasks(LocalDateTime weekBeginning) {
        synchronized (this.notificationLock) {
            List<TaskMessage> tasks = this.tasksDao.getCalendarTasksBetweenDates(
                    weekBeginning, weekBeginning.plusWeeks(1));  
            this.ioEngine.showTasksNotification("week", tasks);
            this.scheduleNextRegularTasksSurvey();
        }
    }
    
    private void performFirstTasks() {
        synchronized (this.taskExecutionLock) {
            this.processObtainedTasksAndUpdateTimer(
                    this.tasksDao.getFirstTasks());
        }
    }
    
    private void performFirstTasksExceptHourlyTasks() {
        synchronized (this.taskExecutionLock) {
            this.processObtainedTasksWithoutHourlyAndUpdateTimer(
                    this.tasksDao.getFirstTasks());
        }
    }
    
    private void performFirstTasksExceptHourlyAndDailyTasks() {
        synchronized (this.taskExecutionLock) {
            this.processObtainedTasksWithoutHourlyDailyAndUpdateTimer(
                    this.tasksDao.getFirstTasks());
        }
    }
    
    private void processObtainedTasksWithoutHourlyDailyAndUpdateTimer(
            List<Task> tasks) {
        
        System.out.println("[TASKS] process tasks without hourly, daily...");
        for (int i = 0; i < tasks.size(); i++) {
            if ( HOURLY.equals(tasks.get(i).getType()) 
                    || DAILY.equals(tasks.get(i).getType())) {
                
                // do not show neither hourly nor daily tasks, just
                // update them to set actual time of future execution.
                tasks.get(i).modifyAccordingToType();
            } else {
                this.ioEngine.showTask(tasks.get(i).generateMessage());
                tasks.get(i).modifyAccordingToType();
            }
        }
        this.updateTimer(this.tasksDao.updateTasksAndGetNextFirstTime(tasks));
    }
    
    private void processObtainedTasksWithoutHourlyAndUpdateTimer(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            if ( HOURLY.equals(tasks.get(i).getType())) {
                // do not show hourly tasks, only update 
                // them to set actual time of future execution.
                tasks.get(i).modifyAccordingToType();
            } else {                
                this.ioEngine.showTask(tasks.get(i).generateMessage());
                tasks.get(i).modifyAccordingToType();
            }
        }
        this.updateTimer(this.tasksDao.updateTasksAndGetNextFirstTime(tasks));
    }  
    
    private void processObtainedTasksAndUpdateTimer(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            this.ioEngine.showTask(tasks.get(i).generateMessage());
            tasks.get(i).modifyAccordingToType();
        }
        this.updateTimer(this.tasksDao.updateTasksAndGetNextFirstTime(tasks));
    }
    
    private boolean updateTimer(LocalDateTime newTime) {
        synchronized (this.taskExecutionLock) {            
            if ( this.currentExecution != null && !this.currentExecution.isDone() ) {
                this.currentExecution.cancel(false);
                // task is executing now, so there is no need
                // to keep reference on its ScheduledFuture 
                // to cancel it.
                this.currentExecution = null;
            }                
            // if there was no error and all previous operations
            // have been perfrormed properly...
            if ( newTime != null ) {
                // if there is new LocalDateTime to update scheduler, update it;
                // if time == LocalDateTime.MIN it means that there are no
                // actual tasks and there is no need to update scheduler, but
                // all operations have been performed properly.
                if ( ! newTime.equals(LocalDateTime.MIN) ) {
                    this.currentExecution =
                    this.scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {                                
                                // get the lag between time that was scheduled
                                // and actual execution time. 
                                // If system was inactive or program was 
                                // shutdown this lag may be large.
                                long inactivePeriod = 
                                        Duration.between(
                                                newTime, 
                                                LocalDateTime.now())
                                                .toMinutes();
                                if ( inactivePeriod <= 30 ) {
                                    performFirstTasks();
                                } else if ( (30 < inactivePeriod) && (inactivePeriod <= 60*5) ) {
                                    // If lag is longer than 30 minutes but no
                                    // longer than 5 hours, do not show hourly 
                                    // tasks that was expired while program
                                    // or system was inactive, just update them.
                                    performFirstTasksExceptHourlyTasks();
                                } else {
                                    // if lag is longer than 5 hours, do not 
                                    // show both hourly and daily tasks that 
                                    // was expired while program or system was
                                    // inactive, just update them.
                                    performFirstTasksExceptHourlyAndDailyTasks();
                                }
                            }
                        }, 
                        this.getMillisFromNowToTime(newTime), 
                        TimeUnit.MILLISECONDS);
                } else {
                    this.currentExecution = null;
                }
                // all operations have been performed properly so return TRUE
                return true;
            } else {
                // NULL was returned that means that some errors have been
                // occured during SQL or task processing
                return false;
            }
        }
    }       
    
    private long getMillisFromNowToTime(LocalDateTime futureTime) {        
        return Duration.between(LocalDateTime.now(), futureTime).toMillis();
    }
    
    @Override
    public boolean createNewTask(TaskType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours) {
        
        try {
            LocalDateTime taskTime = this.formatter.ofFormat(time, true);            
            LocalDateTime newTime = this.tasksDao
                    .addTask(Task.newTask(type, taskTime, task, days, hours));
            
            if (newTime == null) {
                this.ioEngine.reportError(
                        "Task was not saved.",
                        "Something has gone wrong :(");
                return false;
            }
            return this.updateTimer(newTime);
        } catch (TaskTypeInvalidException e) {
            this.ioEngine.reportMessage("Invalid task type: " + e.getMessage());
        } catch (TaskTimeFormatInvalidException e) {
            this.ioEngine.reportMessage("Time verifying: Unrecognizable time format.");
        } catch (TaskTimeInvalidException e) {
            this.ioEngine.reportMessage("Time verifying: Given time is past. It must be future!");
        } catch (NumberFormatException e) {
            this.ioEngine.reportMessage("Time verifying: Wrong characters have been inputted!");
        } catch (DateTimeParseException e) {
            this.ioEngine.reportMessage("Time verifying: Wrong time format.");
        } catch (DateTimeException e) {
            this.ioEngine.reportMessage("Time verifying: Invalid dates out of range.");
        } 
        return false;
    }
    
    @Override
    public String getFirstAlarmTime() {
        LocalDateTime first = this.tasksDao.getFirstTaskTime();
        if (first != null) {
            if ( LocalDateTime.MIN.equals(first) ) {
                return "there aren't tasks now.";            
            } else {
                return this.formatter.outputTimePatternFormat(first);
            }
        } else {
            return "";
        }        
    }
    
    @Override
    public List<TaskMessage> getFutureTasks() {
        return this.tasksDao.getActualTasks();
    }
    
    @Override
    public List<TaskMessage> getPastTasks() {        
        return this.tasksDao.getNonActualTasks();  
    }
    
    @Override
    public List<TaskMessage> getActualReminders() {
        return this.tasksDao.getActualReminders();
    }
    
    @Override
    public List<TaskMessage> getActualEvents() {
        return this.tasksDao.getActualEvents();
    }
    
    @Override
    public List<TaskMessage> getFirstTask() {
        return this.tasksDao.getFirstTasks()
                .stream()
                .map(Task::generateMessage)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean deleteTaskByText(String text) {   
        return this.updateTimer(this.tasksDao.deleteTaskByText(text));                    
    }
    
    @Override
    public boolean removeAllTasks() {
        return this.updateTimer(this.tasksDao.deleteAllTasks());
    }
    
    @Override
    public boolean removeAllFutureTasks() {
        return this.updateTimer(this.tasksDao.deleteActualTasks());
    }
    
    @Override
    public boolean removeAllPastTasks() {
        return this.updateTimer(this.tasksDao.deleteNonActualTasks());
    }
    
    /*
    @Override
    public boolean suspendTask(String text) {
        List<Task> suspendableTasks = this.tasksDao.getSuspendableTasks();
        if ( suspendableTasks.size() > 1 ) {
            List<String> taskStrings = new ArrayList<>();
            for (Task task : suspendableTasks) {
                taskStrings.add(task.getContent()[0]);
            }
            int choice = this.ioEngine.resolveVariantsWithExternalIO(
                    "Which task to suspend?", taskStrings);
        }
        this.updateTimer(this.tasksDao.suspendTask());
    }
    
    @Override
    public boolean activateSuspendedTask(String text) {
        
    }
    */
}
