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
import static diarsid.beam.core.base.util.StringUtils.normalize;

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
        
        return correctInput().withAny(
                singleArg().priority(HIGH).withAny(
                        prefixes(
                                "/", 
                                "l/").withAny(
                                        domainWord().with(executable(OPEN_LOCATION)),
                                        relativePath().with(executable(OPEN_LOCATION_TARGET))),
                        prefixes(
                                "w/", 
                                "i/").with(domainWord().with(executable(BROWSE_WEBPAGE))),
                        prefixes(
                                "r/", 
                                "p/").withAny(
                                        domainWord().with(executable(RUN_PROGRAM)),
                                        relativePath().with(executable(RUN_PROGRAM))),
                        prefixes(
                                "b/", 
                                "e/", 
                                "c/").with(domainWord().with(executable(CALL_BATCH))),
                        controlWord("exit").with(only(EXIT)),
                        controlWord("close").with(only(CLOSE_CONSOLE)),
                        controlWords(
                                "n", 
                                "note", 
                                "notes").withAny(
                                        controlWords(
                                                "+", 
                                                "new", 
                                                "add", 
                                                "create").with(argumentsFor(CREATE_NOTE)),
                                        only(OPEN_NOTES)),
                        domainWord().priority(LOWEST).with(executable(EXECUTOR_DEFAULT)), 
                        relativePath().priority(LOWER).with(executable(OPEN_LOCATION_TARGET))),
                multipleArgs().withAny(controlWords(
                                "see", 
                                "www",
                                "browse").priority(HIGH).with(domainWord().with(executable(BROWSE_WEBPAGE))), 
                        controlWords(
                                "o", 
                                "op", 
                                "open").priority(HIGH).withAny(
                                        domainWord().with(executable(OPEN_LOCATION)),
                                        relativePath().with(executable(OPEN_LOCATION_TARGET))),
                        controlWords(
                                "call", 
                                "exe", 
                                "exec").priority(HIGH).with(domainWord().with(executable(CALL_BATCH))), 
                        controlWords(
                                "r", 
                                "run").priority(HIGH).withAny(
                                        domainWord().with(executable(RUN_PROGRAM)),
                                        relativePath().with(executable(RUN_PROGRAM))),
                        controlWord("start").priority(HIGH).withAny(
                                        domainWord().with(executableWith(RUN_PROGRAM, "start")),
                                        relativePath().with(executableWith(RUN_PROGRAM, "start"))),
                        controlWord("stop").priority(HIGH).withAny(
                                        domainWord().with(executableWith(RUN_PROGRAM, "stop")),
                                        relativePath().with(executableWith(RUN_PROGRAM, "stop"))),
                        controlWord("pause").priority(HIGH).with(pause()),
                        controlWords(
                                "edit", 
                                "change", 
                                "alter").withAny(
                                        controlWords(
                                                "loc", 
                                                "location").with(argumentsFor(EDIT_LOCATION)),
                                        controlWords(
                                                "task").with(argumentsFor(EDIT_TASK)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").withAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").priority(HIGH).with(argumentsFor(EDIT_WEB_DIR)),
                                                        argumentsFor(EDIT_PAGE)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory").with(argumentsFor(EDIT_WEB_DIR)),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").with(argumentsFor(EDIT_BATCH))),
                        controlWords(
                                "+", 
                                "add", 
                                "new", 
                                "create").withAny(
                                        controlWords(
                                                "loc", 
                                                "location").with(argumentsFor(CREATE_LOCATION)),
                                        controlWord(
                                                "task").with(argumentsFor(CREATE_TASK)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory",
                                                "webdir",
                                                "webdirectory").with(argumentsFor(CREATE_WEB_DIR)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").withAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").with(argumentsFor(CREATE_WEB_DIR)), 
                                                        argumentsFor(CREATE_PAGE).priority(lowerThan(LOWEST))),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").with(argumentsFor(CREATE_BATCH)),
                                        controlWords(
                                                "n", 
                                                "note", 
                                                "not", 
                                                "nt").withAny(argumentsFor(CREATE_NOTE))),
                        controlWords(
                                "-", 
                                "del", 
                                "delete", 
                                "remove").withAny(
                                        controlWords(
                                                "loc", 
                                                "location").with(argumentsFor(DELETE_LOCATION)),
                                        controlWord("task").with(argumentsFor(DELETE_TASK)),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory").with(argumentsFor(DELETE_WEB_DIR)),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web").withAny(
                                                        controlWords(
                                                                "dir", 
                                                                "direct", 
                                                                "directory").with(argumentsFor(DELETE_WEB_DIR)),
                                                        argumentsFor(DELETE_PAGE)
                                        ),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe").with(argumentsFor(DELETE_BATCH))),
                        controlWords(
                                "?", 
                                "get", 
                                "find").withAny(
                                        controlWord("task"), 
                                        controlWords(
                                                "reminder", 
                                                "rem", 
                                                "remind"),
                                        controlWord("event"),
                                        controlWords(
                                                "loc", 
                                                "location"),
                                        controlWords(
                                                "page", 
                                                "webpage", 
                                                "webp", 
                                                "web"),
                                        controlWords(
                                                "dir", 
                                                "direct", 
                                                "directory"),
                                        controlWords(
                                                "bat", 
                                                "batch", 
                                                "exe")),
                        controlWord("list").withAny(
                                domainWord().with(argumentsFor(LIST_LOCATION)), 
                                relativePath().with(argumentsFor(LIST_PATH))),
                        controlWords(
                                "n", 
                                "note", 
                                "notes").priority(LOW).withAny(
                                        relativePath().priority(HIGHER).with(argumentsFor(OPEN_PATH_IN_NOTES)), 
                                        domainWord().with(argumentsFor(OPEN_TARGET_IN_NOTES)))
                )
        );
    }
    
    public Command interprete(String inputString) {
        return this.decisionTree.assess(new Input(normalize(inputString)));
    }
}
