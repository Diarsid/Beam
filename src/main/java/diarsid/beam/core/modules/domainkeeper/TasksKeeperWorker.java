/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.domainkeeper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.flow.OperationFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Question;
import diarsid.beam.core.base.control.io.base.interaction.TimeMessage;
import diarsid.beam.core.base.control.io.commands.MultiStringCommand;
import diarsid.beam.core.base.control.io.commands.SingleStringCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.TaskTime;
import diarsid.beam.core.domain.inputparsing.time.TasksTimeAndText;
import diarsid.beam.core.domain.inputparsing.time.TimeAndTextParser;
import diarsid.beam.core.domain.inputparsing.time.TimePatternParsersHolder;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.flow.Operations.operationFailedWith;
import static diarsid.beam.core.base.control.flow.Operations.operationStopped;
import static diarsid.beam.core.base.control.flow.Operations.success;
import static diarsid.beam.core.base.control.io.base.interaction.Question.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.TaskRepeat.DAILY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.HOURLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.MONTHLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.NO_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.YEARLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.repeatByItsName;
import static diarsid.beam.core.domain.entities.TaskRepeat.repeatNames;
import static diarsid.beam.core.domain.entities.Tasks.newEventTask;
import static diarsid.beam.core.domain.entities.Tasks.newInstantTask;
import static diarsid.beam.core.domain.entities.Tasks.newReminderTask;


public class TasksKeeperWorker implements TasksKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoTasks dao;
    private final KeeperDialogHelper helper;
    private final TimeAndTextParser timeAndTextParser;
    private final TimePatternParsersHolder timeParser;
    private final AllowedTimePeriodsParser timePeriodsParser;

    public TasksKeeperWorker(
            InnerIoEngine ioEngine, 
            DaoTasks dao, 
            KeeperDialogHelper helper, 
            TimeAndTextParser timeAndTextParser,
            TimePatternParsersHolder timeParser,
            AllowedTimePeriodsParser timePeriodsParser) {
        this.ioEngine = ioEngine;
        this.dao = dao;
        this.helper = helper;
        this.timeAndTextParser = timeAndTextParser;
        this.timeParser = timeParser;
        this.timePeriodsParser = timePeriodsParser;
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
        if ( command.type().isNot(CREATE_TASK) ) {
            return operationFailedWith("wrong command type!");
        }
        TasksTimeAndText timeAndText = this.timeAndTextParser.parse(command.arguments());        
        Optional<TaskTime> optTime = timeAndText.getTime();
        String initialText = timeAndText.getText();
        
        TaskTime taskTime;
        if ( optTime.isPresent() ) {
            taskTime = optTime.get();
        } else {
            String timePattern;
            Optional<TaskTime> parsedTime;
            taskTime = null;
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
        
        TaskRepeat repeat;
        if ( taskTime.isTimeRelative() ) {
            repeat = NO_REPEAT;
        } else {
            Question question = question("choose repeat").withAnswerStrings(repeatNames());
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                repeat = repeatByItsName(answer.getText());
            } else {
                return operationStopped();
            }
        }        
        if ( repeat.isUndefined() ) {
            return operationFailedWith("unexpected undefined task repeat.");
        }
        
        List<String> text = new ArrayList<>();
        if ( nonEmpty(initialText) ) {
            text.add(initialText);
        } else {
            String line;
            boolean input = true;
            while ( input ) {                
                line = this.ioEngine.askInput(initiator, "text");
                if ( nonEmpty(line) ) {
                    text.add(line);
                } else {
                    input = false;
                }
            }
        }
        if ( text.isEmpty() ) {
            return operationStopped();
        }
        
        Task task;
        if ( repeat.equals(NO_REPEAT) ) {
            task = newInstantTask(repeat, time, text);
        } else if ( repeat.isOneOf(HOURLY_REPEAT, DAILY_REPEAT) ) {
            Optional<AllowedTimePeriod> periods = this.askForAllowedTimePeriod(initiator);
            if ( periods.isPresent() ) {
                task = newReminderTask(
                        repeat, time, periods.get().days(), periods.get().hours(), text);
            } else {
                return operationStopped();
            }            
        } else if ( repeat.isOneOf(MONTHLY_REPEAT, YEARLY_REPEAT) ) {
            task = newEventTask(repeat, time, text);
        } else {
            return operationFailedWith("unexpected TaskRepeat value.");
        }
        if ( isNull(task) ) {
            return operationFailedWith("unexpected NULL task");
        }
        
        if ( this.dao.saveTask(initiator, task) ) {
            fireAsync("tasks_updated");
            return success();
        } else {
            return operationFailedWith("DAO failed to save task.");
        }
    }
    
    private Optional<AllowedTimePeriod> askForAllowedTimePeriod(Initiator initiator) {
        String daysTimePattern = this.ioEngine.askInput(initiator, "allowed days");
        if ( daysTimePattern.isEmpty() ) {
            return Optional.empty();
        }
        AllowedTimePeriod periods = this.timePeriodsParser.parseAllowedDays(daysTimePattern);
        while ( periods.hasNotDays() ) {                
            daysTimePattern = this.ioEngine.askInput(initiator, "unknown format, try again");
            if ( daysTimePattern.isEmpty() ) {
                return Optional.empty();
            }
            periods.merge(this.timePeriodsParser.parseAllowedDays(daysTimePattern));
        }

        String hoursTimePattern = this.ioEngine.askInput(initiator, "allowed hours");
        if ( hoursTimePattern.isEmpty() ) {
            return Optional.empty();
        }
        periods.merge(this.timePeriodsParser.parseAllowedHours(hoursTimePattern));
        while ( periods.hasNotHours() ) {
            hoursTimePattern = this.ioEngine.askInput(initiator, "unknown format, try again");
            if ( hoursTimePattern.isEmpty() ) {
                return Optional.empty();
            }
            periods.merge(this.timePeriodsParser.parseAllowedHours(hoursTimePattern));
        }
        return Optional.of(periods);
    }

    @Override
    public OperationFlow deleteTask(
            Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(DELETE_TASK) ) {
            return operationFailedWith("wrong command type!");
        }
        
        String text;
        if ( command.hasArg() ) {
            text = command.getArg();
        } else {
            text = this.ioEngine.askInput(initiator, "text");
            if ( text.isEmpty() ) {
                return operationStopped();
            }
        }
        
        List<Task> matchingTasks = this.dao.findTasksByTextPattern(initiator, text);
        Task taskToRemove;
        if ( matchingTasks.isEmpty() ) {
            return operationFailedWith("no tasks with this text.");
        } else if ( hasOne(matchingTasks) ) {
            taskToRemove = getOne(matchingTasks);
        } else {
            Question question = question("choose task").withAnswerEntities(matchingTasks);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                taskToRemove = matchingTasks.get(answer.index());
            } else {
                return operationStopped();
            }
        }
        
        if ( this.dao.deleteTaskById(initiator, taskToRemove.getId()) ) {
            fireAsync("tasks_updated");
            return success();
        } else {
            return operationFailedWith("DAO failed to remove task.");
        }
    }

    @Override
    public OperationFlow editTask(
            Initiator initiator, SingleStringCommand command) {
        if ( command.type().isNot(EDIT_TASK) ) {
            return operationFailedWith("wrong command type!");
        }        
        
        String text;
        if ( command.hasArg() ) {
            text = command.getArg();
        } else {
            text = this.ioEngine.askInput(initiator, "text");
            if ( text.isEmpty() ) {
                return operationStopped();
            }
        }
        
        List<Task> matchingTasks = this.dao.findTasksByTextPattern(initiator, text);
        Task taskToEdit;
        if ( matchingTasks.isEmpty() ) {
            return operationFailedWith("no tasks with this text.");
        } else if ( hasOne(matchingTasks) ) {
            taskToEdit = getOne(matchingTasks);
        } else {
            Question question = question("choose task").withAnswerEntities(matchingTasks);
            Answer answer = this.ioEngine.ask(initiator, question);
            if ( answer.isGiven() ) {
                taskToEdit = matchingTasks.get(answer.index());
            } else {
                return operationStopped();
            }
        }
        
        Question whatToEdit = question("edit").withAnswerStrings("time", "text");
        Answer answer = this.ioEngine.ask(initiator, whatToEdit);
        String target;
        if ( answer.isGiven() ) {
            target = answer.getText();
        } else {
            return operationStopped();
        }
        
        if ( target.equals("time") ) {
            String timePattern = this.ioEngine.askInput(initiator, "new time");
            Optional<TaskTime> optNewTime = this.timeParser.parse(timePattern);
            while ( ! optNewTime.isPresent() ) {                
                timePattern = this.ioEngine.askInput(initiator, "wrong format, try again");
                if ( timePattern.isEmpty() ) {
                    return operationStopped();
                }
                optNewTime = this.timeParser.parse(timePattern);
            }
            TaskTime newTime = optNewTime.get();
            if ( taskToEdit.type().isOneOf(HOURLY_REPEAT, DAILY_REPEAT) ) {
                if ( this.ioEngine.ask(initiator, "edit days/hours").isNotPositive() ) {
                    return operationStopped();
                }
                Optional<AllowedTimePeriod> newPeriods = this.askForAllowedTimePeriod(initiator);
                if ( newPeriods.isPresent() ) {
                    
                } else {
                    return operationStopped();
                }
            } else {
                if ( this.dao.editTaskTime(initiator, taskToEdit.getId(), newTime.actualizedTime()) ) {
                    
                } else {
                    
                }
            }            
        } else if ( target.equals("text") ) {
            
        } else {
            return operationFailedWith("unexpected target to edit: " + target);
        }
        
        fireAsync("tasks_updated");
    }

    @Override
    public List<Task> findTasks(
            Initiator initiator, SingleStringCommand findEntityCommand) {
        
    }  
    
}
