/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.external.sysconsole.modules.workers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import diarsid.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import diarsid.beam.external.sysconsole.modules.ConsoleListenerModule;

/**
 *
 * @author Diarsid
 */
class ConsoleListener implements ConsoleListenerModule {
    
    private final ConsoleDispatcherModule dispatcher;
    
    ConsoleListener(ConsoleDispatcherModule dispatcher) {
        this.dispatcher = dispatcher;
    }
    
    @Override
    public void run() {
        String command;
        List<String> params = new ArrayList<>(); 
        input: while (true) {
            try {
                this.dispatcher.newLoop();
                command = this.dispatcher.waitForNewCommand();
                if (command.length() == 0){
                    continue input;
                }
                this.parseCommand(command, params);                
                parsing: switch (params.get(0)){ 
                    case "help" : {
                        this.dispatcher.printHelp();
                        break parsing;
                    }
                    case "close" :{
                        this.dispatcher.closeConsole();
                    }
                    case "+" :
                    case "new" : {
                        if (params.size() < 2){
                            break parsing;
                        }                        
                        switch (params.get(1)){
                            case "loc" :
                            case "location" : {
                                this.dispatcher.newLocation();
                                break parsing;
                            }
                            case "task" : {
                                this.dispatcher.newTask();
                                break parsing;
                            }
                            case "event" : {
                                this.dispatcher.newScheduledEvent();
                                break parsing;
                            }
                            case "rem" :
                            case "remind" :
                            case "reminder" : {
                                this.dispatcher.newReminder();
                                break parsing;
                            }                            
                            case "comm" :
                            case "command" : {
                                this.dispatcher.newCommand();
                                break parsing;
                            }
                            case "page" :
                            case "web" : {
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
                        if (params.size() > 1){
                            this.dispatcher.listLocation(params.get(1));
                        }
                        break parsing;
                    }
                    case "get" : {
                        if (params.size() < 2) {
                            break parsing;
                        }                        
                        switch (params.get(1)) {
                            case "comm" :
                            case "command" : {
                                this.dispatcher.getCommand();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                this.dispatcher.getLocation();
                                break parsing;
                            } 
                            case "web" :
                            case "page" : {
                                if (params.size() < 3){
                                    this.dispatcher.getPage();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
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
                                if (params.size() < 3){
                                    this.dispatcher.getAllWebPanelPages();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
                                        this.dispatcher.getPagesOfPanelDirectory();
                                        break parsing;
                                    }   
                                    case "dirs" : {
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
                                if (params.size() < 3){
                                    this.dispatcher.getAllBookmarksPages();
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "dir" :
                                    case "directory" : {
                                        this.dispatcher.getPagesOfBookmarksDirectory();
                                        break parsing;
                                    }  
                                    case "dirs" : {
                                        this.dispatcher.getAllBookmarkDirs();
                                        break parsing;
                                    }
                                    default: {
                                        break parsing;
                                    }
                                }                                
                            }
                            case "dirs" : {
                                this.dispatcher.getAllDirs();
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "alarm" : {
                        this.dispatcher.printAlarm();
                        break parsing;
                    }
                    case "r" :
                    case "run" : {
                        this.dispatcher.run(params); 
                        break parsing;
                    }
                    case "start" : {
                       this.dispatcher.start(params);
                        break parsing;
                    }
                    case "stop" : {
                        this.dispatcher.stop(params);
                        break parsing;
                    }
                    case "o" :
                    case "op" :    
                    case "open" : {
                        this.dispatcher.open(params);
                        break parsing;
                    }
                    case "exe" :
                    case "call" : {
                        this.dispatcher.call(params);
                        break parsing;
                    }
                    case "www" :
                    case "web" :
                    case "see" : {
                        this.dispatcher.openWebPage(params);
                        break parsing;
                    }
                    case "n" :
                    case "note" : {
                        if (params.size() < 2) {
                            this.dispatcher.openNotes();
                            break parsing;
                        } else {
                            this.dispatcher.openNote(params);
                            break parsing;
                        }                        
                    }                        
                    case "notes" : {                        
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
                                this.dispatcher.printActualTasks();                                
                                break parsing;
                            }
                            case "events" : {                                
                                this.dispatcher.printActualEvents();                                
                                break parsing;
                            }
                            case "rem" :
                            case "rems" :
                            case "reminders" : {
                                this.dispatcher.printActualReminders();
                                break parsing;
                            }
                            case "past" : {
                                this.dispatcher.printPastTasks();
                                break parsing;
                            }
                            case "loc" :
                            case "location" :
                            case "locations" : {
                                this.dispatcher.getLocations();
                                break parsing;
                            }
                            case "mem" :
                            case "choices" : {
                                this.dispatcher.getAllChoices();
                                break parsing;
                            }
                            case "comm" :
                            case "comms" :    
                            case "commands" : {
                                this.dispatcher.getCommands();
                                break parsing;
                            }
                            case "web" :
                            case "page" :
                            case "pages" : {
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
                        switch (params.get(1)){
                            case "page" : {
                                this.dispatcher.editPage();
                                break parsing;
                            } 
                            case "loc" :
                            case "location" : {
                                this.dispatcher.editLocation();
                                break parsing;
                            } 
                            case "comm" : 
                            case "command" : {
                                this.dispatcher.editCommand();
                                break parsing;
                            }
                            case "dir" :
                            case "directory" : {
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
                                this.dispatcher.deleteTask();
                                break parsing;
                            }
                            case "event" : {
                                this.dispatcher.deleteEvent();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                this.dispatcher.deleteLocation();
                                break parsing;
                            }
                            case "page" : {
                                this.dispatcher.deleteWebPage();
                                break parsing;
                            }
                            case "mem" :
                            case "memory" : {
                                this.dispatcher.deleteMem();
                                break parsing;
                            }                                
                            case "com" :
                            case "comm" :
                            case "command" : {
                                this.dispatcher.deleteCommand();
                                break parsing;
                            }
                            case "all" : {
                                if (params.size() < 3) {
                                    break parsing;
                                }
                                switch (params.get(2)) {
                                    case "tasks" : {
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
                                        this.dispatcher.removeAllPastTasks();
                                        break parsing;
                                    }
                                    case "future" : {
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
                        this.dispatcher.exitDialog();
                        break parsing;
                    }
                    case "clear" : {
                        if (params.size() < 2) {
                            break parsing;
                        }
                        switch (params.get(1)) {
                            case "tasks" : {
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
                                        if (params.size() < 4) {
                                            break parsing;
                                        }
                                        this.dispatcher.setAskUserToRememberHisChoice(params.get(3));
                                        break parsing;
                                    }
                                    case "active" : {
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
                                                this.dispatcher.useExternalShowTaskMethod();
                                                break parsing;
                                            }
                                            case "native" :
                                            case "core" : {
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
    
    private void parseCommand(String command, List<String> params) {
        params.clear();
        params.addAll(Arrays.asList(command.split("\\s+")));
    }
}
