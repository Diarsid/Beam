/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.recognizers.PluginPrefixesRecognizer;
import diarsid.beam.core.base.control.plugins.Plugin;

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
import static diarsid.beam.core.base.control.io.commands.CommandType.DELETE_MEM;
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
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_ALL;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_MEM;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBDIRECTORY;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.MULTICOMMAND;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGHER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGHEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.higherThan;
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
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognizers.plugins;
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
    private final PluginPrefixesRecognizer plugins;
    
    public Interpreter() {        
        this.plugins = plugins();
        this.decisionTree = this.prepareRecognizersTree();
    }
    
    private Recognizer prepareRecognizersTree() {
        
        return correctInput().andAny(
                this.plugins.priority(HIGH),                
                prefixes(
                        "/", 
                        "l/").priority(HIGH).andAny(
                                domainWord().and(executable(OPEN_LOCATION)),
                                relativePath().and(executable(OPEN_LOCATION_TARGET))),
                prefixes(
                        "w/", 
                        "i/").priority(HIGH).and(domainWord().and(executable(BROWSE_WEBPAGE))),
                prefixes(
                        "r/", 
                        "p/").priority(HIGH).andAny(
                                domainWord().and(executable(RUN_PROGRAM)),
                                relativePath().and(executable(RUN_PROGRAM))),
                prefixes(
                        "b/", 
                        "e/", 
                        "c/").priority(HIGH).and(domainWord().and(executable(CALL_BATCH))),
                controlWord("exit").priority(HIGH).and(only(EXIT)),
                controlWord("close").priority(HIGH).and(only(CLOSE_CONSOLE)),
                controlWords(
                        "n", 
                        "note", 
                        "notes").priority(HIGH).andAny(
                                singleArg().priority(HIGH).and(only(OPEN_NOTES)),
                                relativePath().priority(HIGHER).and(argumentsFor(OPEN_PATH_IN_NOTES)),
                                domainWord().and(argumentsFor(OPEN_TARGET_IN_NOTES)),
                                controlWords(
                                        "+", 
                                        "new", 
                                        "add", 
                                        "create").priority(higherThan(HIGHEST)).and(argumentsFor(CREATE_NOTE))), 
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
                                        "mem", 
                                        "memory", 
                                        "com", 
                                        "comm", 
                                        "command").and(argumentsFor(DELETE_MEM)),
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
                                        "mem", 
                                        "memory", 
                                        "com", 
                                        "comm", 
                                        "command").and(argumentsFor(FIND_MEM)),
                                controlWords(
                                        "bat", 
                                        "batch", 
                                        "exe").and(argumentsFor(FIND_BATCH)),
                                domainWord().priority(LOWER).and(argumentsFor(FIND_ALL))),
                controlWord("list").andAny(
                        domainWord().and(argumentsFor(LIST_LOCATION)), 
                        relativePath().and(argumentsFor(LIST_PATH))),
                relativePath().priority(LOW).and(executable(OPEN_LOCATION_TARGET)),
                singleArg().priority(LOWER).and(domainWord().and(executable(EXECUTOR_DEFAULT))), 
                multipleArgs().priority(lowerThan(LOWEST)).and(argumentsFor(MULTICOMMAND))                
        );
    }
    
    public Command interprete(String inputString) {
        return this.decisionTree.assess(new Input(normalizeSpaces(inputString)));
    }
    
    public boolean install(Plugin plugin) {
        boolean pluginPrefixIsNotFree = 
                this.interprete(plugin.prefix() + "testArgument").type().isDefined() ||
                this.interprete(plugin.prefix() + "test argument").type().isNot(MULTICOMMAND) ||
                this.interprete(plugin.prefix()).type().isDefined();
        if ( pluginPrefixIsNotFree ) {
            return false;
        } else {
            return this.plugins.install(plugin);
        }        
    }
}
