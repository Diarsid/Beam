/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.drs.beam.external.sysconsole.modules.workers;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.drs.beam.core.entities.Location;
import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.entities.WebPagePlacement;
import com.drs.beam.core.modules.executor.StoredExecutorCommand;
import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.external.sysconsole.modules.BeamCoreAccessModule;
import com.drs.beam.external.sysconsole.modules.ConsoleDispatcherModule;
import com.drs.beam.external.sysconsole.modules.ConsolePrinterModule;
import com.drs.beam.external.sysconsole.modules.ConsoleReaderModule;

/**
 *
 * @author Diarsid
 */
class ConsoleDispatcher implements ConsoleDispatcherModule {
    
    private final BeamCoreAccessModule beam;
    private final ConsolePrinterModule printer;
    private final ConsoleReaderModule reader;
    private final InputHandler input;
    private final List<String> placements;
    private final List<String> pageEditVars;
    
    private final Object readerLock;
    
    ConsoleDispatcher(
            BeamCoreAccessModule be,
            ConsolePrinterModule pr,
            ConsoleReaderModule re,
            InputHandler ih) {
        
        this.printer = pr;
        this.reader = re;
        this.beam = be;
        this.input = ih;
        this.readerLock = new Object();
        this.placements = new ArrayList<>();
        this.placements.add("webpanel");
        this.placements.add("bookmarks");
        this.pageEditVars = new ArrayList<>();
        this.pageEditVars.add("name");
        this.pageEditVars.add("shortcuts");
        this.pageEditVars.add("url");
        this.pageEditVars.add("directory");
        this.pageEditVars.add("browser");
        
    }
    
    // ------------------- External IO Interface methods -----------------------
    
    
    @Override
    public void isActive () throws RemoteException {
        // nothing to do here.
        // if this method is reachable itself through RMI it means
        // that whole program is active.
    }
    
    @Override
    public void showTask (Task task) throws RemoteException {
        try {
            this.printer.showTask(task);
        } catch (IOException e) {}
    }
    
    
    @Override
    public void reportInfo (String... info) throws RemoteException {
        try {
            this.printer.printBeamWithMessageLn(info);
        } catch (IOException e) {}
    }
    
    @Override
    public void reportMessage (String... message) throws RemoteException {
        try {
            this.printer.printBeamWithUnderLn(message);
        } catch (IOException e) {}
    }
    
    @Override
    public void reportError (String... error) throws RemoteException {
        try {
            this.printer.printBeamErrorWithMessageLn(error);
        } catch (IOException e) {}
    }
    
    @Override
    public void reportException (String... description) throws RemoteException {
        try {
            this.printer.printBeamErrorWithMessageLn(description);
        } catch (IOException e) {}
    }
        
    @Override
    public void exitExternalIO() throws RemoteException {
        this.closeConsole();
    }
        
    @Override
    public int chooseVariants(String message, List<String> variants) throws RemoteException {
        return this.input.chooseVariants(message, variants);
    }    
    
    // ------------------- Console Dispatcher methods --------------------------
    
    @Override
    public void closeConsole() throws RemoteException {
        try {
            if (this.beam.remoteControl() != null) {
                this.beam.remoteControl().setDefaultIO();
            }    
        } catch (RemoteException e) { }
        System.exit(0);
    }
    
    @Override
    public String waitForNewCommand() throws IOException {
        synchronized (this.readerLock) {
            return this.reader.read();
        }
    }
    
    @Override
    public void newLoop() throws IOException {
        this.printer.printBeam();
    }
    
    @Override
    public void printHelp() {
        
    }
    
    @Override
    public void getLocations() throws IOException {
        List<Location> locations = this.beam.locations().getAllLocations();
        this.printer.printLocations(locations);
    }
    
    @Override
    public void getCommands() throws IOException {
        List<StoredExecutorCommand> commands = this.beam.executor().getAllCommands();
        this.printer.printCommands(commands);
    }    
    
    @Override
    public void newLocation() throws IOException {        
        this.printer.printUnder("set name: ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        }
        this.printer.printUnder("set path: ");
        String location = this.reader.read();
        if (location.isEmpty()) {
            return;
        }
        if ( this.beam.locations().newLocation(location, name) ) {
            this.printer.printUnderLn("New location created.");
        }
    }
    
    @Override
    public void newTask() throws IOException {        
        String newTime = this.input.inputTime();
        if (newTime.isEmpty()) {
            return;
        }
        String[] newTask = this.input.inputTask();
        this.beam.taskManager().createNewTask(newTime, newTask);
    }
    
    @Override
    public void newEvent() throws IOException{
        
    }
    
    @Override
    public void newCommand() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        } 
        List<String> commands = this.input.inputCommands();
        if (commands.size() > 0) {
            this.beam.executor().newCommand(commands, name);
        }
    }
    
    @Override
    public void newWebPage() throws IOException {
        this.printer.printUnder("name : ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        }
        this.printer.printUnder("shortcuts : ");
        String shortcuts = this.reader.read();
        this.printer.printUnder("url : ");
        String urlAddress = this.reader.read();
        if (urlAddress.isEmpty()) {
            return;
        }
        WebPagePlacement placement = this.askForPlacement();
        if (placement == null) {
            return;
        }
        this.printer.printUnder("directory : ");
        String category = this.reader.read();
        if (category.isEmpty()) {
            category = "common";
        }
        this.printer.printUnder("browser : ");
        String browser = this.reader.read();
        if(browser.isEmpty()) {
            browser = "default";
        }
        this.beam.webPages().newWebPage(
                name, shortcuts, urlAddress, placement, category, browser);
    }
    
    @Override
    public void getAllWebPages() throws IOException {
        WebPagePlacement placement = this.askForPlacement();
        if (placement == null) {
            return;
        } 
        List<WebPage> pages = this.beam.webPages()
                .getAllPagesInPlacement(placement);
        this.printer.printWebPages(pages, true);
    }
    
    @Override
    public void getAllWebPanelPages() throws IOException {
        List<WebPage> pages = this.beam.webPages()
                .getAllPagesInPlacement(WebPagePlacement.WEBPANEL);
        this.printer.printWebPages(pages, true);
    }
    
    @Override
    public void getAllBookmarksPages() throws IOException {
        List<WebPage> pages = this.beam.webPages()
                .getAllPagesInPlacement(WebPagePlacement.BOOKMARKS);
        this.printer.printWebPages(pages, true);
    }
    
    @Override
    public void getAllChoices() throws IOException {
        this.printer.printChoices(this.beam.executor().getAllChoices());
    }
    
    @Override
    public void deleteLocation() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if ( !name.isEmpty() ) {
            if (this.beam.locations().deleteLocation(name)) {
                this.printer.printUnderLn("Location was removed.");
            }
        }        
    }
    
    @Override
    public void deleteWebPage(String name) {
        
    }
    
    @Override
    public void deleteWebPage() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if ( !name.isEmpty() ) {
            if (this.beam.webPages().deleteWebPage(name)) {
                this.printer.printUnderLn("WebPage was removed.");
            }
        }
    }
    
    @Override
    public void deleteTask() throws IOException {        
        String deleted = input.inputTextToDelete();
        // stop deletion if text input was incorrect or stopped
        if ( deleted.length() > 0 ) {
            if (this.beam.taskManager().deleteTaskByText(deleted)) {
                this.printer.printUnderLn("Task has been deleted.");
            }                                    
        }
    }
    
    @Override
    public void deleteEvent() throws IOException{
        
    }
    
    @Override
    public void deleteCommand() throws IOException{
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        }
        if (name.length() > 0) {
            if (this.beam.executor().deleteCommand(name)) {
                this.printer.printUnderLn("Command was removed.");
            }
        }        
    }
    
    @Override
    public void deleteMem() throws IOException {
        this.printer.printUnder("command choice in memory: ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        }
        if ( ! name.isEmpty() ) { 
            if ( this.beam.executor().deleteMem(name) ) {
                this.printer.printUnderLn("Choice removed from memory.");
            }
        }
    }
    
    @Override
    public void editPage() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()){
            return;
        }
        
        int choosed = this.input.chooseVariants("edit: ", this.pageEditVars);
        if (choosed < 0) {
            return;
        } else if (choosed == 1) {
            this.printer.printUnder("new name: ");
            String newName = this.reader.read();
            if (newName.isEmpty()){
                return;
            }
            if (this.beam.webPages().editWebPageName(name, newName)){
                this.printer.printUnderLn("Page was renamed.");
            }
        } else if (choosed == 2) {
            this.printer.printUnder("new shortcuts: ");
            String newShorts = this.reader.read();
            if (newShorts.isEmpty()) {
                return;
            }
            if (this.beam.webPages().editWebPageShortcuts(name, newShorts)){
                this.printer.printUnderLn("Shortcuts was changed.");
            }
        } else if (choosed == 3) {
            this.printer.printUnder("new url: ");
            String newUrl = this.reader.read();
            if (newUrl.isEmpty()) {
                return;
            }
            if (this.beam.webPages().editWebPageUrl(name, newUrl)){
                this.printer.printUnderLn("URL was changed.");
            }
        } else if (choosed == 4) {
            this.printer.printUnder("new directory: ");
            String newDirectoReady = this.reader.read();
            if (newDirectoReady.isEmpty()) {
                return;
            }
            if (this.beam.webPages().editWebPageDirectory(name, newDirectoReady)){
                this.printer.printUnderLn("directory was changed.");
            }
        } else if (choosed == 5) {
            this.printer.printUnder("new browser: ");
            String newBrowser = this.reader.read();
            if (newBrowser.isEmpty()) {
                return;
            }
            if (this.beam.webPages().editWebPageBrowser(name, newBrowser)) {
                this.printer.printUnderLn("New browser was assigned to "+name+".");
            }
        } 
    }
    
    @Override
    public void movePageToDirectoryAndPlacement() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()){
            return;
        }
        this.printer.printUnder("new directory : ");
        String newDir = this.reader.read();
        if ( newDir.isEmpty() ) {
            return;
        }
        WebPagePlacement placement = this.askForPlacement();
        if ( placement == null ) {
            return;
        }
        if ( this.beam.webPages().moveWebPageTo(name, newDir, placement)) {
            this.printer.printUnderLn("Page was moved.");
        }
    }
    
    @Override
    public void editLocation() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()){
            return;
        }
        String[] vars = {"name", "path"};
        int choosed = this.input.chooseVariants("edit: ", Arrays.asList(vars));
        if (choosed < 0){
            return;
        } else if (choosed == 1) {
            this.printer.printUnderLn("Not implemented yet :(");
        } else if (choosed == 2) {
            this.printer.printUnder("new path: ");
            String newPath = this.reader.read();
            if (newPath.isEmpty()) {
                return;
            }
            if (this.beam.locations().editLocationPath(name, newPath)) {
                this.printer.printUnderLn("Path of "+name+" was changed.");
            } else {
                this.printer.printUnderLn("Something is wrong: edit failed :(");
            }
        }
    }
    
    @Override
    public void editCommand() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if (name.isEmpty()) {
            return;
        }
        String[] vars = {"name", "commands"};
        int choosed = this.input.chooseVariants("edit: ", Arrays.asList(vars));
        if (choosed < 0){
            return;
        } else if (choosed == 1) {
            this.printer.printUnderLn("Not implemented yet :(");
        } else if (choosed == 2) {
            this.printer.printUnderLn("Not implemented yet :(");
        }
    }
    
    @Override
    public void renameDirectory() throws IOException {
        WebPagePlacement placement = this.askForPlacement();
        if (placement == null) {
            return;
        } 
        this.printer.printUnder("directory: ");
        String directory = this.reader.read();
        if (directory.isEmpty()) {
            return;
        }
        this.printer.printUnder("new name: ");
        String newDirectory = this.reader.read();
        if (newDirectory.isEmpty()) {
            return;
        }
        if (this.beam.webPages().renameDirectory(
                directory, newDirectory, placement)) {
            this.printer.printUnderLn("Directory renamed.");
        }
    }
    
    @Override
    public void listLocation(String locationName) throws IOException {
        if ( locationName.isEmpty() ) {
            return;
        }
        this.printer.printLocationContent(
                this.beam.executor().listLocationContent(locationName));                
    }
    
    @Override
    public void getLocation() throws IOException {
        this.printer.printUnder("name: ");
        String location = this.reader.read();
        if ( !location.isEmpty() ) {
            this.printer.printLocations(this.beam.locations().getLocations(location));
        }
    }
    
    @Override
    public void getPage() throws IOException {
        this.printer.printUnder("name: ");
        String name = this.reader.read();
        if ( !name.isEmpty() ) {
            this.printer.printWebPages(this.beam.webPages().getWebPages(name), false);
        }
    }
     
    @Override
    public void getPagesInDirectoryAndPlacement() throws IOException {
        WebPagePlacement placement = this.askForPlacement();
        if (placement == null) {
            return;
        } 
        this.printer.printUnder("directory: ");
        String directory = this.reader.read();
        if ( !directory.isEmpty() ) {
            this.printer.printWebPages(
                    this.beam.webPages().getAllWebPagesInDirectoryAndPlacement(directory, placement), 
                    false);
        }
    }
    
    @Override
    public void getCommand() throws IOException {
        this.printer.printUnder("name: ");
        String commandName = this.reader.read();
        if ( !commandName.isEmpty() ) {
            this.printer.printCommands(
                    this.beam.executor().getCommand(commandName));
        }
    }
    
    @Override
    public void call(List<String> command) throws IOException {
        this.beam.executor().call(command);
    }
    
    @Override
    public void start(List<String> command) throws IOException {
        this.beam.executor().start(command);
    }
    
    @Override
    public void stop(List<String> command) throws IOException {
        this.beam.executor().stop(command);
    }
    
    @Override
    public void run(List<String> command) throws IOException {
        this.beam.executor().run(command);
    }
    
    @Override
    public void open(List<String> command) throws IOException {
        this.beam.executor().open(command);
    }
    
    @Override
    public void openWebPage(List<String> command) throws IOException {
        this.beam.executor().openWebPage(command);
    }
    
    @Override
    public void printAlarm() throws IOException {
        this.printer.printUnderLn(
                "Next task: " + this.beam.taskManager().getFirstAlarmTime());
    }
    
    @Override
    public void printPastTasks() throws IOException {
        this.printer.printTasks(
                "Past tasks:", 
                this.beam.taskManager().getPastTasks());
    }
    
    @Override
    public void printActualTasks() throws IOException {
        this.printer.printTasks(
                "Future tasks:", 
                this.beam.taskManager().getFutureTasks());
    }
    
    @Override
    public boolean confirmAction(String question) throws IOException {
        return this.input.confirmAction(question);
    }
    
    @Override
    public void removeAllTasks() throws IOException {
        if (this.beam.taskManager().removeAllTasks()) {
            this.printer.printUnderLn("All tasks have been removed.");
        }
    }
    
    @Override
    public void removeAllPastTasks() throws IOException {
        if (this.beam.taskManager().removeAllPastTasks()) {
            this.printer.printUnderLn("All past tasks have been removed.");
        }
    }
    
    @Override
    public void removeAllFutureTasks() throws IOException {
        if (this.beam.taskManager().removeAllFutureTasks()) {
            this.printer.printUnderLn("All future tasks have been removed.");
        }
    }
    
    @Override
    public void exitDialog() throws IOException {
        boolean confirm = this.confirmAction("Do you really want to stop me?");        
        if (confirm){
            this.printer.exitMessage();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            exitProgram();
        }
    }
    
    private void exitProgram(){
        try{  
            if (this.beam.remoteControl() != null) {
                this.beam.remoteControl().exit();
            }
        } catch(RemoteException e) {} 
        System.exit(0);
    }
    
    @Override
    public void useNativeShowTaskMethod() throws IOException {
        this.beam.remoteControl().useNativeShowTaskMethod();
    }
    
    @Override
    public void useExternalShowTaskMethod() throws IOException {
        this.beam.remoteControl().useExternalShowTaskMethod();
    }
    
    @Override
    public void setAskUserToRememberHisChoice(String yesOrNo) throws IOException {
        boolean yes = this.input.checkOnYes(yesOrNo);
        this.beam.executor().setAskUserToRememberHisChoice(yes);
    }
    
    @Override
    public void setIntelligentActive(String yesOrNo) throws IOException {
        boolean yes = this.input.checkOnYes(yesOrNo);
        this.beam.executor().setIntelligentActive(yes);
    }
    
    @Override
    public void openNotes() throws IOException {
        this.beam.executor().openNotes();
    }
    
    @Override
    public void openNote(List<String> command) throws IOException {
        this.beam.executor().openNote(command);
    }
    
    @Override
    public void newNote(List<String> params) throws IOException {
        this.beam.executor().newNote(params);
    }
    
    private WebPagePlacement askForPlacement() {
        int choice = this.input.chooseVariants("placement : ", this.placements);
        if ( choice == 1 ) {
            return WebPagePlacement.WEBPANEL;
        } else if ( choice == 2 ) {
            return WebPagePlacement.BOOKMARKS;
        } else {
            return null;
        }
    }
}
