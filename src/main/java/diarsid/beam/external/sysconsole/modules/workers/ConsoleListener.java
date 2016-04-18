/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.external.sysconsole.modules.workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import diarsid.beam.external.sysconsole.modules.ConsoleListenerModule;

/**
 *
 * @author Diarsid
 */
class ConsoleListener implements ConsoleListenerModule {
    
    private final ConsoleDispatcherModule dispatcher;
    private final Set<String> commandsCache;
    private final List<String> params;
    
    private String command;
    private boolean commandWasExecuted;
    private int executeCount;
    
    ConsoleListener(ConsoleDispatcherModule dispatcher) {
        this.dispatcher = dispatcher;
        this.commandsCache = new HashSet<>();
        try {
            this.commandsCache.addAll(this.dispatcher.getCommandsFromCoreStorage());
        } catch (IOException e) {
            throw new WorkflowBrokenException(
                    "ConsoleListener creation: it is impossible to get " + 
                    "stored console commands from core through the RMI."); 
        }        
        this.params = new ArrayList<>();
    }
    
    private void commandAccepted() {
        this.commandWasExecuted = true;
    }
    
    @Override
    public void run() {
         
        input: while (true) {
            try {
                this.commandWasExecuted = false;
                this.executeCount = 0;
                this.dispatcher.newLoop();
                this.command = this.dispatcher.waitForNewCommand();
                if (this.command.length() == 0){
                    continue input;
                }
                do {    
                this.parseCommand(this.command, params);                
                parsing: switch (params.get(0)) { 
                    case "help" : {
                        this.commandAccepted();
                        this.dispatcher.printHelp();
                        break parsing;
                    }
                    case "close" : {
                        this.commandAccepted();
                        this.dispatcher.dumpCommandsIntoCore(this.commandsCache);
                        this.dispatcher.closeConsole();
                    }
                    case "+" :
                    case "new" : {
                        if (this.params.size() < 2) {
                            break parsing;
                        }                        
                        switch (this.params.get(1)) {
                            case "loc" :
                            case "location" : {
                                this.commandAccepted();
                                this.dispatcher.newLocation();
                                break parsing;
                            }
                            case "task" : {
                                this.commandAccepted();
                                this.dispatcher.newTask();
                                break parsing;
                            }
                            case "event" : {
                                this.commandAccepted();
                                this.dispatcher.newScheduledEvent();
                                break parsing;
                            }
                            case "rem" :
                            case "remind" :
                            case "reminder" : {
                                this.commandAccepted();
                                this.dispatcher.newReminder();
                                break parsing;
                            }                            
                            case "comm" :
                            case "command" : {
                                this.commandAccepted();
                                this.dispatcher.newCommand();
                                break parsing;
                            }
                            case "page" :
                            case "web" : {
                                this.commandAccepted();
                                this.dispatcher.newWebPage();
                                break parsing;
                            }
                            /*
                            case "n" :
                            case "note" : {
                                this.dispatcher.newNote(params);
                                break parsing;                               
                            }
                            */
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "li" :
                    case "list" : {
                        if (this.params.size() > 1) {
                            this.commandAccepted();
                            this.dispatcher.listLocation(this.params.get(1));
                        }
                        break parsing;
                    }
                    case "get" : {
                        if (this.params.size() < 2) {
                            break parsing;
                        }                        
                        switch (this.params.get(1)) {
                            case "comm" :
                            case "command" : {
                                this.commandAccepted();
                                this.dispatcher.getCommand();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                this.commandAccepted();
                                this.dispatcher.getLocation();
                                break parsing;
                            } 
                            case "web" :
                            case "page" : {
                                if (params.size() < 3){
                                    this.commandAccepted();
                                    this.dispatcher.getPage();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
                                        this.commandAccepted();
                                        this.dispatcher.getPagesInDirectoryAndPlacement();
                                        break parsing;
                                    }    
                                    default: {
                                        break parsing;
                                    }
                                }
                            }
                            case "webpanel" :
                            case "panel" : {
                                if (params.size() < 3) {
                                    this.commandAccepted();
                                    this.dispatcher.getAllWebPanelPages();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
                                        this.commandAccepted();
                                        this.dispatcher.getPagesOfPanelDirectory();
                                        break parsing;
                                    }   
                                    case "dirs" : {
                                        this.commandAccepted();
                                        this.dispatcher.getAllWebPanelDirs();
                                        break parsing;
                                    }
                                    default: {
                                        break parsing;
                                    }
                                }                                
                            }
                            case "bookm" :
                            case "bmarks" :
                            case "marks" :
                            case "bookmarks" : {
                                if (params.size() < 3) {
                                    this.commandAccepted();
                                    this.dispatcher.getAllBookmarksPages();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
                                        this.commandAccepted();
                                        this.dispatcher.getPagesOfBookmarksDirectory();
                                        break parsing;
                                    }  
                                    case "dirs" : {
                                        this.commandAccepted();
                                        this.dispatcher.getAllBookmarkDirs();
                                        break parsing;
                                    }
                                    default: {
                                        break parsing;
                                    }
                                }                                
                            }
                            case "dirs" : {
                                this.commandAccepted();
                                this.dispatcher.getAllDirs();
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "alarm" : {
                        this.commandAccepted();
                        this.dispatcher.printAlarm();
                        break parsing;
                    }
                    case "r" :
                    case "run" : {
                        this.commandAccepted();
                        this.dispatcher.run(params); 
                        break parsing;
                    }
                    case "start" : {
                        this.commandAccepted();
                        this.dispatcher.start(params);
                        break parsing;
                    }
                    case "stop" : {
                        this.commandAccepted();
                        this.dispatcher.stop(params);
                        break parsing;
                    }
                    case "o" :
                    case "op" :    
                    case "open" : {
                        this.commandAccepted();
                        this.dispatcher.open(params);
                        break parsing;
                    }
                    case "exe" :
                    case "call" : {
                        this.commandAccepted();
                        this.dispatcher.call(params);
                        break parsing;
                    }
                    case "www" :
                    case "web" :
                    case "see" : {
                        this.commandAccepted();
                        this.dispatcher.openWebPage(params);
                        break parsing;
                    }
                    case "n" :
                    case "note" : {                        
                        if (params.size() < 2) {
                            this.commandAccepted();
                            this.dispatcher.openNotes();
                            break parsing;
                        } else {
                            this.commandAccepted();
                            this.dispatcher.openNote(params);
                            break parsing;
                        }                        
                    }                        
                    case "notes" : {       
                        this.commandAccepted();
                        this.dispatcher.openNotes();
                        break parsing;
                    }                    
                    case "all" :
                    case "view" : {
                        if (params.size() < 2) {
                            break parsing;
                        }                        
                        switch (params.get(1)) {
                            case "tasks" :
                            case "future" : {         
                                this.commandAccepted();
                                this.dispatcher.printActualTasks();                                
                                break parsing;
                            }
                            case "events" : {               
                                this.commandAccepted();
                                this.dispatcher.printActualEvents();                                
                                break parsing;
                            }
                            case "rem" :
                            case "rems" :
                            case "reminders" : {
                                this.commandAccepted();
                                this.dispatcher.printActualReminders();
                                break parsing;
                            }
                            case "past" : {
                                this.commandAccepted();
                                this.dispatcher.printPastTasks();
                                break parsing;
                            }
                            case "loc" :
                            case "location" :
                            case "locations" : {
                                this.commandAccepted();
                                this.dispatcher.getLocations();
                                break parsing;
                            }
                            case "mem" :
                            case "choices" : {
                                this.commandAccepted();
                                this.dispatcher.getAllChoices();
                                break parsing;
                            }
                            case "comm" :
                            case "comms" :    
                            case "commands" : {
                                this.commandAccepted();
                                this.dispatcher.getCommands();
                                break parsing;
                            }
                            case "web" :
                            case "page" :
                            case "pages" : {
                                this.commandAccepted();
                                this.dispatcher.getAllWebPages();
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }                        
                    }  
                    case "ed" :
                    case "edit" :
                    case "change" : {
                        if (params.size() < 2) {
                            break parsing;
                        }
                        switch (params.get(1)) {
                            case "page" : {
                                this.commandAccepted();
                                this.dispatcher.editPage();
                                break parsing;
                            } 
                            case "loc" :
                            case "location" : {
                                this.commandAccepted();
                                this.dispatcher.editLocation();
                                break parsing;
                            } 
                            case "comm" : 
                            case "command" : {
                                this.commandAccepted();
                                this.dispatcher.editCommand();
                                break parsing;
                            }
                            case "dir" :
                            case "directory" : {
                                this.commandAccepted();
                                this.dispatcher.editDirectory();
                                break parsing;
                            }    
                            default : {
                                break parsing;
                            }
                        }
                    } 
                    case "move" : {
                        if (params.size() < 2) {
                            break parsing;
                        }
                        switch (params.get(1)){
                            case "page" : {
                                this.commandAccepted();
                                this.dispatcher.movePageToDirectoryAndPlacement();
                                break parsing;
                            } 
                            case "dir" :
                            case "directory" : {
                                //this.dispatcher.editDirectory();
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "delete" :
                    case "del" : {
                        if (params.size() < 2){
                            break parsing;
                        }                        
                        switch (params.get(1)){
                            case "task" : {
                                this.commandAccepted();
                                this.dispatcher.deleteTask();
                                break parsing;
                            }
                            case "event" : {
                                this.commandAccepted();
                                this.dispatcher.deleteEvent();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                this.commandAccepted();
                                this.dispatcher.deleteLocation();
                                break parsing;
                            }
                            case "page" : {
                                this.commandAccepted();
                                this.dispatcher.deleteWebPage();
                                break parsing;
                            }
                            case "dir" :
                            case "directory" : {
                                this.commandAccepted();
                                this.dispatcher.deleteDirectory();
                                break parsing;
                            }    
                            case "mem" :
                            case "memory" : {
                                this.commandAccepted();
                                this.dispatcher.deleteMem();
                                break parsing;
                            }                                
                            case "com" :
                            case "comm" :
                            case "command" : {
                                this.commandAccepted();
                                this.dispatcher.deleteCommand();
                                break parsing;
                            }
                            case "all" : {
                                if (params.size() < 3) {
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "tasks" : {
                                        this.commandAccepted();
                                        boolean confirm = this.dispatcher.confirmAction("Delete all tasks?");
                                        if(confirm) {
                                            confirm = this.dispatcher.confirmAction("Really?");
                                            if (confirm) {
                                                this.dispatcher.removeAllTasks();
                                            }                                            
                                        }
                                        break parsing;
                                    }
                                    case "past" : {
                                        this.commandAccepted();
                                        this.dispatcher.removeAllPastTasks();
                                        break parsing;
                                    }
                                    case "future" : {
                                        this.commandAccepted();
                                        boolean confirm = this.dispatcher.confirmAction("Delete all future tasks?");
                                        if (confirm){
                                            this.dispatcher.removeAllFutureTasks();
                                        }
                                        break parsing;
                                    }
                                    case "events" : {
                                        //deleteAllEvents();
                                        break parsing;
                                    }
                                    default : {
                                        break parsing;
                                    }
                                }
                            }
                            default : {
                                break parsing;
                            }
                        }                        
                    }
                    case "exit" : {
                        this.commandAccepted();
                        this.dispatcher.dumpCommandsIntoCore(this.commandsCache);
                        this.dispatcher.exitDialog();
                        break parsing;
                    }
                    case "clear" : {
                        if (params.size() < 2) {
                            break parsing;
                        }
                        switch (params.get(1)) {
                            case "tasks" : {
                                this.commandAccepted();
                                this.dispatcher.removeAllPastTasks();
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "set" : {
                        if (params.size() < 2) {
                            break parsing;
                        }
                        switch (params.get(1)) {
                            case "intell" :
                            case "intel" : {
                                if (params.size() < 3) {
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "ask" : {
                                        this.commandAccepted();
                                        if (params.size() < 4) {
                                            break parsing;
                                        }
                                        this.dispatcher.setAskUserToRememberHisChoice(params.get(3));
                                        break parsing;
                                    }
                                    case "active" : {
                                        this.commandAccepted();
                                        if (params.size() < 4) {
                                            break parsing;
                                        }
                                        this.dispatcher.setIntelligentActive(params.get(3));
                                        break parsing;
                                    }
                                    default: {
                                        break parsing;
                                    }
                                }
                            }
                            case "task" : {
                                if (params.size() < 3) {
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "use" : {
                                        if (params.size() < 4) {
                                            break parsing;
                                        }
                                        switch (params.get(3)) {
                                            case "external" :
                                            case "cons" :
                                            case "console" :
                                            case "ext" : {
                                                this.commandAccepted();
                                                this.dispatcher.useExternalShowTaskMethod();
                                                break parsing;
                                            }
                                            case "native" :
                                            case "core" : {
                                                this.commandAccepted();
                                                this.dispatcher.useNativeShowTaskMethod();
                                                break parsing;
                                            }
                                            default: {
                                                break parsing;
                                            }
                                        }   
                                    }
                                    default : {
                                        break parsing;
                                    }
                                }                                
                            }
                            default : {
                                break parsing;
                            }
                        } 
                    }
                    default : {
                        break parsing;
                    }
                }
                
                // if command has been recognized, save it in chache
                if ( this.commandWasExecuted && 
                        this.isCommandNecessary(this.command)) {                    
                    this.commandsCache.add(this.command);
                } else {
                    // else try to find command from cache that looks 
                    // like given one
                    this.command = this.intellSearchFromCache(this.command);
                    // if such command has not been found, do not proceed
                    if (this.command.isEmpty()) {
                        // algorithm will proceed if (executeCount < 2) that 
                        // actually means it can be executed only one more 
                        // time after first fail to recognize original given 
                        // command
                        this.executeCount = 10;
                    } else {
                        // if such command has been found, increment counter
                        // and try to execute it
                        this.executeCount++;
                    }
                }
                
                } while ( (! commandWasExecuted) && (executeCount < 2) );
                
                command = "";
                params.clear();
            } catch (IOException e) {
                System.out.println("Exception:");
                System.out.println(e.getMessage());
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }    
        }
    }
    
    private String intellSearchFromCache(String unknownCommand) 
            throws IOException {
        
        if (unknownCommand.length() < 2) {
            // There is no reason to search commands that matches only
            // one letter
            return "";
        }
        List<String> choosedCommands = new ArrayList<>();
        String choosedPreviousCommand = "";
        
        // character '-' separates different parts in command argument's 
        // body. All these parts must be presented in searched target.
        // For instance, 'jav-ol' could mean 'java_folder' but neither only 
        // 'java' nor 'java projects'.
        if (unknownCommand.contains("-")) {
            // loop to iterate through the cache 
            searchInCommandsCache:
            for (String previousCommand : this.commandsCache) {
                // loop to evaluate every command in cache if it contains
                // all required parts
                for (String requiredPart : unknownCommand.split("-")) {
                    if ( ! previousCommand.contains(requiredPart) ) {
                        // if currently evaluated command from cache does 
                        // not contain this required part, begin next 
                        // iteration with next command from cache
                        continue searchInCommandsCache;
                    }
                }
                
                // initially set first suitable command
                // that have been encountered
                if ( choosedPreviousCommand.isEmpty() ) {
                    choosedPreviousCommand = previousCommand;
                } else {
                    // trying to chose the shortest command from cache
                    // if there are more than one suitable command with
                    // the same action type
                    if ( this.ifCommandActionsAreEqual(
                            previousCommand, choosedPreviousCommand) ) {
                        // chose the shortest command among currently 
                        // evaluated command from cache and suitable
                        // command that has been choosed from cache during 
                        // previous iterations
                        if ( previousCommand.length() < choosedPreviousCommand.length() ) {
                            choosedPreviousCommand = previousCommand;
                        }
                    } else {
                        // if there are more than one suitable command in the 
                        // old commands cache but they have different action
                        // types, collect them in order to ask user to chose 
                        // one that suits for him
                        choosedCommands.add(previousCommand);
                    }                    
                }
            }
        } else {
            // if command consists only from one part to be guessed
            for (String previousCommand : this.commandsCache) {            
                // chose from old commands cache only commands that
                // contain new command entry
                if ( previousCommand.contains(unknownCommand) ) {  
                    // initially set first suitable command
                    // that have been encountered
                    if ( choosedPreviousCommand.isEmpty() ) {
                        choosedPreviousCommand = previousCommand;
                    } else {
                        // trying to chose the shortest command from cache
                        // if there are more than one suitable command with
                        // the same action type
                        if ( this.ifCommandActionsAreEqual(
                                previousCommand, choosedPreviousCommand) ) {
                            // chose the shortest command among two
                            if ( previousCommand.length() < choosedPreviousCommand.length() ) {
                                choosedPreviousCommand = previousCommand;
                            }
                        } else {
                            // if there are more than one suitable command in the 
                            // old commands cache but they have different action
                            // types, collect them in order to ask user to chose 
                            // one that suits for him
                            choosedCommands.add(previousCommand);
                        }                    
                    }                
                }
            }    
        }        
        
        if ( ! choosedCommands.isEmpty() ) {
            choosedCommands.add(choosedPreviousCommand);
            int choosed = this.dispatcher.chooseVariants("Action?", choosedCommands);
            if ( choosed > 0 ) {
                return choosedCommands.get(choosed - 1);
            } else {
                return "";
            }            
        } else {
            if ( this.ifSeeCommand(choosedPreviousCommand) ) {
                return "see " + unknownCommand;
            } else if ( this.ifRunCommand(choosedPreviousCommand) ) {
                return "run " + unknownCommand;
            } else if ( this.ifCallCommand(choosedPreviousCommand) ) {
                return "call " + unknownCommand;
            } else {
                return choosedPreviousCommand;
            }
        }
    }
    
    private boolean isCommandNecessary(String searchedCommand) {
        if ( searchedCommand.startsWith("see") ||
                searchedCommand.startsWith("www") ||
                searchedCommand.startsWith("web") ||
                searchedCommand.startsWith("o") ||
                searchedCommand.startsWith("r") ||
                searchedCommand.startsWith("call") ||
                searchedCommand.startsWith("exe") ||
                searchedCommand.startsWith("start") ) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean ifCommandActionsAreEqual(String previous, String choosed) {
        return ( previous.charAt(0) == choosed.charAt(0) );
    }
    
    private boolean ifRunCommand(String previousCommand) {
        return 
                previousCommand.startsWith("r") || 
                previousCommand.startsWith("run");
    }
    
    private boolean ifCallCommand(String previousCommand) {
        return 
                previousCommand.startsWith("call") ||
                previousCommand.startsWith("exe");
    }
    
    private boolean ifSeeCommand(String previousCommand) {
        return 
                previousCommand.startsWith("see") ||
                previousCommand.startsWith("www") ||
                previousCommand.startsWith("web");
    }
    
    private void parseCommand(String command, List<String> params) {
        this.params.clear();
        this.params.addAll(Arrays.asList(command.split("\\s+")));
    }
}
