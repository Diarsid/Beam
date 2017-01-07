/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.interpreter;

import diarsid.beam.core.control.io.commands.Command;
import diarsid.beam.core.control.io.commands.CreateEntityCommand;
import diarsid.beam.core.control.io.commands.EmptyCommand;
import diarsid.beam.core.control.io.commands.ExecutorCommand;
import diarsid.beam.core.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.control.io.commands.executor.SeePageCommand;
import diarsid.beam.core.control.io.interpreter.recognizers.CreateLocationRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.CreateTaskRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.CreateWebDirectoryRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.CreateWebPageRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.EditEntityRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.InputCorrectnessRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.MultipleArgsRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.OneArgRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.PauseRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.PrefixRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.RelativePathRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.RemoveEntityByArgRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.SimpleWordRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.WordRecognizer;
import diarsid.beam.core.control.io.interpreter.recognizers.WordsRecognizer;

import static diarsid.beam.core.control.io.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_EVENT;
import static diarsid.beam.core.control.io.commands.CommandType.CREATE_REMINDER;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_EVENT;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_PAGE_DIR;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_REMINDER;
import static diarsid.beam.core.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_EVENT;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_PAGE_DIR;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_REMINDER;
import static diarsid.beam.core.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_PATH_IN_NOTE;
import static diarsid.beam.core.control.io.commands.CommandType.OPEN_TARGET_IN_NOTE;
import static diarsid.beam.core.control.io.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.control.io.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.control.io.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.control.io.interpreter.RecognizerPriority.lowerThan;
import static diarsid.beam.core.control.io.interpreter.recognizers.RemoveEntityByArgRecognizer.ArgumentsMode.JOIN_ALL_REMAINING_ARGS;
import static diarsid.beam.core.control.io.interpreter.recognizers.RemoveEntityByArgRecognizer.ArgumentsMode.USE_FIRST_REMAINING_ARG;
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
                        new PrefixRecognizer(
                                "/", 
                                "l/").branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new OpenLocationCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new OpenPathCommand(input.currentArg()))
                        ),
                        new PrefixRecognizer(
                                "w/", 
                                "i/").pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new SeePageCommand(input.currentArg()))),
                        new PrefixRecognizer(
                                "r/", 
                                "p/").pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg()))),
                        new PrefixRecognizer(
                                "b/", 
                                "e/", 
                                "c/").pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new CallBatchCommand(input.currentArg()))),
                        new WordRecognizer(
                                "exit").pointsTo(
                                        input -> new EmptyCommand(EXIT)),
                        new WordRecognizer(
                                "close").pointsTo(
                                        input -> new EmptyCommand(CLOSE_CONSOLE)),
                        new WordsRecognizer(
                                "n", 
                                "note", 
                                "notes").pointsTo(
                                        input -> new EmptyCommand(OPEN_NOTES)),
                        new SimpleWordRecognizer().priority(LOWEST).pointsTo(
                                input -> new ExecutorDefaultCommand(input.currentArg())), 
                        new RelativePathRecognizer().priority(LOWER).pointsTo(
                                input -> new OpenPathCommand(input.currentArg()))
                ),
                new MultipleArgsRecognizer().branchesTo(new WordsRecognizer(
                                "see", 
                                "www").priority(HIGH).pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new SeePageCommand(input.currentArg()))
                        ), 
                        new WordsRecognizer(
                                "o", 
                                "op", 
                                "open").priority(HIGH).branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new OpenLocationCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new OpenPathCommand(input.currentArg()))
                        ),
                        new WordsRecognizer(
                                "call", 
                                "exe", 
                                "exec").priority(HIGH).pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new CallBatchCommand(input.currentArg()))
                        ), 
                        new WordsRecognizer(
                                "r", 
                                "run").priority(HIGH).pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg()))),
                        new WordRecognizer(
                                "start").priority(HIGH).pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg() + "-start"))),
                        new WordRecognizer(
                                "stop").priority(HIGH).pointsTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg() + "-stop"))),
                        new WordRecognizer(
                                "pause").priority(HIGH).pointsTo(
                                        new PauseRecognizer()),
                        new WordsRecognizer(
                                "edit", 
                                "change", 
                                "alter").branchesTo(
                                        new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(
                                                        new EditEntityRecognizer(EDIT_LOCATION)),
                                        new WordRecognizer(
                                                "task").pointsTo(
                                                        new TimeEntityEditRecognizer(EDIT_TASK)),
                                        new WordsRecognizer(
                                                "reminder", 
                                                "rem", 
                                                "remind").pointsTo(
                                                        new TimeEntityEditRecognizer(EDIT_REMINDER)),
                                        new WordRecognizer(
                                                "event").pointsTo(
                                                        new TimeEntityEditRecognizer(EDIT_EVENT)),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(
                                                        new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").priority(HIGH).pointsTo(
                                                                        new EditEntityRecognizer(EDIT_PAGE_DIR)),
                                                        new EditEntityRecognizer(EDIT_PAGE)                                        
                                        ),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(
                                                        new EditEntityRecognizer(EDIT_PAGE_DIR)),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        new EditEntityRecognizer(EDIT_BATCH))
                        ),
                        new WordsRecognizer(
                                "+", 
                                "add", 
                                "new", 
                                "create").branchesTo(new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(
                                                        new CreateLocationRecognizer()),
                                        new WordRecognizer(
                                                "task").pointsTo(
                                                        new CreateTaskRecognizer()),
                                        new WordsRecognizer(
                                                "reminder", 
                                                "rem", 
                                                "remind").pointsTo(
                                                        input -> new CreateEntityCommand(CREATE_REMINDER)),
                                        new WordRecognizer(
                                                "event").pointsTo(
                                                        input -> new CreateEntityCommand(CREATE_EVENT)),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(
                                                        new CreateWebDirectoryRecognizer()),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(
                                                        new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").pointsTo(
                                                                        new CreateWebDirectoryRecognizer()), 
                                                        new CreateWebPageRecognizer().priority(lowerThan(LOWEST))
                                        ),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        input -> new CreateEntityCommand(CREATE_BATCH))
                        ),
                        new WordsRecognizer(
                                "-", 
                                "del", 
                                "delete", 
                                "remove").branchesTo(
                                        new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_LOCATION, 
                                                                USE_FIRST_REMAINING_ARG)),
                                        new WordRecognizer(
                                                "task").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_TASK, 
                                                                JOIN_ALL_REMAINING_ARGS)),
                                        new WordsRecognizer(
                                                "reminder", 
                                                "rem", 
                                                "remind").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_REMINDER, 
                                                                JOIN_ALL_REMAINING_ARGS)),
                                        new WordRecognizer(
                                                "event").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_EVENT, 
                                                                JOIN_ALL_REMAINING_ARGS)),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_PAGE_DIR, 
                                                                USE_FIRST_REMAINING_ARG)),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(
                                                        new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").pointsTo(
                                                                        new RemoveEntityByArgRecognizer(
                                                                                DELETE_PAGE_DIR, 
                                                                                USE_FIRST_REMAINING_ARG)),
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_PAGE, 
                                                                USE_FIRST_REMAINING_ARG)
                                        ),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        new RemoveEntityByArgRecognizer(
                                                                DELETE_BATCH, 
                                                                USE_FIRST_REMAINING_ARG))
                        ),
                        new WordsRecognizer(
                                "?", 
                                "get", 
                                "find").branchesTo(
                                new WordRecognizer(
                                        "task"), 
                                new WordsRecognizer(
                                        "reminder", 
                                        "rem", 
                                        "remind"),
                                new WordRecognizer(
                                        "event"),
                                new WordsRecognizer(
                                        "loc", 
                                        "location"),
                                new WordsRecognizer(
                                        "page", 
                                        "webpage", 
                                        "webp", 
                                        "web"),
                                new WordsRecognizer(
                                        "dir", 
                                        "direct", 
                                        "directory"),
                                new WordsRecognizer(
                                        "bat", 
                                        "batch", 
                                        "exe")
                        ),
                        new WordRecognizer(
                                "list").branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new ExecutorCommand(
                                                        input.currentArg(), 
                                                        LIST_LOCATION)), 
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new ExecutorCommand(
                                                        input.currentArg(), 
                                                        LIST_PATH))
                        ),
                        new WordsRecognizer(
                                "n", 
                                "note", 
                                "notes").priority(LOW).branchesTo(
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new ExecutorCommand(
                                                        input.currentArg(), 
                                                        OPEN_PATH_IN_NOTE)), 
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new ExecutorCommand(
                                                        input.currentArg(), 
                                                        OPEN_TARGET_IN_NOTE))
                        )
                )
        );
    }
    
    public Command interprete(String inputString) {
        return this.decisionTree.assess(new Input(normalize(inputString)));
    }
}
