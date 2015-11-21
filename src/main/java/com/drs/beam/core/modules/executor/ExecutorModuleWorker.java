/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.core.modules.executor;

import com.drs.beam.core.modules.ExecutorModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.modules.DataModule;
import com.drs.beam.core.modules.IoInnerModule;
import com.drs.beam.core.modules.data.DaoCommands;
import com.drs.beam.core.modules.data.DaoLocations;
import com.drs.beam.core.modules.data.DaoWebPages;

class ExecutorModuleWorker implements ExecutorModule{
    // Fields =============================================================================
    
    private final IoInnerModule ioEngine;
    private final OS system;
    private final DaoLocations locationsDao;
    private final DaoCommands commandsDao;
    private final DaoWebPages pagesDao;
    
    // Constructors =======================================================================
    ExecutorModuleWorker(IoInnerModule io, DataModule dataModule, OS os) {
        this.ioEngine = io;
        this.locationsDao = dataModule.getLocationsDao();
        this.commandsDao = dataModule.getCommandsDao();
        this.pagesDao = dataModule.getWebPagesDao();
        this.system = os;
    }

    // Methods ============================================================================
    
    @Override
    public void open(List<String> commandParams){
        try{
            if (commandParams.contains("in")){
                if (commandParams.contains("with")){
                    // command pattern: open [file] in [location] with [program]
                    this.openFileInLocationWithProgram(
                            commandParams.get(1), 
                            commandParams.get(3),
                            commandParams.get(5));
                } else {
                    // command pattern: open [file|folder] in [location]
                    this.openFileInLocation(
                            commandParams.get(1), 
                            commandParams.get(3));
                }
            } else {
                // command pattern: open [location]
                this.openLocation(commandParams.get(1));
            }
        } catch (IndexOutOfBoundsException indexException) {
            this.ioEngine.reportError("Unrecognizable command.");
        }
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
    public void call(List<String> commandNames){
        // command pattern: call [command_1] [command_2]...
        StoredExecutorCommand command;
        for(int i = 1; i < commandNames.size(); i++){
            command = this.getCommand(commandNames.get(i));
            if (command != null){
                this.executeCommand(command);
            }
        }   
    }
    
    @Override
    public void openWebPage(List<String> commandParams){
        if (commandParams.contains("with") || commandParams.contains("w")){
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
        Location location = this.getLocation(locationName);
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
       
    private void openLocation(String locationName){
        Location location = this.getLocation(locationName);
        if (location != null){
            this.system.openLocation(location);
        } 
    }
    
    private void openFileInLocation(String targetName, String locationName){
        targetName = targetName.trim().toLowerCase();
        Location location = this.getLocation(locationName);
        if (location != null){
            this.system.openFileInLocation(targetName, location);
        }             
    }
    
    private void openFileInLocationWithProgram(String file, String locationName, String program){
        file = file.trim().toLowerCase();
        program = program.trim().toLowerCase();
        Location location = this.getLocation(locationName);
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
    
    private Location getLocation(String locationName){
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
            return this.resolveMultipleLocations(foundLocations);
        }
    }
    
    private Location resolveMultipleLocations(List<Location> foundLocations){
        List<String> locationNames = new ArrayList();
        for (Location loc : foundLocations){
            locationNames.add(loc.getName());
        }
        int varNumber = this.ioEngine.resolveVariantsWithExternalIO(
                "There are several locations:", 
                locationNames);
        if (varNumber < 0){
            return null;
        } else {
            return foundLocations.get(varNumber-1);
        }
    }
    
    
    private StoredExecutorCommand getCommand(String name){        
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
            int variant = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several commands:", 
                    commandNames
            );
            
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
        for (int i = 1; i < commandParams.size(); i++){            
            page = this.getWebPage(commandParams.get(i));
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
        if (commandParams.size() > 3 && commandParams.get(2).contains("w")){
            WebPage page = this.getWebPage(commandParams.get(1));
            String browserName = commandParams.get(3);
            if (page != null){
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
    
    private WebPage getWebPage(String name){
        name = name.trim().toLowerCase();
        List<WebPage> pages;
        if (name.contains("-")){
            pages = this.pagesDao.getWebPagesByNameParts(name.split("-"));
        } else {
            pages = this.pagesDao.getWebPagesByName(name);
        }
        return resolveMultiplePages(pages);
    }
    
    private WebPage resolveMultiplePages(List<WebPage> pages){
        if (pages.size() == 1){
            return pages.get(0);
        } else if (pages.isEmpty()){
            this.ioEngine.reportMessage("Couldn`t find such page.");
            return null;
        } else {
            List<String> pageNames = new ArrayList<>();
            for (WebPage wp : pages){
                pageNames.add(wp.getName());
            }
            int choosedVariant = this.ioEngine.resolveVariantsWithExternalIO(
                    "There are several pages:", pageNames);
            
            if (choosedVariant < 0){
                return null;
            } else {
                return pages.get(choosedVariant-1);
            } 
        }
    }    
}