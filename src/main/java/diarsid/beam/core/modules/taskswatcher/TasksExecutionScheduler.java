/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.taskswatcher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.TimeMessage;
import diarsid.beam.core.control.io.base.TimeMessagesIo;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.domain.entities.SchedulableType.DAILY_REPEAT;
import static diarsid.beam.core.domain.entities.SchedulableType.HOURLY_REPEAT;
import static diarsid.beam.core.modules.taskswatcher.LagType.LAG_AFTER_INITIAL_START;
import static diarsid.beam.core.modules.taskswatcher.LagType.LAG_AFTER_TEMPORARY_PAUSE;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getMillisFromNowToTime;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getMinutesFromPastToNow;
import static diarsid.beam.core.util.ConcurrencyUtil.asyncDo;
import static diarsid.beam.core.util.Logs.debug;

/**
 *
 * @author Diarsid
 */
class TasksExecutionScheduler {
    
    private final TimeMessagesIo tasksIo;
    private final TasksKeeper tasksKeeper;
    private final Initiator ownInitiator;
    private final ScheduledThreadPoolExecutor scheduler;        
    private final Object taskExecutionLock;
    
    // contains the reference to Runnable scheduled in
    // this.scheduler for now and represents execution 
    // the earliest task.
    // Is refreshed every time when task or tasks are 
    // being executed.
    // Can be null, if there are no any tasks.
    private ScheduledFuture currentExecution;

    TasksExecutionScheduler(
            TimeMessagesIo tasksIo, 
            TasksKeeper tasksKeeper, 
            ScheduledThreadPoolExecutor scheduler,
            Initiator ownInitiator) {
        this.tasksIo = tasksIo;
        this.tasksKeeper = tasksKeeper;
        this.scheduler = scheduler;
        this.taskExecutionLock = new Object();
        this.ownInitiator = ownInitiator;
    }
    
    void refresh() {
        synchronized ( this.taskExecutionLock ) { 
            this.updateCurrentExecution(this.tasksKeeper.getTimeOfFirstTask(this.ownInitiator));
        }
    }
    
    void beginTasksProcessing() {
        synchronized ( this.taskExecutionLock ) {
            // get lag, expired tasks, and show them
            Optional<Long> possibleLag = this.tasksKeeper.getInactivePeriodMinutes(this.ownInitiator);
            List<Task> expiredTasks = this.tasksKeeper.getExpiredTasks(this.ownInitiator); 
            this.showExpiredTasks(possibleLag, expiredTasks);

            // switch all tasks
            expiredTasks.stream().forEach(Task::switchTime);

            // update expired tasks and schedule new execution.        
            Optional<LocalDateTime> newExecutionTime = this.tasksKeeper
                    .updateTasksAndGetNextFirstTime(this.ownInitiator, expiredTasks);
            this.updateCurrentExecution(newExecutionTime);
        }
    }

    private void showExpiredTasks(Optional<Long> possibleLag, List<Task> expiredTasks) {
        // if possible lag is not present it means that there are no active
        // tasks to measure lag between their expired but active execution time
        // and present moment
        if ( possibleLag.isPresent() ) {
            List<TimeMessage> tasksToShow = this.filterAccordingToLagAndConvertToMessages(
                    expiredTasks, possibleLag.get(), LAG_AFTER_INITIAL_START);
            asyncDo(() -> {
                this.tasksIo.showAll(tasksToShow);
            });
        }
    }
    
    private void updateCurrentExecution(Optional<LocalDateTime> possibleNewTimeToSchedule) {                   
        this.purgeCurrentExecutionIfAny();   
        // if there is new LocalDateTime to update scheduler, update it;
        // if there isn't it means that there are no
        // actual tasks and there is no need to update scheduler, but
        // all operations have been performed properly.
        if ( possibleNewTimeToSchedule.isPresent() ) {
            LocalDateTime scheduledTime = possibleNewTimeToSchedule.get();
            this.currentExecution = this.scheduler.schedule(
                    // runnable to execute when scheduled time comes
                    () -> {
                        synchronized ( this.taskExecutionLock ) {
                            debug("...first tasks delayed execution.");
                            // get all first tasks, compute the lag (if any) and 
                            // create appropriate displayable messages and show them
                            List<Task> tasks = this.tasksKeeper.getFirstTasks(this.ownInitiator); // getExpiredTasks() to avoid possible multiple executions?
                            this.showFirstTasks(tasks, scheduledTime);

                            // switch all tasks
                            tasks.stream().forEach(Task::switchTime);               

                            // udpate tasks in storage, obtain new execution 
                            // time, compute and set new execution moment                        
                            Optional<LocalDateTime> newExecutionTime = this.tasksKeeper
                                    .updateTasksAndGetNextFirstTime(this.ownInitiator, tasks);
                            this.updateCurrentExecution(newExecutionTime);
                        }
                    },
                    // calculate runnable's delay
                    getMillisFromNowToTime(scheduledTime),
                    MILLISECONDS);
        } else {
            this.currentExecution = null;
        }
    }

    private void purgeCurrentExecutionIfAny() {
        if ( this.isCurrentExecutionAlive() ) {
            this.currentExecution.cancel(false);
            this.currentExecution = null;
        }
    }

    private boolean isCurrentExecutionAlive() {
        return 
                this.currentExecution != null && 
                ! this.currentExecution.isDone();
    }

    private void showFirstTasks(List<Task> tasks, LocalDateTime scheduledTime) {
        long lag = getMinutesFromPastToNow(scheduledTime);
        List<TimeMessage> tasksToShow = this.filterAccordingToLagAndConvertToMessages(
                tasks, lag, LAG_AFTER_TEMPORARY_PAUSE);
        asyncDo(() -> {
            this.tasksIo.showAll(tasksToShow);
        });
    }
    
    private List<TimeMessage> filterAccordingToLagAndConvertToMessages(
            List<Task> tasks, long minutes, LagType lagType) {
        if ( lagType.isShort(minutes) ) {
            return tasks
                    .stream()
                    .map(task -> task.toTimeMessage())
                    .collect(toList());
        } else if ( lagType.isNeitherShortNorLong(minutes) ) {
            return tasks
                    .stream()
                    .filter(task -> task.type().isNot(HOURLY_REPEAT))
                    .map(task -> task.toTimeMessage())
                    .collect(toList());
        } else {
            return tasks
                    .stream()
                    .filter(task -> task.type().isNot(HOURLY_REPEAT, DAILY_REPEAT))
                    .map(task -> task.toTimeMessage())
                    .collect(toList());
        }
    }
}
