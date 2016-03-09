/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.external.sysconsole.modules;

import java.io.IOException;
import java.util.List;

import diarsid.beam.external.ExternalIOInterface;

import com.drs.gem.injector.module.GemModule;

/**
 *
 * @author Diarsid
 */
public interface ConsoleDispatcherModule extends GemModule, ExternalIOInterface {

    void deleteCommand() throws IOException;

    void deleteEvent() throws IOException;

    void deleteLocation() throws IOException;

    void deleteMem() throws IOException;

    void deleteTask() throws IOException;

    void deleteWebPage(String name) throws IOException;

    void deleteWebPage() throws IOException;

    void editCommand() throws IOException;

    void editLocation() throws IOException;

    void editPage() throws IOException;
    
    void movePageToDirectoryAndPlacement() throws IOException;

    void getAllChoices() throws IOException;

    void getCommand() throws IOException;

    void getCommands() throws IOException;

    void getLocation() throws IOException;

    void getLocations() throws IOException;

    void getPage() throws IOException;

    void getPagesInDirectoryAndPlacement() throws IOException;
    
    void getPagesOfPanelDirectory() throws IOException;
    
    void getPagesOfBookmarksDirectory() throws IOException;

    void listLocation(String locationName) throws IOException;

    void newCommand() throws IOException;    
    
    void newLocation() throws IOException;

    void newLoop() throws IOException;

    void newTask() throws IOException;
    
    void newReminder() throws IOException;

    void newScheduledEvent() throws IOException;

    void newWebPage() throws IOException;

    void printHelp() throws IOException;

    void editDirectory() throws IOException;
    
    void getAllWebPages() throws IOException;

    void getAllWebPanelPages() throws IOException;
    
    void getAllBookmarksPages() throws IOException;

    String waitForNewCommand() throws IOException;
    
    void closeConsole() throws IOException;
    
    void call(List<String> command) throws IOException; 
    
    void start(List<String> command) throws IOException; 
    
    void stop(List<String> command) throws IOException; 
    
    void run(List<String> command) throws IOException; 
    
    void open(List<String> command) throws IOException; 
    
    void openWebPage(List<String> command) throws IOException; 
    
    void printAlarm() throws IOException; 
    
    void printPastTasks() throws IOException; 
    
    void printActualTasks() throws IOException; 
    
    void printActualEvents() throws IOException; 
    
    void printActualReminders() throws IOException; 
    
    boolean confirmAction(String question) throws IOException; 
    
    void removeAllTasks() throws IOException; 
    
    void removeAllPastTasks() throws IOException; 
    
    void removeAllFutureTasks() throws IOException; 
    
    void exitDialog() throws IOException; 
    
    void useNativeShowTaskMethod() throws IOException; 
    
    void useExternalShowTaskMethod() throws IOException; 
    
    void setAskUserToRememberHisChoice(String yesOrNo) throws IOException; 
    
    void setIntelligentActive(String yesOrNo) throws IOException;    
    
    void openNotes() throws IOException;
    
    void openNote(List<String> command) throws IOException;
    
    void newNote(List<String> command) throws IOException;
}
