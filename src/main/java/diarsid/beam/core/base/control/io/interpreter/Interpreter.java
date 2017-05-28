/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import diarsid.beam.core.base.control.io.commands.Command;

import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CLOSE_CONSOLE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_NOTE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.CREATE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.EDIT_WEB_DIR;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXECUTOR_DEFAULT;
import static diarsid.beam.core.base.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBDIRECTORY;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGHER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.lowerThan;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.argumentsFor;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.controlWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.controlWords;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.correctInput;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.domainWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.executable;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.executableWith;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.multipleArgs;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.only;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.pause;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.prefixes;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.relativePath;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.singleArg;
import static diarsid.beam.core.base.util.StringUtils.normalizeSpaces;

/**
 * Class that interprets CLI input commands and transforms them into
 * an object of Command subclass.
 * 
 * Uses a predefined tree structure (decision tree) of Recognizer objects 
 * that assess each part of a given command line and decide whether to 
 * abort parsing in their branch or to dispatch an input deeper to their 
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
        
        return correctInput().andAny(
                singleArg().priority(HIGH).andAny(
                        prefixes(
                                "/", 
                                "l/").andAny(
                                        domainWord().and(executable(OPEN_LOCATION)),
                                        relativePath().and(executable(OPEN_LOCATION_TARGET))),
                        prefixes(
                                "w/", 
                                "i/").and(domainWord().and(executable(BROWSE_WEBPAGE))),
                        prefixes(
                                "r/", 
                                "p/").andAny(
                                        domainWord().and(executable(RUN_PROGRAM)),
                                        relativePath().and(executable(RUN_PROGRAM))),
                        prefixes(
                                "b/", 
                                "e/", 
                                "c/").and(domainWord().and(executable(CALL_BATCH))),
                        controlWord("exit").and(only(EXIT)),
                        controlWord("close").and(only(CLOSE_CONSOLE)),
                        controlWords(
                                "n", 
                                "note", 
                                "notes").andAny(
                                        controlWords(
                                                "+", 
                                                "new", 
                                                "add", 
                                                "create").and(argumentsFor(CREATE_NOTE)),
                                        only(OPEN_NOTES)),
                        domainWord().priority(LOWEST).and(executable(EXECUTOR_DEFAULT)), 
                        relativePath().priority(LOWER).and(executable(OPEN_LOCATION_TARGET))),
                multipleArgs().andAny(
                        controlWords(
                                "see", 
                                "www",
                                "browse").priority(HIGH).and(domainWord().and(executable(BROWSE_WEBPAGE))), 
                        controlWords(
                                "o", 
                                "op", 
                                "open").priority(HIGH).andAny(
                                        domainWord().and(executable(OPEN_LOCATION)),
                                        relativePath().and(executable(OPEN_LOCATION_TARGET))),
                        controlWords(
                                "call", 
                                "exe", 
                                "exec").priority(HIGH).and(domainWord().and(executable(CALL_BATCH))), 
                        controlWords(
                                "r", 
                                "run").priority(HIGH).andAny(
                                        domainWord().and(executable(RUN_PROGRAM)),
                                        relativePath().and(executable(RUN_PROGRAM))),
                        controlWord("start").priority(HIGH).andAny(
                                        domainWord().and(executableWith(RUN_PROGRAM, "start")),
                                        relativePath().and(executableWith(RUN_PROGRAM, "start"))),
                        controlWord("stop").priority(HIGH).andAny(
                                        domainWord().and(executableWith(RUN_PROGRAM, "stop")),
                                        relativePath().and(executableWith(RUN_PROGRAM, "stop"))),
                        controlWord("pause").priority(HIGH).and(pause()),
                        controlWords(
                                "edit", 
                                "change", 
                                "alter").andAny(
                                        controlWords(
                                                "loc", 
                                                "location").and(argumentsFor(EDIT_LOCATION)),
                                        controlWords(
                                                "task").and(argumentsFor(EDIT_TASK)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").andAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").priority(HIGH).and(argumentsFor(EDIT_WEB_DIR)),
                                                        argumentsFor(EDIT_PAGE)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory").and(argumentsFor(EDIT_WEB_DIR)),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").and(argumentsFor(EDIT_BATCH))),
                        controlWords(
                                "+", 
                                "add", 
                                "new", 
                                "create").andAny(
                                        controlWords(
                                                "loc", 
                                                "location").and(argumentsFor(CREATE_LOCATION)),
                                        controlWord(
                                                "task").and(argumentsFor(CREATE_TASK)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory",
                                                "webdir",
                                                "webdirectory").and(argumentsFor(CREATE_WEB_DIR)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").andAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").and(argumentsFor(CREATE_WEB_DIR)), 
                                                        argumentsFor(CREATE_PAGE).priority(lowerThan(LOWEST))),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").and(argumentsFor(CREATE_BATCH)),
                                        controlWords(
                                                "n", 
                                                "note", 
                                                "not", 
                                                "nt").andAny(argumentsFor(CREATE_NOTE))),
                        controlWords(
                                "-", 
                                "del", 
                                "delete", 
                                "remove").andAny(
                                        controlWords(
                                                "loc", 
                                                "location").and(argumentsFor(DELETE_LOCATION)),
                                        controlWord("task").and(argumentsFor(DELETE_TASK)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory").and(argumentsFor(DELETE_WEB_DIR)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").andAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").and(argumentsFor(DELETE_WEB_DIR)),
                                                        argumentsFor(DELETE_PAGE)
                                        ),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").and(argumentsFor(DELETE_BATCH))),
                        controlWords(
                                "?", 
                                "get", 
                                "find").andAny(
                                        controlWord("task").and(argumentsFor(FIND_TASK)), 
                                        controlWords(
                                                "reminder", 
                                                "rem", 
                                                "remind").and(argumentsFor(FIND_TASK)),
                                        controlWord("event"),
                                        controlWords(
                                                "loc", 
                                                "location").and(argumentsFor(FIND_LOCATION)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").and(argumentsFor(FIND_PAGE)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory").and(argumentsFor(FIND_WEBDIRECTORY)),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").and(argumentsFor(FIND_BATCH))),
                        controlWord("list").andAny(
                                domainWord().and(argumentsFor(LIST_LOCATION)), 
                                relativePath().and(argumentsFor(LIST_PATH))),
                        controlWords(
                                "n", 
                                "note", 
                                "notes").priority(LOW).andAny(
                                        relativePath().priority(HIGHER).and(argumentsFor(OPEN_PATH_IN_NOTES)), 
                                        domainWord().and(argumentsFor(OPEN_TARGET_IN_NOTES)))
                )
        );
    }
    
    public Command interprete(String inputString) {
        return this.decisionTree.assess(new Input(normalizeSpaces(inputString)));
    }
}
