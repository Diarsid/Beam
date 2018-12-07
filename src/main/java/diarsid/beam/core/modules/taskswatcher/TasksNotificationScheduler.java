/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.taskswatcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.info;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getMillisFromNowToTime;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getNextMonthBeginning;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getNextWeekBeginning;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getThisWeekBeginning;

import diarsid.beam.core.modules.io.gui.TasksGui;

/**
 *
 * @author Diarsid
 */
class TasksNotificationScheduler {
    
    private final TasksGui tasksGui;
    private final TasksKeeper tasksKeeper;
    private final Initiator ownInitiator;
    private final ScheduledThreadPoolExecutor scheduler;
    private final Object notificationLock;
    
    // contains  the reference to Runnable scheduled in
    // this.scheduler for now and represents time 
    // of user notification about weekly and monthly tasks;
    // Is refreshed every time when notification are 
    // being executed.
    private ScheduledFuture currentNotification;

    public TasksNotificationScheduler(
            TasksGui tasksGui, 
            TasksKeeper tasksKeeper, 
            ScheduledThreadPoolExecutor scheduler,
            Initiator ownInitiator) {
        this.tasksGui = tasksGui;
        this.tasksKeeper = tasksKeeper;
        this.scheduler = scheduler;
        this.notificationLock = new Object();
        this.ownInitiator = ownInitiator;
    }

    void beginNotificationsProcessing() {
        synchronized ( this.notificationLock ) {
            this.scheduleNextRegularTasksSurvey();
            LocalDateTime thisWeekBeginning = getThisWeekBeginning();
//            this.notifyUserAboutTasksInWeek(thisWeekBeginning);
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
        synchronized ( this.notificationLock ) {
            this.purgeCurrentNotificationIfAny();
            
            // get time of next Monday, 12:00:00:000
            LocalDateTime nextWeekBeginning = getNextWeekBeginning();
            
            // get time of next month's first day, 12:00:00:000
            LocalDateTime nextMonthBeginning = getNextMonthBeginning();
            // what happens earlier - begining of the next week or 
            // of the next month            
            
            Runnable scheduledNotificationAction;
            LocalDateTime nextNotificationTime;
            if ( nextWeekBeginning.isBefore(nextMonthBeginning) ) {
                nextNotificationTime = nextWeekBeginning;
                scheduledNotificationAction = () -> {
                    this.notifyUserAboutTasksInWeek(nextWeekBeginning);
                };
            } else {
                nextNotificationTime = nextMonthBeginning;
                scheduledNotificationAction = () -> {
                    this.notifyUserAboutTasksInMonth(nextMonthBeginning);
                };
            }
            this.currentNotification = this.scheduler.schedule( 
                    scheduledNotificationAction,
                    getMillisFromNowToTime(nextNotificationTime), 
                    MILLISECONDS);
        }
    }

    private void purgeCurrentNotificationIfAny() {
        // if there is older notification have been scheduled
        // clear them.
        if ( this.isCurrentNotificationAlive() ) {
            this.currentNotification.cancel(false);
            this.currentNotification = null;
        }
    }

    private boolean isCurrentNotificationAlive() {
        return 
                this.currentNotification != null && 
                ! this.currentNotification.isDone();
    }
    
    private void notifyUserAboutTasksInMonth(LocalDateTime monthBeginning) {
        synchronized (this.notificationLock) {
            List<Task> tasks = this.tasksKeeper
                    .getCalendarTasksForNextMonth(this.ownInitiator, monthBeginning);
            
            Runnable notification;
            if ( tasks.isEmpty() ) {
                notification = () -> {
                    this.tasksGui.show(info("There are no any tasks scheduled in this month."));
                };
            } else {
                notification = () -> {
                    this.tasksGui.showAllJointly("Tasks scheduled in this month", tasks);
                };
            }
            
            asyncDo(notification);
            this.scheduleNextRegularTasksSurvey();
        }
    }
    
    private void notifyUserAboutTasksInWeek(LocalDateTime weekBeginning) {
        synchronized (this.notificationLock) {
            List<Task> tasks = this.tasksKeeper
                    .getCalendarTasksForNextWeek(this.ownInitiator, weekBeginning);
            
            Runnable notification;
            if ( tasks.isEmpty() ) {
                notification = () -> {
                    this.tasksGui.show(info("There are no any tasks scheduled in this week."));
                };
            } else {
                notification = () -> {
                    this.tasksGui.showAllJointly("Tasks scheduled in this week", tasks);
                };
            }
            
            asyncDo(notification);
            this.scheduleNextRegularTasksSurvey();
        }
    }
}
