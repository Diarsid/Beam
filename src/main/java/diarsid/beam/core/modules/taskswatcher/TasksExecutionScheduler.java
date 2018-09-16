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

import diarsid.beam.core.application.gui.OutputTasksGui;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.support.objects.Possible;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.modules.domainkeeper.TasksKeeper;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.ConcurrencyUtil.asyncDo;
import static diarsid.support.log.Logging.logFor;
import static diarsid.beam.core.domain.entities.TaskRepeat.DAILY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.HOURLY_REPEAT;
import static diarsid.beam.core.modules.taskswatcher.LagType.LAG_AFTER_INITIAL_START;
import static diarsid.beam.core.modules.taskswatcher.LagType.LAG_AFTER_TEMPORARY_PAUSE;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getMillisFromNowToTime;
import static diarsid.beam.core.modules.taskswatcher.TimeUtil.getMinutesFromPastToNow;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
class TasksExecutionScheduler {
    
    private final OutputTasksGui tasksGui;
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
    private final Possible<ScheduledFuture> currentExecution;

    TasksExecutionScheduler(
            OutputTasksGui tasksGui, 
            TasksKeeper tasksKeeper, 
            ScheduledThreadPoolExecutor scheduler,
            Initiator ownInitiator) {
        this.tasksGui = tasksGui;
        this.tasksKeeper = tasksKeeper;
        this.scheduler = scheduler;
        this.taskExecutionLock = new Object();
        this.ownInitiator = ownInitiator;
        this.currentExecution = possibleButEmpty();
    }
    
    void refresh() {
        synchronized ( this.taskExecutionLock ) {             
            this.updateCurrentExecution();
        }
    }
    
    void beginTasksProcessing() {
        synchronized ( this.taskExecutionLock ) {
            // if possible lag is not present it means that there are no active
            // tasks to measure lag between their expired but active execution time
            // and present moment
            Optional<Long> possibleLag = this.tasksKeeper
                    .getInactivePeriodMinutes(this.ownInitiator);
            if ( possibleLag.isPresent() ) {
                // get lag, expired tasks, and show them
                List<Task> expiredTasks = this.tasksKeeper.getPastActiveTasks(this.ownInitiator); 
                this.showExpiredTasks(possibleLag.get(), expiredTasks);
                if ( nonEmpty(expiredTasks) ) {
                    // switch all tasks
                    expiredTasks.stream().forEach(Task::switchTime);
                    // update expired tasks     
                    this.tasksKeeper.updateTasks(this.ownInitiator, expiredTasks);
                }  
            }        
        }
    }

    private void showExpiredTasks(long lag, List<Task> expiredTasks) {        
        List<Task> tasksToShow = this.filterAccordingToLagAndConvertToMessages(
                    expiredTasks, lag, LAG_AFTER_INITIAL_START);
        asyncDo(() -> {
            this.tasksGui.showAllSeparately(tasksToShow);
        });
    }
    
    private void updateCurrentExecution() {                   
        this.purgeCurrentExecutionIfAny();
        Optional<LocalDateTime> probableNewTimeToSchedule = 
                this.tasksKeeper.getTimeOfFirstTask(this.ownInitiator);
        // if there is new LocalDateTime to update scheduler, update it;
        // if there isn't it means that there are no
        // actual tasks and there is no need to update scheduler, but
        // all operations have been performed properly.
        if ( probableNewTimeToSchedule.isPresent() ) {
            LocalDateTime scheduledTime = probableNewTimeToSchedule.get();
            ScheduledFuture execution = this.scheduler.schedule(
                    // runnable to execute when scheduled time comes
                    () -> {
                        synchronized ( this.taskExecutionLock ) {
                            logFor(this).info("...first tasks delayed execution.");
                            // get all first tasks, compute the lag (if any) and 
                            // create appropriate displayable messages and show them
                            List<Task> tasks = this.tasksKeeper
                                    .getPastActiveTasks(this.ownInitiator); // getPastActiveTasks() to avoid possible multiple executions?
                            this.showFirstTasks(tasks, scheduledTime);

                            // switch all tasks
                            tasks.stream().forEach(Task::switchTime);               

                            // udpate tasks in storage                    
                            this.tasksKeeper.updateTasks(this.ownInitiator, tasks);
                        }
                    },
                    // calculate runnable's delay
                    getMillisFromNowToTime(scheduledTime),
                    MILLISECONDS);
            this.currentExecution.resetTo(execution);
        } else {
            this.currentExecution.nullify();
        }
    }

    private void purgeCurrentExecutionIfAny() {
        if ( this.isCurrentExecutionAlive() ) {
            this.currentExecution.orThrow().cancel(false);
            this.currentExecution.nullify();
        }
    }

    private boolean isCurrentExecutionAlive() {
        return 
                this.currentExecution.isPresent() && 
                ! this.currentExecution.orThrow().isDone();
    }

    private void showFirstTasks(List<Task> tasks, LocalDateTime scheduledTime) {
        long lag = getMinutesFromPastToNow(scheduledTime);
        List<Task> tasksToShow = this.filterAccordingToLagAndConvertToMessages(
                tasks, lag, LAG_AFTER_TEMPORARY_PAUSE);
        asyncDo(() -> {
            this.tasksGui.showAllSeparately(tasksToShow);
        });
    }
    
    private List<Task> filterAccordingToLagAndConvertToMessages(
            List<Task> tasks, long minutes, LagType lagType) {
        if ( lagType.isShort(minutes) ) {
            return tasks;
        } else if ( lagType.isNeitherShortNorLong(minutes) ) {
            return tasks
                    .stream()
                    .filter(task -> task.type().isNot(HOURLY_REPEAT))
                    .collect(toList());
        } else {
            return tasks
                    .stream()
                    .filter(task -> task.type().isNot(HOURLY_REPEAT, DAILY_REPEAT))
                    .collect(toList());
        }
    }
}
