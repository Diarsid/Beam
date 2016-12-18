/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import diarsid.beam.core.control.interpreter.recognizers.PrefixRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.WordsRecognizer;
import diarsid.beam.core.control.commands.Command;
import diarsid.beam.core.control.commands.EmptyCommandProducer;
import diarsid.beam.core.control.commands.executor.CallBatchCommand;
import diarsid.beam.core.control.commands.executor.OpenLocationCommand;
import diarsid.beam.core.control.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.commands.executor.RunMarkedProgramCommand;
import diarsid.beam.core.control.commands.executor.RunProgramCommand;
import diarsid.beam.core.control.commands.executor.SeePageCommand;
import diarsid.beam.core.control.interpreter.recognizers.BatchRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.DirectoryRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.EditEntityRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.EventRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.ExecutorDefaultRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.InputCorrectnessRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.LocationCreationRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.LocationRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.MultipleArgsRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.OneArgRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.OpenNotePathRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.OpenNoteTargetRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.PageCreationRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.PageDirectoryCreationRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.PageRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.RelativePathRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.ReminderRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.RemoveEntityByArgRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.SimpleWordRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.TaskCreationRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.TaskRecognizer;
import diarsid.beam.core.control.interpreter.recognizers.WordRecognizer;

import static diarsid.beam.core.control.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.control.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.control.commands.CommandType.CREATE_EVENT;
import static diarsid.beam.core.control.commands.CommandType.CREATE_REMINDER;
import static diarsid.beam.core.control.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.control.commands.CommandType.DELETE_EVENT;
import static diarsid.beam.core.control.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.control.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.control.commands.CommandType.DELETE_PAGE_DIR;
import static diarsid.beam.core.control.commands.CommandType.DELETE_REMINDER;
import static diarsid.beam.core.control.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.control.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.control.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.control.commands.CommandType.EDIT_PAGE_DIR;
import static diarsid.beam.core.control.commands.CommandType.EXIT;
import static diarsid.beam.core.control.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.control.interpreter.PrioritizedRecognizerWrapper.prioritized;
import static diarsid.beam.core.control.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.control.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.control.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.control.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.control.interpreter.RecognizerPriority.lowerThan;
import static diarsid.beam.core.control.interpreter.recognizers.RemoveEntityByArgRecognizer.ArgumentsMode.JOIN_ALL_REMAINING_ARGS;
import static diarsid.beam.core.control.interpreter.recognizers.RemoveEntityByArgRecognizer.ArgumentsMode.USE_FIRST_REMAINING_ARG;
import static diarsid.beam.core.util.StringUtils.normalize;

/**
 * Class that interprets CLI input commands and transforms them into
 * an object of Command subclass.
 * 
 * Uses predefined tree structure (decision tree) of Recognizer objects 
 * that assess each part of given command line and decide whether to 
 * abort parsing in their branch or to dispatch input deeper to their 
 * children. 
 * 
 * The last element in such branch usually makes a final
 * decision - whether to create Command object of appropriate 
 * Command subclass and/or type representing particular action 
 * with or without arguments or return a singleton instance of 
 * EmptyCommand class with UNDEFINED command type.
 * 
 * @author Diarsid
 */
public class Interpreter {
    
    private final Recognizer decisionTree;
    
    public Interpreter() {        
        this.decisionTree = this.prepareRecognizersTree();
    }
    
    private Recognizer prepareRecognizersTree() {
        
        return new InputCorrectnessRecognizer().branchesTo(
                new OneArgRecognizer().priority(HIGH).branchesTo(
                        new PrefixRecognizer("/", "l/").branchesTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new OpenLocationCommand(input.currentArg())),
                                new RelativePathRecognizer().pointsTo(
                                        input -> new OpenPathCommand(input.currentArg()))
                        ),
                        new PrefixRecognizer("w/", "i/").pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new SeePageCommand(input.currentArg()))),
                        new PrefixRecognizer("r/", "p/").pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new RunProgramCommand(input.currentArg()))),
                        new PrefixRecognizer("b/", "e/", "c/").pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new CallBatchCommand(input.currentArg()))),
                        new WordRecognizer("exit").pointsTo(
                                new EmptyCommandProducer(EXIT)),
                        new WordRecognizer("close").pointsTo(
                                new EmptyCommandProducer(CLOSE_CONSOLE)),
                        new WordsRecognizer("n", "note", "notes").pointsTo(
                                new EmptyCommandProducer(OPEN_NOTES)),
                        new SimpleWordRecognizer().priority(LOWEST).pointsTo(
                                new ExecutorDefaultRecognizer()), 
                        new RelativePathRecognizer().priority(LOWER).pointsTo(
                                input -> new OpenPathCommand(input.currentArg()))
                ), 
                new MultipleArgsRecognizer().branchesTo(
                        new WordsRecognizer("see", "www").priority(HIGH).pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new SeePageCommand(input.currentArg()))
                        ), 
                        new WordsRecognizer("o", "op", "open").priority(HIGH).branchesTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new OpenLocationCommand(input.currentArg())),
                                new RelativePathRecognizer().pointsTo(
                                        input -> new OpenPathCommand(input.currentArg()))
                        ),
                        new WordsRecognizer("call", "exe", "exec").priority(HIGH).pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new CallBatchCommand(input.currentArg()))
                        ), 
                        new WordsRecognizer("r", "run").priority(HIGH).pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new RunProgramCommand(input.currentArg()))),
                        new WordRecognizer("start").priority(HIGH).pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new RunMarkedProgramCommand(input.currentArg(), "start"))),
                        new WordRecognizer("stop").priority(HIGH).pointsTo(
                                new SimpleWordRecognizer().pointsTo(
                                        input -> new RunMarkedProgramCommand(input.currentArg(), "stop"))),
                        new WordsRecognizer("edit", "change", "alter").branchesTo(
                                new LocationRecognizer().branchesTo(),
                                new TaskRecognizer().branchesTo(),
                                new ReminderRecognizer(),
                                new EventRecognizer(),
                                new PageRecognizer().branchesTo(
                                        new DirectoryRecognizer().priority(HIGH).pointsTo(
                                                new EditEntityRecognizer(EDIT_PAGE_DIR)),
                                        new EditEntityRecognizer(EDIT_PAGE)                                        
                                ),
                                new DirectoryRecognizer().pointsTo(
                                        new EditEntityRecognizer(EDIT_PAGE_DIR)),
                                new BatchRecognizer().pointsTo(
                                        new EditEntityRecognizer(EDIT_BATCH))
                        ),
                        new WordsRecognizer("+", "add", "new", "create").branchesTo(
                                new LocationRecognizer().pointsTo(
                                        new LocationCreationRecognizer()),
                                new TaskRecognizer().pointsTo(
                                        new TaskCreationRecognizer()),
                                new ReminderRecognizer().pointsTo(
                                        new EmptyCommandProducer(CREATE_REMINDER)),
                                new EventRecognizer().pointsTo(
                                        new EmptyCommandProducer(CREATE_EVENT)),
                                new DirectoryRecognizer().pointsTo(
                                        new PageDirectoryCreationRecognizer()),
                                new PageRecognizer().branchesTo(
                                        new DirectoryRecognizer().pointsTo(
                                                new PageDirectoryCreationRecognizer()), 
                                        prioritized(new PageCreationRecognizer()).priority(lowerThan(LOWEST))
                                ),
                                new BatchRecognizer().pointsTo(
                                        new EmptyCommandProducer(CREATE_BATCH))
                        ),
                        new WordsRecognizer("-", "del", "delete", "remove").branchesTo(
                                new LocationRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_LOCATION, USE_FIRST_REMAINING_ARG)),
                                new TaskRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_TASK, JOIN_ALL_REMAINING_ARGS)),
                                new ReminderRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_REMINDER, JOIN_ALL_REMAINING_ARGS)),
                                new EventRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_EVENT, JOIN_ALL_REMAINING_ARGS)),
                                new DirectoryRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_PAGE_DIR, USE_FIRST_REMAINING_ARG)),
                                new PageRecognizer().branchesTo(
                                        new DirectoryRecognizer().pointsTo(
                                                new RemoveEntityByArgRecognizer(DELETE_PAGE_DIR, USE_FIRST_REMAINING_ARG)),
                                        new RemoveEntityByArgRecognizer(DELETE_PAGE, USE_FIRST_REMAINING_ARG)
                                ),
                                new BatchRecognizer().pointsTo(
                                        new RemoveEntityByArgRecognizer(DELETE_BATCH, USE_FIRST_REMAINING_ARG))
                        ),
                        new WordsRecognizer("?", "get", "find").branchesTo(
                                new TaskRecognizer().branchesTo(), 
                                new ReminderRecognizer(),
                                new EventRecognizer(),
                                new LocationRecognizer().branchesTo(),
                                new PageRecognizer().branchesTo(),
                                new DirectoryRecognizer(),
                                new BatchRecognizer().branchesTo()
                        ),
                        new WordRecognizer("list").branchesTo(
                                new SimpleWordRecognizer(), 
                                new RelativePathRecognizer()
                        ),
                        new WordsRecognizer("n", "note", "notes").priority(LOW).branchesTo(
                                new RelativePathRecognizer().pointsTo(
                                        new OpenNotePathRecognizer()), 
                                new SimpleWordRecognizer().pointsTo(
                                        new OpenNoteTargetRecognizer())
                        )
                )
        );
    }
    
    public Command interprete(String inputString) {
        System.out.println("INTERPRETE STRING: " + inputString);
        return this.decisionTree.assess(new Input(normalize(inputString)));
    }
}
