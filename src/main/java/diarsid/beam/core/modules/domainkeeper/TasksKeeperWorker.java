/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.base.TimeMessage;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.RemoveEntityCommand;
import diarsid.beam.core.control.io.commands.creation.CreateTaskCommand;
import diarsid.beam.core.domain.actions.Callback;
import diarsid.beam.core.domain.entities.SchedulableType;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.exceptions.TaskTimeFormatInvalidException;
import diarsid.beam.core.domain.entities.exceptions.TaskTimeInvalidException;
import diarsid.beam.core.domain.entities.exceptions.TaskTypeInvalidException;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.time.LocalDateTime.now;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.domain.entities.SchedulableType.MONTHLY_REPEAT;
import static diarsid.beam.core.domain.entities.SchedulableType.NO_REPEAT;
import static diarsid.beam.core.domain.entities.SchedulableType.YEARLY_REPEAT;


public class TasksKeeperWorker implements TasksKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoTasks dao;
    private Optional<Callback> tasksUpdatedCallback;

    public TasksKeeperWorker(InnerIoEngine ioEngine, DaoTasks dao) {
        this.ioEngine = ioEngine;
        this.dao = dao;
        this.tasksUpdatedCallback = Optional.empty();
    }

    @Override
    public List<Task> getExpiredTasks(Initiator initiator) {
        return this.dao.getExpiredTasks(initiator, LocalDateTime.now());
    }

    @Override
    public Optional<Long> getInactivePeriodMinutes(Initiator initiator) {
        return this.dao.getTimeOfFirstActiveTask(initiator)
                .map(time -> Duration.between(time, now()).toMinutes());
    }

    @Override
    public List<Task> getFirstTasks(Initiator initiator) {
        return this.dao.getFirstActiveTasks(initiator);
    }

    @Override
    public Optional<LocalDateTime> updateTasksAndGetNextFirstTime(
            Initiator initiator, List<Task> tasks) {
        return this.dao.updateTasks(initiator, tasks);
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstTask(Initiator initiator) {
        return this.dao.getTimeOfFirstActiveTask(initiator);
    }

    @Override
    public void registerTasksUpdatingCallback(Callback callback) {
        if ( ! this.tasksUpdatedCallback.isPresent() ) {
            this.tasksUpdatedCallback = Optional.of(callback);
        }
    }

    @Override
    public List<TimeMessage> getCalendarTasksForNextMonth(
            Initiator initiator, LocalDateTime nextMonthBeginning) {
        return this.dao.getActiveTasksOfTypeBetweenDates(
                initiator,
                nextMonthBeginning, 
                nextMonthBeginning.plusMonths(1), 
                NO_REPEAT, MONTHLY_REPEAT, YEARLY_REPEAT)
                        .stream()
                        .map(Task::toTimeMessage)
                        .collect(toList());
    }

    @Override
    public List<TimeMessage> getCalendarTasksForNextWeek(
            Initiator initiator, LocalDateTime nextWeekBeginning) {
        return this.dao.getActiveTasksOfTypeBetweenDates(
                initiator,
                nextWeekBeginning, 
                nextWeekBeginning.plusWeeks(1), 
                NO_REPEAT, MONTHLY_REPEAT, YEARLY_REPEAT)
                        .stream()
                        .map(Task::toTimeMessage)
                        .collect(toList());
    }

    @Override
    public boolean createTask(Initiator initiator, CreateTaskCommand command) {
        boolean created = false;
        this.tasksUpdatedCallback.ifPresent(Callback::call);
        return created;
    }

    @Override
    public boolean createReminder(Initiator initiator, CreateEntityCommand command) {
        boolean created = false;
        this.tasksUpdatedCallback.ifPresent(Callback::call);
        return created;
    }

    @Override
    public boolean createEvent(Initiator initiator, CreateEntityCommand command) {
        boolean created = false;
        this.tasksUpdatedCallback.ifPresent(Callback::call);
        return created;
    }

    @Override
    public boolean deleteTask(Initiator initiator, RemoveEntityCommand command) {
        boolean removed = false;
        this.tasksUpdatedCallback.ifPresent(Callback::call);
        return removed;
    }
    
    public boolean createNewTask(SchedulableType type, String time, String[] task, 
            Set<Integer> days, Set<Integer> hours) {
        
        try {
            LocalDateTime taskTime = this.formatter.ofFormat(time, true);            
            LocalDateTime newTime = this.tasksDao
                    .addTask(Task.newTask(type, taskTime, task, days, hours));
            
            if (newTime == null) {
                this.tasksIo.reportError(
                        "Task was not saved.",
                        "Something has gone wrong :(");
                return false;
            }
            return this.updateTimer(newTime);
        } catch (TaskTypeInvalidException e) {
            this.tasksIo.reportMessage("Invalid task type: " + e.getMessage());
        } catch (TaskTimeFormatInvalidException e) {
            this.tasksIo.reportMessage("Time verifying: Unrecognizable time format.");
        } catch (TaskTimeInvalidException e) {
            this.tasksIo.reportMessage("Time verifying: Given time is past. It must be future!");
        } catch (NumberFormatException e) {
            this.tasksIo.reportMessage("Time verifying: Wrong characters have been inputted!");
        } catch (DateTimeParseException e) {
            this.tasksIo.reportMessage("Time verifying: Wrong time format.");
        } catch (DateTimeException e) {
            this.tasksIo.reportMessage("Time verifying: Invalid dates out of range.");
        } 
        return false;
    }
    
    
}
