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

import diarsid.beam.core.base.control.flow.ValueFlow;
import diarsid.beam.core.base.control.flow.VoidFlow;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.base.interaction.Answer;
import diarsid.beam.core.base.control.io.base.interaction.Help;
import diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion;
import diarsid.beam.core.base.control.io.commands.ArgumentsCommand;
import diarsid.beam.core.domain.entities.Task;
import diarsid.beam.core.domain.entities.TaskRepeat;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriod;
import diarsid.beam.core.domain.inputparsing.time.AllowedTimePeriodsParser;
import diarsid.beam.core.domain.inputparsing.time.Time;
import diarsid.beam.core.domain.inputparsing.time.TimeAndText;
import diarsid.beam.core.domain.inputparsing.time.TimeParser;
import diarsid.beam.core.modules.data.DaoTasks;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;

import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedEmpty;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowCompletedWith;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.valueFlowStopped;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowCompleted;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowFail;
import static diarsid.beam.core.base.control.flow.Flows.voidFlowStopped;
import static diarsid.beam.core.base.control.io.base.interaction.VariantsQuestion.question;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_TASK;
import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.UNACCEPTABLE_TEXT_CHARS;
import static diarsid.beam.core.base.events.BeamEventRuntime.fireAsync;
import static diarsid.beam.core.base.events.BeamEventRuntime.when;
import static diarsid.beam.core.base.util.CollectionsUtils.getOne;
import static diarsid.beam.core.base.util.CollectionsUtils.hasOne;
import static diarsid.beam.core.base.util.OptionalUtil.isNotPresent;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.domain.entities.TaskRepeat.DAILY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.HOURLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.MONTHLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.NO_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.YEARLY_REPEAT;
import static diarsid.beam.core.domain.entities.TaskRepeat.repeatByItsName;
import static diarsid.beam.core.domain.entities.TaskRepeat.repeatNames;
import static diarsid.beam.core.domain.entities.TaskRepeat.repeatsDescription;
import static diarsid.beam.core.domain.entities.Tasks.newEventTask;
import static diarsid.beam.core.domain.entities.Tasks.newInstantTask;
import static diarsid.beam.core.domain.entities.Tasks.newReminderTask;


public class TasksKeeperWorker implements TasksKeeper {
    
    private final InnerIoEngine ioEngine;
    private final DaoTasks dao;
    private final KeeperDialogHelper helper;
    private final TimeParser parser;
    private final AllowedTimePeriodsParser timePeriodsParser;
    private final Help enterTimePatternHelp;
    private final Help chooseRepeatHelp;
    private final Help enterNewTaskTextHelp;
    private final Help enterTextToDeleteHelp;
    private final Help chooseTaskToDeleteHelp;
    private final Help enterTextToEditHelp;
    private final Help chooseTaskToEditHelp;
    private final Help chooseWhatToEditHelp;
    private final Help enterTextToFindHelp;
    private final Help enterAllowedDaysFormatHelp;
    private final Help enterAlloweHoursFormatsHelp;

    public TasksKeeperWorker(
            InnerIoEngine ioEngine, 
            DaoTasks dao, 
            KeeperDialogHelper helper, 
            TimeParser timeParser,
            AllowedTimePeriodsParser timePeriodsParser) {
        this.ioEngine = ioEngine;
        this.dao = dao;
        this.helper = helper;
        this.parser = timeParser;
        this.timePeriodsParser = timePeriodsParser;        
        this.enterTimePatternHelp = this.ioEngine.addToHelpContext(this.parser.timePatterns());        
        this.chooseRepeatHelp = this.ioEngine.addToHelpContext(repeatsDescription());
        this.enterNewTaskTextHelp = this.ioEngine.addToHelpContext(
                "Enter task text.",
                "Text can have multiple lines and cannot contain",
                "following characters: " + join("", UNACCEPTABLE_TEXT_CHARS) + ".",
                "You can enter any number of lines.",
                "Use:",
                "   - print text and enter for new line",
                "   - dot or empty line to break text input"
        );
        this.enterTextToDeleteHelp = this.ioEngine.addToHelpContext(
                "Enter text of task you want to remove.",
                "Text cannot contain following characters: ",
                "" + join("", UNACCEPTABLE_TEXT_CHARS) + ".",
                "Use:",
                "   - enter to input text",
                "   - dot or empty line to break"
        );
        this.chooseTaskToDeleteHelp = this.ioEngine.addToHelpContext(
                "Choose a task to be removed.",
                "Use:",
                "   - task number to remove it",
                "   - task text part to remove it",
                "   - n/no/. to break"
        );
        this.enterTextToEditHelp = this.ioEngine.addToHelpContext(
                "Enter text of task you want to edit.",
                "Text cannot contain following characters: ",
                "" + join("", UNACCEPTABLE_TEXT_CHARS) + ".",
                "Use:",
                "   - enter to input text",
                "   - dot or empty line to break"
        );
        this.chooseTaskToEditHelp = this.ioEngine.addToHelpContext(
                "Choose a task to be edited.",
                "Use:",
                "   - task number to choose it",
                "   - task text part to choose it",
                "   - n/no/. to break"
        );
        this.chooseWhatToEditHelp = this.ioEngine.addToHelpContext(
                "Choose whether you want to edit task's time or text.",
                "Use:",
                "   - variant number to choose it",
                "   - part of variant to choose it",
                "   - n/no/. to break"
        );
        this.enterTextToFindHelp = this.ioEngine.addToHelpContext(
                "Enter text of task you want to find.",
                "Text cannot contain following characters: ",
                "" + join("", UNACCEPTABLE_TEXT_CHARS) + ".",
                "Use:",
                "   - enter to input text",
                "   - dot or empty line to break"
        );
        this.enterAllowedDaysFormatHelp = this.ioEngine.addToHelpContext(
                this.timePeriodsParser.allowedDaysFormats());
        this.enterAlloweHoursFormatsHelp = this.ioEngine.addToHelpContext(
                this.timePeriodsParser.allowedHoursFormats());
    }

    @Override
    public List<Task> getPastActiveTasks(
            Initiator initiator) {
        return this.dao.getActiveTasksBeforeTime(initiator, now().plusSeconds(1));
    }

    @Override
    public Optional<Long> getInactivePeriodMinutes(
            Initiator initiator) {
        return this.dao
                .getTimeOfFirstActiveTask(initiator)
                .map(time -> Duration.between(time, now()).toMinutes());
    }

    @Override
    public boolean updateTasks(
            Initiator initiator, List<Task> tasks) {
        boolean updated = this.dao.updateTasks(initiator, tasks);
        when(updated).thenFireAsync("tasks_updated");
        return updated;
    }

    @Override
    public Optional<LocalDateTime> getTimeOfFirstTask(
            Initiator initiator) {
        return this.dao.getTimeOfFirstActiveTask(initiator);
    }

    @Override
    public List<Task> getCalendarTasksForNextMonth(
            Initiator initiator, LocalDateTime nextMonthBeginning) {
        return this.dao.getActiveTasksOfTypeBetweenDates(initiator,
                nextMonthBeginning, 
                nextMonthBeginning.plusMonths(1), 
                NO_REPEAT, MONTHLY_REPEAT, YEARLY_REPEAT);
    }

    @Override
    public List<Task> getCalendarTasksForNextWeek(
            Initiator initiator, LocalDateTime nextWeekBeginning) {
        return this.dao.getActiveTasksOfTypeBetweenDates(initiator,
                nextWeekBeginning, 
                nextWeekBeginning.plusWeeks(1), 
                NO_REPEAT, MONTHLY_REPEAT, YEARLY_REPEAT);
    }

    @Override
    public VoidFlow createTask(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(CREATE_TASK) ) {
            return voidFlowFail("wrong command type!");
        }
        
        Optional<Time> optTime;
        String initialText;
        if ( command.hasArguments() ) {
            TimeAndText timeAndText = this.parser.parse(command.arguments());        
            optTime = timeAndText.getTime();
            initialText = timeAndText.getText();
        } else {
            optTime = Optional.empty();
            initialText = "";
        }       
        
        Time taskTime;
        if ( optTime.isPresent() ) {
            taskTime = optTime.get();
        } else {
            String timePattern;
            Optional<Time> parsedTime;
            taskTime = null;
            while ( isNull(taskTime) ) {
                timePattern = this.ioEngine.askInput(initiator, "time", this.enterTimePatternHelp);
                if ( timePattern.isEmpty() ) {
                    return voidFlowStopped();
                }
                parsedTime = this.parser.parse(timePattern);
                if ( parsedTime.isPresent() ) {
                    taskTime = parsedTime.get();
                } else {
                    this.ioEngine.report(initiator, "unknown format.");
                }
            }
        }
        
        if ( isNull(taskTime) ) {
            return voidFlowFail("unexpected null.");
        }
        LocalDateTime time = taskTime.actualTime();
        if ( time.isBefore(now()) ) {
            return voidFlowFail("unexpected past time.");
        }  
        
        TaskRepeat repeat;
        if ( taskTime.isRelative() ) {
            repeat = NO_REPEAT;
        } else {
            VariantsQuestion question = 
                    question("choose repeat").withAnswerStrings(repeatNames());
            Answer answer = this.ioEngine.ask(initiator, question, this.chooseRepeatHelp);
            if ( answer.isGiven() ) {
                repeat = repeatByItsName(answer.text());
            } else {
                return voidFlowStopped();
            }
        }        
        if ( repeat.isUndefined() ) {
            return voidFlowFail("unexpected undefined task repeat.");
        }
        
        List<String> text = new ArrayList<>();
        if ( nonEmpty(initialText) ) {
            text.add(initialText);
        } else {
            String line;
            boolean input = true;
            while ( input ) {                
                line = this.ioEngine.askInput(initiator, "text", this.enterNewTaskTextHelp);
                if ( nonEmpty(line) ) {
                    text.add(line);
                } else {
                    input = false;
                }
            }
        }
        if ( text.isEmpty() ) {
            return voidFlowStopped();
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
                return voidFlowStopped();
            }            
        } else if ( repeat.isOneOf(MONTHLY_REPEAT, YEARLY_REPEAT) ) {
            task = newEventTask(repeat, time, text);
        } else {
            return voidFlowFail("unexpected TaskRepeat value.");
        }
        if ( isNull(task) ) {
            return voidFlowFail("unexpected NULL task");
        }
        
        if ( this.dao.saveTask(initiator, task) ) {
            fireAsync("tasks_updated");
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to save task.");
        }
    }
    
    private Optional<AllowedTimePeriod> askForAllowedTimePeriod(Initiator initiator) {
        String daysTimePattern = this.ioEngine.askInput(
                initiator, "allowed days", this.enterAllowedDaysFormatHelp);
        if ( daysTimePattern.isEmpty() ) {
            return Optional.empty();
        }
        AllowedTimePeriod periods = this.timePeriodsParser.parseAllowedDays(daysTimePattern);
        while ( periods.hasNotDays() ) {                
            daysTimePattern = this.ioEngine.askInput(
                    initiator, "unknown format, try again", this.enterAllowedDaysFormatHelp);
            if ( daysTimePattern.isEmpty() ) {
                return Optional.empty();
            }
            periods.merge(this.timePeriodsParser.parseAllowedDays(daysTimePattern));
        }

        String hoursTimePattern = this.ioEngine.askInput(
                initiator, "allowed hours", this.enterAlloweHoursFormatsHelp);
        if ( hoursTimePattern.isEmpty() ) {
            return Optional.empty();
        }
        periods.merge(this.timePeriodsParser.parseAllowedHours(hoursTimePattern));
        while ( periods.hasNotHours() ) {
            hoursTimePattern = this.ioEngine.askInput(
                    initiator, "unknown format, try again", this.enterAlloweHoursFormatsHelp);
            if ( hoursTimePattern.isEmpty() ) {
                return Optional.empty();
            }
            periods.merge(this.timePeriodsParser.parseAllowedHours(hoursTimePattern));
        }
        return Optional.of(periods);
    }

    @Override
    public VoidFlow deleteTask(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(DELETE_TASK) ) {
            return voidFlowFail("wrong command type!");
        }
        
        String text;
        if ( command.hasArguments()) {
            text = command.joinedArguments();
        } else {
            text = this.ioEngine.askInput(initiator, "text", this.enterTextToDeleteHelp);
            if ( text.isEmpty() ) {
                return voidFlowStopped();
            }
        }
        
        List<Task> matchingTasks = this.dao.findTasksByTextPattern(initiator, text);
        Task taskToRemove;
        if ( matchingTasks.isEmpty() ) {
            return voidFlowFail("no tasks with this text.");
        } else if ( hasOne(matchingTasks) ) {
            taskToRemove = getOne(matchingTasks);
        } else {
            VariantsQuestion question = question("choose task").withAnswerEntities(matchingTasks);
            Answer answer = this.ioEngine.ask(initiator, question, this.chooseTaskToDeleteHelp);
            if ( answer.isGiven() ) {
                taskToRemove = matchingTasks.get(answer.index());
            } else {
                return voidFlowStopped();
            }
        }
        
        if ( this.dao.deleteTaskById(initiator, taskToRemove.id()) ) {
            fireAsync("tasks_updated");
            return voidFlowCompleted();
        } else {
            return voidFlowFail("DAO failed to remove task.");
        }
    }

    @Override
    public VoidFlow editTask(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(EDIT_TASK) ) {
            return voidFlowFail("wrong command type!");
        }        
        
        String text;
        if ( command.hasArguments()) {
            text = command.joinedArguments();
        } else {
            text = this.ioEngine.askInput(initiator, "text", this.enterTextToEditHelp);
            if ( text.isEmpty() ) {
                return voidFlowStopped();
            }
        }
        
        List<Task> matchingTasks = this.dao.findTasksByTextPattern(initiator, text);
        Task taskToEdit;
        if ( matchingTasks.isEmpty() ) {
            return voidFlowFail("no tasks with this text.");
        } else if ( hasOne(matchingTasks) ) {
            taskToEdit = getOne(matchingTasks);
            this.ioEngine.report(
                    initiator, 
                    format("found: %s (%s)", 
                            taskToEdit.stringifyText(),
                            taskToEdit.stringifyTime()));
        } else {
            VariantsQuestion question = question("choose task").withAnswerEntities(matchingTasks);
            Answer answer = this.ioEngine.ask(initiator, question, this.chooseTaskToEditHelp);
            if ( answer.isGiven() ) {
                taskToEdit = matchingTasks.get(answer.index());
            } else {
                return voidFlowStopped();
            }
        }
        
        VariantsQuestion whatToEdit = question("edit").withAnswerStrings("time", "text");
        Answer answer = this.ioEngine.ask(initiator, whatToEdit, this.chooseWhatToEditHelp);
        String target;
        if ( answer.isGiven() ) {
            target = answer.text();
        } else {
            return voidFlowStopped();
        }
        
        if ( target.equals("time") ) {
            String timePattern = this.ioEngine.askInput(
                    initiator, "new time", this.enterTimePatternHelp);
            Optional<Time> optNewTime = this.parser.parse(timePattern);
            while ( isNotPresent(optNewTime) ) {                
                timePattern = this.ioEngine.askInput(
                        initiator, "wrong format, try again", this.enterTimePatternHelp);
                if ( timePattern.isEmpty() ) {
                    return voidFlowStopped();
                }
                optNewTime = this.parser.parse(timePattern);
            }
            Time newTime = optNewTime.get();
            if ( taskToEdit.type().isOneOf(HOURLY_REPEAT, DAILY_REPEAT) ) {
//                if ( this.ioEngine.ask(initiator, "edit days/hours").isNotPositive() ) {
//                    return voidFlowStopped();
//                }
                Optional<AllowedTimePeriod> newPeriods = this.askForAllowedTimePeriod(initiator);
                if ( newPeriods.isPresent() ) {
                    if ( this.dao.editTaskTime(
                            initiator, taskToEdit.id(), newTime.actualTime(), newPeriods.get()) ) {
                        fireAsync("tasks_updated");
                        return voidFlowCompleted();
                    } else {
                        return voidFlowFail("DAO failed to edit task time and periods.");
                    }
                } else {
                    return voidFlowStopped();
                }
            } else {
                if ( this.dao.editTaskTime(initiator, taskToEdit.id(), newTime.actualTime()) ) {
                    fireAsync("tasks_updated");
                    return voidFlowCompleted();
                } else {
                    return voidFlowFail("DAO failed to edit task time.");
                }
            }            
        } else if ( target.equals("text") ) {
            List<String> newText = new ArrayList<>();
            String line;
            boolean input = true;
            while ( input ) {                
                line = this.ioEngine.askInput(initiator, "text", this.enterNewTaskTextHelp);
                if ( nonEmpty(line) ) {
                    newText.add(line);
                } else {
                    input = false;
                }
            }
            if ( newText.isEmpty() ) {
                return voidFlowStopped();
            }
            if ( this.dao.editTaskText(initiator, taskToEdit.id(), newText) ) {
                return voidFlowCompleted();
            } else {
                return voidFlowFail("DAO failed to edit task text.");
            }
        } else {
            return voidFlowFail("unexpected target to edit: " + target);
        }
    }

    @Override
    public ValueFlow<List<Task>> findTasks(
            Initiator initiator, ArgumentsCommand command) {
        if ( command.type().isNot(FIND_TASK) ) {
            return valueFlowFail("wrong command type!");
        }        
        
        String text;
        if ( command.hasArguments()) {
            text = command.joinedArguments();
        } else {
            text = this.ioEngine.askInput(initiator, "text", this.enterTextToFindHelp);
            if ( text.isEmpty() ) {
                return valueFlowStopped();
            }
        }
        
        List<Task> matchingTasks = this.dao.findTasksByTextPattern(initiator, text);
        if ( matchingTasks.isEmpty() ) {
            return valueFlowCompletedEmpty();
        } else {
            return valueFlowCompletedWith(matchingTasks);
        }
    }      
}
