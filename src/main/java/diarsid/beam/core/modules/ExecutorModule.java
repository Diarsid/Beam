/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules;

import java.util.List;

import diarsid.beam.core.StoppableBeamModule;
import diarsid.beam.core.modules.executor.entities.StoredExecutorCommand;

/**
 * Executes remote commands such as open particular directory in the local
 * file system, run certain program, open web site or run a bunch of 
 * such commands.
 * 
 * @author Diarsid
 */
public interface ExecutorModule extends StoppableBeamModule {
    
    // Basic executor functionality. Provides methods representing
    // concrete actions - execut program, open web page, file, folder
    // or call a stored command.
    void open(List<String> commandParams);
    
    void run(List<String> commandParams);
    
    void call(List<String> commandParams);
    
    void start(List<String> commandParams);
    
    void stop(List<String> commandParams);
    
    void openWebPage(List<String> commandParams);
    
    void newCommand(List<String> command, String commandName);
    
    List<String> listLocationContent(String locationName);
    
    List<StoredExecutorCommand> getAllCommands();
    
    List<StoredExecutorCommand> getCommands(String commandName);
    
    boolean deleteCommand(String commandName);
    
    void setIntelligentActive(boolean isActive);
    
    boolean deleteMem(String command);  
    
    void rememberChoiceAutomatically(boolean auto);
    
    List<String> getAllChoices();
    
    //void newNote(List<String> commandParams);
    
    void openNotes();
    
    void openNote(List<String> commandParams);
    
    void executeIfExists(List<String> commandParams);
}
