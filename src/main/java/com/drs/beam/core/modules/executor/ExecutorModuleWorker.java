/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.executor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.ExecutorModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoCommands;
import com.drs.beam.core.modules.data.DaoLocations;
import com.drs.beam.core.modules.data.DaoWebPages;

class ExecutorModuleWorker implements ExecutorModule {
    
    private final IoInnerModule ioEngine;
    private final IntelligentResolver intell;
    private final OS system;
    private final DaoLocations locationsDao;
    private final DaoCommands commandsDao;
    private final DaoWebPages pagesDao;        
    private final List<String> command;
    private final Location notes;
    
    ExecutorModuleWorker(
            IoInnerModule io, 
            DataModule dataModule, 
            IntelligentResolver i, 
            OS os,
            Location notes) {
        this.ioEngine = io;
        this.intell = i;
        this.locationsDao = dataModule.getLocationsDao();
        this.commandsDao = dataModule.getCommandsDao();
        this.pagesDao = dataModule.getWebPagesDao();
        this.system = os;
        this.command = new ArrayList<>();
        this.notes = notes;
    }
    
    @Override
    public void open(List<String> commandParams) {
        command.addAll(commandParams);
        try {
            if (commandParams.contains("in")) {
                if (commandParams.contains("with")) {
                    // command pattern: open [file] in [location] with [program]
                    this.openFileInLocationWithProgram(
                            commandParams.get(1), 
                            commandParams.get(3),
                            commandParams.get(5),
                            String.join(" ", commandParams));
                } else {
                    // command pattern: open [file|folder] in [location]
                    this.openFileInLocation(
                            commandParams.get(1), 
                            commandParams.get(3),
                            String.join(" ", commandParams));
                }
            } else {
                // command pattern: open [location]
                this.openLocation(commandParams.get(1), 
                        String.join(" ", commandParams));
            }
        } catch (IndexOutOfBoundsException indexException) {
            this.ioEngine.reportError("Unrecognizable command.");
        }
        command.clear();
    }
    
    @Override
    public void run(List<String> commandParams){
        // command pattern: run [program_1] [program_2] [program_3]...
        for(int i = 1; i < commandParams.size(); i++){
            this.system.runProgram(commandParams.get(i));
        }
    }
    
    @Override
    public void start(List<String> commandParams){
        // command pattern: start [program]
        this.runMarkedProgram("start", commandParams);       
    }
    
    @Override
    public void stop(List<String> commandParams){
        // command pattern: stop [program]
        this.runMarkedProgram("stop", commandParams);        
    }
    
    @Override
    public void call(List<String> commandParams) {
        // command pattern: call [command_1] [command_2]...
        StoredExecutorCommand storedCommand;
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i < commandParams.size(); i++) {
            sb.append(commandParams.get(0)).append(" ").append(commandParams.get(i));
            storedCommand = this.getCommand(commandParams.get(i), sb.toString());
            if (storedCommand != null) {
                this.executeCommand(storedCommand);
            }
        }   
    }
    
    @Override
    public void openWebPage(List<String> commandParams){
        if (commandParams.contains("with") || 
                commandParams.contains("w") || 
                commandParams.contains("in")) {
            this.openWebPageWithGivenBrowser(commandParams);
        } else {
            this.openWebPages(commandParams);
        }
    }
    
    @Override
    public void newCommand(List<String> commands, String commandName){
        for(int i = 0; i < commands.size(); i++){
            String s = commands.get(i).trim().toLowerCase();
            if (s.equals("call") || s.equals("exe")){
                this.ioEngine.reportMessage(
                        "'call' and 'exe' is not permitted to use.",
                        "It can cause cyclical execution.");
                return;
            }
            commands.set(i, s);
        }
        commandName = commandName.trim().toLowerCase();
        this.commandsDao.saveNewCommand(new StoredExecutorCommand(commandName, commands));
    }    
        
    @Override
    public boolean deleteCommand(String commandName){
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.removeCommand(commandName);
    }    
    
    @Override
    public List<StoredExecutorCommand> getAllCommands(){
        return this.commandsDao.getAllCommands();
    }
    
    @Override
    public boolean checkPath(String path) {
        return this.system.checkIfDirectoryExists(path);
    }
    
    @Override
    public List<String> listLocationContent(String locationName){
        Location location = this.getLocation(locationName, "list "+locationName);
        if (location != null){
            List<String> locationContent = this.system.getLocationContent(location);
            if (locationContent != null){
                locationContent.add(0, location.getName());
                return locationContent;
            } else {                
                return new ArrayList<>();
            }
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<StoredExecutorCommand> getCommands(String commandName){
        commandName = commandName.trim().toLowerCase();
        return this.commandsDao.getCommandsByName(commandName);
    }
       
    private void openLocation(String locationName, String command){
        Location location = this.getLocation(locationName, command);
        if (location != null){
            this.system.openLocation(location);
        } 
    }
    
    private void openFileInLocation(
            String targetName, String locationName, String command){
        targetName = targetName.trim().toLowerCase();
        Location location = this.getLocation(locationName, command);
        if (location != null){
            this.system.openFileInLocation(targetName, location);
        }             
    }
    
    private void openFileInLocationWithProgram(
            String file, String locationName, String program, String command){
        file = file.trim().toLowerCase();
        program = program.trim().toLowerCase();
        Location location = this.getLocation(locationName, command);
        if (location != null){
            this.system.openFileInLocationWithProgram(file, location, program);
        }    
    }
    
    private void executeCommand(StoredExecutorCommand command){
        List<String> commandParams;
        for(String commandString : command.getCommands()){
            commandParams = Arrays.asList(commandString.split("\\s+"));
            switch(commandParams.get(0)){
                case "open" :
                case "op" :
                case "o" : {
                    this.open(commandParams);
                    break;
                } 
                case "r" :
                case "run" : {
                    this.run(commandParams);
                    break;
                }
                case "call" : {
                    this.call(commandParams);
                    break;
                }
                case "start" : {
                    this.start(commandParams);
                    break;
                }
                case "stop" : {
                    this.stop(commandParams);
                    break;
                }
                case "see" :
                case "www" : {
                    this.openWebPage(commandParams);
                    break;
                }
                case "pause" : {
                    this.pauseCommandExecution(commandParams);
                    break;
                }
                default : {
                    this.ioEngine.reportError("Unrecognizible command.");
                }
            }
        }
    }
        
    private void runMarkedProgram(String mark, List<String> commandParams){
        if (commandParams.size() == 2){
            this.system.runProgram(commandParams.get(1)+"-"+mark);
        } else {
            this.ioEngine.reportMessage("Unrecognizable command.");
        }
    }
    
    private Location getLocation(String locationName, String command){
        locationName = locationName.trim().toLowerCase();
        List<Location> foundLocations;
        
        if (locationName.contains("-")){
            foundLocations = this.locationsDao.getLocationsByNameParts(locationName.split("-"));            
        } else {
            foundLocations = this.locationsDao.getLocationsByName(locationName);            
        }
        
        if (foundLocations.size() < 1){
            this.ioEngine.reportMessage("Couldn`t find such location.");
            return null;
        } else if (foundLocations.size() == 1){
            return foundLocations.get(0);
        } else { 
            return this.resolveMultipleLocations(foundLocations, command);
        }
    }
    
    private Location resolveMultipleLocations(
            List<Location> foundLocations, String command) {
        
        List<String> locationNames = new ArrayList();
        for (Location loc : foundLocations){
            locationNames.add(loc.getName());
        }
        int varNumber = this.intell.resolve(
                "There are several locations:", 
                command, 
                locationNames);
        //int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
        //        "There are several locations:", 
        //        locationNames);
        if (varNumber < 0){
            return null;
        } else {
            return foundLocations.get(varNumber-1);
        }
    }
    
    
    private StoredExecutorCommand getCommand(String name, String command){        
        name = name.trim().toLowerCase();
        List<StoredExecutorCommand> commands = this.commandsDao.getCommandsByName(name);    
        
        if (commands.size() < 1){
            this.ioEngine.reportMessage("Couldn`t find such command.");
            return null;
        } else if (commands.size() == 1){
            return commands.get(0);
        } else {
            List<String> commandNames = new ArrayList<>();
            for (StoredExecutorCommand c : commands){
                commandNames.add(c.getName());
            }
            int variant = this.intell.resolve(
                    "There are several commands:", 
                    command, 
                    commandNames);
            //int variant = this.ioEngine.resolveVariantsWithExternalIO(
            //        "There are several commands:", 
            //        commandNames
            //);
            
            if (variant < 0){
                return null;
            } else {
                return commands.get(variant-1);
            }            
        }
    }
    
    private void openWebPages(List<String> commandParams){
        // command pattern: see [webPage_1] [webPage_2]...
        WebPage page;
        StringBuilder commandBuilder = new StringBuilder();
        for (int i = 1; i < commandParams.size(); i++) {
            commandBuilder.append(commandParams.get(0)).append(" ").append(commandParams.get(i));
            page = this.getWebPage(commandParams.get(i), commandBuilder.toString());
            if (page != null){
                if (page.useDefaultBrowser()){
                    this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
                } else {
                    this.system.openUrlWithGivenBrowser(page.getUrlAddress(), page.getBrowser());
                }
            }
        }
    }
    
    private void openWebPageWithGivenBrowser(List<String> commandParams){
        // command pattern: see [webPage] with|w [browserName]
        if (commandParams.size() > 3 && 
                (commandParams.get(2).contains("w") || 
                commandParams.get(2).contains("in") )) {
            WebPage page = this.getWebPage(
                    commandParams.get(1),
                    String.join(" ", commandParams.subList(0, 2)));
            String browserName = commandParams.get(3);
            if (page != null) {
                if (browserName.equals("default") || browserName.equals("def")){
                    this.system.openUrlWithDefaultBrowser(page.getUrlAddress());
                    this.pagesDao.editWebPageBrowser(page.getName(), "default");
                } else {
                    this.system.openUrlWithGivenBrowser(page.getUrlAddress(), browserName);
                    String[] vars = {"yes", "no"};
                    int choosed = this.ioEngine.resolveVariantsWithExternalIO(
                            "Use given browser always for this page?", 
                            Arrays.asList(vars));
                    if (choosed == 1){
                        if (this.pagesDao.editWebPageBrowser(page.getName(), browserName)){
                            this.ioEngine.reportMessage("Get it.");
                        }                   
                    }
                }                
            }
        } else {
            this.ioEngine.reportMessage("Unrecognizale command.");
        }
    }
    
    private WebPage getWebPage(String name, String command){
        name = name.trim().toLowerCase();
        List<WebPage> pages;
        if (name.contains("-")) {
            pages = this.pagesDao.getWebPagesByNameParts(name.split("-"));
        } else {
            pages = this.pagesDao.getWebPagesByName(name);
        }
        return resolveMultiplePages(pages, command);
    }
    
    private WebPage resolveMultiplePages(List<WebPage> pages, String command) {
        if (pages.size() == 1) {
            return pages.get(0);
        } else if (pages.isEmpty()) {
            this.ioEngine.reportMessage("Couldn`t find such page.");
            return null;
        } else {
            List<String> pageNames = new ArrayList<>();
            for (WebPage wp : pages) {
                pageNames.add(wp.getName());
            }
            int choosedVariant = this.intell.resolve(
                    "There are several pages:", 
                    command, 
                    pageNames);
            //int choosedVariant = this.ioEngine.resolveVariantsWithExternalIO(
            //        "There are several pages:", pageNames);
            
            if (choosedVariant < 0) {
                return null;
            } else {
                return pages.get(choosedVariant-1);
            } 
        }
    }    
    
    private void pauseCommandExecution(List<String> commandParams) {
        // default value for pause. It will be used in no other value has been 
        // specified explicitly.
        int pause = 5000;
        try {
            // if after 'pause' there are an argument 
            // that contains numbers 0-9 only
            if ( (commandParams.size() > 1) && (commandParams.get(1).matches("\\d+")) ) {
                pause = Integer.parseInt(commandParams.get(1));
            }
            this.ioEngine.reportMessage("pause " + (pause/1000) + " sec...");
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            // nothing to do with it.
        }
    }
    
    @Override
    public void setIntelligentActive(boolean isActive) {
        this.intell.setActive(isActive);
    }  
    
    @Override
    public boolean deleteMem(String command) {
        return this.intell.deleteMem(command);
    }     
    
    @Override
    public void setAskUserToRememberHisChoice(boolean askUser) {
        this.intell.setAskUserToRememberHisChoice(askUser);
    }  
    
    @Override
    public Map<String, String> getAllChoices() {
        return this.intell.getAllChoices();
    }
    
    @Override
    public void newNote(List<String> commandParams) {
        if (commandParams.size() > 2) {
            String name = String.join(" ", commandParams.subList(2, commandParams.size()));
            this.system.createAndOpenTxtFileIn(name, this.notes);
        } else {
            this.system.createAndOpenTxtFileIn("", this.notes);
        }
    }
    
    @Override
    public void openNotes() {
        this.system.openLocation(this.notes);
    }
    
    @Override
    public void openNote(List<String> commandParams) {
        for (int i = 1; i < commandParams.size(); i++) {
            this.system.openFileInLocation(commandParams.get(i), this.notes);
        }
    }
    
}