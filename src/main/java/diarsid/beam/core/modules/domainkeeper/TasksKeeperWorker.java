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

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.TimeMessage;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeatType;
import diarsid.beam.core.domain.entities.Tasks;
import diarsid.beam.core.domain.inputparsing.time.TasksTime;
import diarsid.beam.core.domain.inputparsing.time.TasksTimeAndText;
import diarsid.beam.core.domain.inputparsing.time.TimeAndTextParser;
import diarsid.beam.core.domain.inputparsing.time.TimePatternParsersHolder;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Operations.operationFailedWith;
import static diarsid.beam.core.base.control.flow.Operations.operationStopped;
import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.domain.entities.TaskRepeatType.MONTHLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeatType.NO_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeatType.YEARLY_REPEAT;




public class TasksKeeperWorker implements TasksKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoTasks dao;
    private final KeeperDialogHelper helper;
    private final TimeAndTextParser timeAndTextParser;
    private final TimePatternParsersHolder timeParser;

    public TasksKeeperWorker(
            InnerIoEngine ioEngine, 
            DaoTasks dao, 
            KeeperDialogHelper helper, 
            TimeAndTextParser timeAndTextParser,
            TimePatternParsersHolder timeParser) {
        this.ioEngine = ioEngine;
        this.dao = dao;
        this.helper = helper;
        this.timeAndTextParser = timeAndTextParser;
        this.timeParser = timeParser;
    }

    @Override
    public List<Task> getExpiredTasks(
            Initiator initiator) {
        return this.dao.getExpiredTasks(initiator, LocalDateTime.now());
    }

    @Override
    public Optional<Long> getInactivePeriodMinutes(
            Initiator initiator) {
        return this.dao.getTimeOfFirstActiveTask(initiator)
                .map(time -> Duration.between(time, now()).toMinutes());
    }

    @Override
    public List<Task> getFirstTasks(
            Initiator initiator) {
        return this.dao.getFirstActiveTasks(initiator);
    }

    @Override
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
        boolean updated = this.dao.updateTasks(initiator, tasks);
        fireAsync("tasks_updated");
        return updated;
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstTask(
            Initiator initiator) {
        return this.dao.getTimeOfFirstActiveTask(initiator);
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
    public OperationFlow createTask(
            Initiator initiator, MultiStringCommand command) {
        OperationFlow flow = operationFailedWith("initial");
        
        TasksTimeAndText timeAndText = this.timeAndTextParser.parse(command.arguments());        
        Optional<TasksTime> optTime = timeAndText.getTime();
        String text = timeAndText.getText();
        
        TasksTime taskTime = null;
        if ( optTime.isPresent() ) {
            taskTime = optTime.get();
        } else {
            String timePattern;
            Optional<TasksTime> parsedTime;
            while ( isNull(taskTime) ) {
                timePattern = this.ioEngine.askInput(initiator, "time");
                if ( timePattern.isEmpty() ) {
                    return operationStopped();
                }
                parsedTime = this.timeParser.parse(timePattern);
                if ( parsedTime.isPresent() ) {
                    taskTime = parsedTime.get();
                } else {
                    this.ioEngine.report(initiator, "unknown format.");
                }
            }
        }
        
        if ( isNull(taskTime) ) {
            return operationFailedWith("unexpected null.");
        }
        LocalDateTime time = taskTime.actualizedTime();
        if ( time.isBefore(now()) ) {
            return operationFailedWith("unexpected past time.");
        }        
        TaskRepeatType taskType = taskTime.defineTasksType();
        
        if ( text.isEmpty() ) {
            
        }
        
        Task task = Tasks.newTask(type, time, days, hours, content);
        this.dao.saveTask(initiator, task);
        
        fireAsync("tasks_updated");
        return flow;
    }

    @Override
    public OperationFlow deleteTask(
            Initiator initiator, SingleStringCommand command) {
        boolean removed = false;
        
        fireAsync("tasks_updated");
        return removed;
    }

    @Override
    public OperationFlow editTask(
            Initiator initiator, SingleStringCommand command) {
        fireAsync("tasks_updated");
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Task> findTasks(
            Initiator initiator, SingleStringCommand findEntityCommand) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public boolean createNewTask(TaskRepeatType type, String time, String[] task, 
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
