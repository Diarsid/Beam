/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.commands.EmptyCommand;
import diarsid.beam.core.base.control.io.commands.executor.CallBatchCommand;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorDefaultCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenLocationCommand;
import diarsid.beam.core.base.control.io.commands.executor.OpenPathCommand;
import diarsid.beam.core.base.control.io.commands.executor.RunProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.SeePageCommand;
import diarsid.beam.core.base.control.io.commands.executor.StartProgramCommand;
import diarsid.beam.core.base.control.io.commands.executor.StopProgramCommand;
import diarsid.beam.core.base.control.io.interpreter.recognizers.ArgumentsRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.InputCorrectnessRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.MultipleArgsRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.OneArgRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.PauseRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.PrefixRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.RelativePathRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.SimpleWordRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.WordRecognizer;
import diarsid.beam.core.base.control.io.interpreter.recognizers.WordsRecognizer;

import static diarsid.beam.core.base.control.io.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTE;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTE;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.lowerThan;
import static diarsid.beam.core.base.util.StringUtils.normalize;

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
                                "p/").branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
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
                                "run").priority(HIGH).branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new RunProgramCommand(input.currentArg()))),
                        new WordRecognizer(
                                "start").priority(HIGH).branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new StartProgramCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new StartProgramCommand(input.currentArg()))),
                        new WordRecognizer(
                                "stop").priority(HIGH).branchesTo(
                                        new SimpleWordRecognizer().pointsTo(
                                                input -> new StopProgramCommand(input.currentArg())),
                                        new RelativePathRecognizer().pointsTo(
                                                input -> new StopProgramCommand(input.currentArg()))),
                        new WordRecognizer(
                                "pause").priority(HIGH).pointsTo(
                                        new PauseRecognizer()),
                        new WordsRecognizer(
                                "edit", 
                                "change", 
                                "alter").branchesTo(new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(
                                                        new ArgumentsRecognizer(EDIT_LOCATION)),
                                        new WordRecognizer(
                                                "task").pointsTo(
                                                        new ArgumentsRecognizer(EDIT_TASK)),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(
                                                        new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").priority(HIGH).pointsTo(new ArgumentsRecognizer(EDIT_PAGE_DIR)),
                                                        new ArgumentsRecognizer(EDIT_PAGE)                                        
                                        ),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(new ArgumentsRecognizer(EDIT_PAGE_DIR)),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        new ArgumentsRecognizer(EDIT_BATCH))
                        ),
                        new WordsRecognizer(
                                "+", 
                                "add", 
                                "new", 
                                "create").branchesTo(new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(new ArgumentsRecognizer(CREATE_LOCATION)),
                                        new WordRecognizer(
                                                "task").pointsTo(new ArgumentsRecognizer(CREATE_TASK)),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(new ArgumentsRecognizer(CREATE_PAGE_DIR)),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").pointsTo(new ArgumentsRecognizer(CREATE_PAGE_DIR)), 
                                                        new ArgumentsRecognizer(CREATE_PAGE).priority(lowerThan(LOWEST))
                                        ),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        new ArgumentsRecognizer(CREATE_BATCH))
                        ),
                        new WordsRecognizer(
                                "-", 
                                "del", 
                                "delete", 
                                "remove").branchesTo(
                                        new WordsRecognizer(
                                                "loc", 
                                                "location").pointsTo(
                                                        new ArgumentsRecognizer(DELETE_LOCATION)),
                                        new WordRecognizer(
                                                "task").pointsTo(
                                                        new ArgumentsRecognizer(DELETE_TASK)),
                                        new WordsRecognizer(
                                                "dir", 
                                                "direct", 
                                                "directory").pointsTo(
                                                        new ArgumentsRecognizer(DELETE_PAGE_DIR)),
                                        new WordsRecognizer(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").branchesTo(
                                                        new WordsRecognizer(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").pointsTo(
                                                                        new ArgumentsRecognizer(DELETE_PAGE_DIR)),
                                                        new ArgumentsRecognizer(DELETE_PAGE)
                                        ),
                                        new WordsRecognizer(
                                                "bat", 
                                                "batch", 
                                                "exe").pointsTo(
                                                        new ArgumentsRecognizer(DELETE_BATCH))
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
                                                new ArgumentsRecognizer(LIST_LOCATION)), 
                                        new RelativePathRecognizer().pointsTo(
                                                new ArgumentsRecognizer(LIST_PATH))
                        ),
                        new WordsRecognizer(
                                "n", 
                                "note", 
                                "notes").priority(LOW).branchesTo(
                                        new RelativePathRecognizer().pointsTo(
                                                new ArgumentsRecognizer(OPEN_PATH_IN_NOTE)), 
                                        new SimpleWordRecognizer().pointsTo(
                                                new ArgumentsRecognizer(OPEN_TARGET_IN_NOTE))
                        )
                )
        );
    }
    
    public Command interprete(String inputString) {
        return this.decisionTree.assess(new Input(normalize(inputString)));
    }
}
