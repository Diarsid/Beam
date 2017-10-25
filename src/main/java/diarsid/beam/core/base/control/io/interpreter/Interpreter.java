/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.util.Set;

import diarsid.beam.core.base.control.io.commands.Command;
import diarsid.beam.core.base.control.io.interpreter.recognizers.ControlWordsContext;
import diarsid.beam.core.base.control.io.interpreter.recognizers.PluginsRecognizer;
import diarsid.beam.core.base.control.plugins.Plugin;

import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.BROWSE_WEBPANEL;
import static diarsid.beam.core.base.control.io.commands.CommandType.CALL_BATCH;
import static diarsid.beam.core.base.control.io.commands.CommandType.CAPTURE_PAGE_IMAGE;
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
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_TASK;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBDIRECTORY;
import static diarsid.beam.core.base.control.io.commands.CommandType.FIND_WEBPAGE;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.LIST_PATH;
import static diarsid.beam.core.base.control.io.commands.CommandType.MULTICOMMAND;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_LOCATION_TARGET;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_PATH_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.OPEN_TARGET_IN_NOTES;
import static diarsid.beam.core.base.control.io.commands.CommandType.RUN_PROGRAM;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_ALL_BATCHES;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_ALL_LOCATIONS;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_ALL_PROGRAMS;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_ALL_WEBDIRECTORIES;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_ALL_WEBPAGES;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_BOOKMARKS;
import static diarsid.beam.core.base.control.io.commands.CommandType.SHOW_WEBPANEL;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.incorrectCommand;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGH;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGHER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.HIGHEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOW;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWER;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.LOWEST;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.higherThan;
import static diarsid.beam.core.base.control.io.interpreter.RecognizerPriority.lowerThan;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.argumentsFor;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.controlWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.controlWords;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.controlWordsContext;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.correctInput;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.domainWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.executable;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.executableWith;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.independentWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.independentWords;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.justControlWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.justControlWords;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.mediatoryControlWord;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.mediatoryControlWords;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.multipleArgs;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.only;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.pause;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.plugins;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.prefixes;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.relativePath;
import static diarsid.beam.core.base.control.io.interpreter.recognizers.Recognition.singleArg;
import static diarsid.beam.core.base.util.Logs.debug;
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
    private final PluginsRecognizer plugins;
    private final ControlWordsContext controlWordsContext;
    
    public Interpreter() {        
        this.plugins = plugins();
        this.decisionTree = this.prepareRecognizersTree();
        this.controlWordsContext = controlWordsContext();
    }
    
    private Recognizer prepareRecognizersTree() {
        
        return correctInput().andAny(this.plugins.priority(HIGHEST),                
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
                justControlWord(independentWord("exit")).priority(HIGH).and(only(EXIT)),
                justControlWord(independentWord("close")).priority(HIGH).and(only(CLOSE_CONSOLE)),
                controlWords(independentWords(
                        "n", 
                        "note", 
                        "notes")).priority(HIGH).andAny(
                                singleArg().priority(HIGH).and(only(OPEN_NOTES)),
                                relativePath().priority(HIGHER).and(argumentsFor(OPEN_PATH_IN_NOTES)),
                                domainWord().and(argumentsFor(OPEN_TARGET_IN_NOTES)),
                                mediatoryControlWords(
                                        "+", 
                                        "new", 
                                        "add", 
                                        "create").priority(higherThan(HIGHEST)).and(argumentsFor(CREATE_NOTE))), 
                mediatoryControlWords(
                        "see", 
                        "www",
                        "browse").priority(HIGH).and(domainWord().and(executable(BROWSE_WEBPAGE))), 
                mediatoryControlWords(
                        "o", 
                        "op", 
                        "open").priority(HIGH).andAny(
                                domainWord().and(executable(OPEN_LOCATION)),
                                relativePath().and(executable(OPEN_LOCATION_TARGET))),
                mediatoryControlWords(
                        "call", 
                        "exe", 
                        "exec").priority(HIGH).and(domainWord().and(executable(CALL_BATCH))), 
                mediatoryControlWords(
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
                justControlWords(independentWords(
                        "webpanel",
                        "wpanel",
                        "panel")).and(only(BROWSE_WEBPANEL)),
                controlWord("pause").priority(HIGH).and(pause()),
                mediatoryControlWords(
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
                mediatoryControlWords(
                        "+", 
                        "add", 
                        "new", 
                        "create").andAny(controlWords(
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
                                                        "image", 
                                                        "img", 
                                                        "picture", 
                                                        "pic", 
                                                        "icon", 
                                                        "ico").and(argumentsFor(CAPTURE_PAGE_IMAGE)),
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
                mediatoryControlWords(
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
                                                mediatoryControlWords(
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
                mediatoryControlWord("all").priority(HIGHER).andAny(
                        justControlWords(
                                "locs", 
                                "locations").and(only(SHOW_ALL_LOCATIONS)),
                        justControlWords(
                                "programs", 
                                "progs").and(only(SHOW_ALL_PROGRAMS)),
                        justControlWords(
                                "bats", 
                                "batches", 
                                "exes").and(only(SHOW_ALL_BATCHES)),
                        justControlWords(
                                "pages", 
                                "webpages", 
                                "webps").and(only(SHOW_ALL_WEBPAGES)),
                        justControlWords(
                                "dirs", 
                                "directs", 
                                "directories").and(only(SHOW_ALL_WEBDIRECTORIES))),
                mediatoryControlWords(
                        "?", 
                        "get", 
                        "find").andAny(
                                mediatoryControlWord("all").priority(HIGHER).andAny(
                                        justControlWords(
                                                "locs", 
                                                "locations").and(only(SHOW_ALL_LOCATIONS)),
                                        justControlWords(
                                                "programs", 
                                                "progs").and(only(SHOW_ALL_PROGRAMS)),
                                        justControlWords(
                                                "bats", 
                                                "batches", 
                                                "exes").and(only(SHOW_ALL_BATCHES)),
                                        justControlWords(
                                                "pages", 
                                                "webpages", 
                                                "webps").and(only(SHOW_ALL_WEBPAGES)),
                                        justControlWords(
                                                "dirs", 
                                                "directs", 
                                                "directories").and(only(SHOW_ALL_WEBDIRECTORIES))),
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
                                        "web").and(argumentsFor(FIND_WEBPAGE)),
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
                                        "program", 
                                        "prog").and(argumentsFor(FIND_PROGRAM)),                                
                                controlWords(
                                        "bat", 
                                        "batch", 
                                        "exe").and(argumentsFor(FIND_BATCH)),
                                justControlWords(independentWords(
                                        "webpanel",
                                        "wpanel",
                                        "panel")).and(only(SHOW_WEBPANEL)),
                                justControlWords(
                                        "bookmarks", 
                                        "bmarks", 
                                        "marks").and(only(SHOW_BOOKMARKS)),
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
        if ( this.controlWordsContext.isDependentControlWord(inputString) ) {
            debug("[INTERPRETER] " + inputString + " is dependent control word.");
            return incorrectCommand();
        }
        return this.decisionTree.assess(new Input(normalizeSpaces(inputString)));
    }
    
    public void install(Set<Plugin> plugins) {
        plugins.forEach(plugin -> this.plugins.install(plugin));
//        boolean pluginPrefixIsNotFree = 
//                this.interprete(plugin.prefix() + "testArgument").type().isDefined() ||
//                this.interprete(plugin.prefix() + "test argument").type().isNot(MULTICOMMAND) ||
//                this.interprete(plugin.prefix()).type().isDefined();
//        if ( pluginPrefixIsNotFree ) {
//            return false;
//        } else {
//            return this.plugins.install(plugin);
//        }        
    }
}
