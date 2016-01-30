/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.x.old.external.sysconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.core.entities.Location;
import com.drs.beam.core.entities.WebPage;
import com.drs.beam.core.modules.executor.StoredExecutorCommand;
import com.drs.beam.core.modules.tasks.Task;
import com.drs.beam.core.rmi.interfaces.RmiRemoteControlInterface;
import com.drs.beam.core.rmi.interfaces.RmiExecutorInterface;
import com.drs.beam.core.rmi.interfaces.RmiLocationsHandlerInterface;
import com.drs.beam.core.rmi.interfaces.RmiTaskManagerInterface;
import com.drs.beam.core.rmi.interfaces.RmiWebPageHandlerInterface;
import com.drs.beam.x.old.util.config.ConfigContainer;

/*
 * Class represents external console.
 * Reads commands, sends them to main program and prints output. It can be closed 
 * without exit of main program.
 */
public class Console implements Runnable, ExternalIOInterface {
    // Fields =============================================================================
    private final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final InputHandler input;
    private final HelpWriter helper;
    
    private final String BEAM =     "Beam > ";    
    private final String UNDER =    "     > ";
    private final String SPACE =    "       ";
    private final String ERROR =    "Beam error > ";
    
    private final String[] yesPatterns = {"y", "+", "yes", "ye", "true", "enable"};
    private final String[] stopPatterns = {".", "", "s", " ", "-", "false", "disable"};
    private final String[] helpPatterns = {"h", "help", "hlp", "hp"};
    
    private RmiTaskManagerInterface taskManager;
    private RmiExecutorInterface executor;
    private RmiRemoteControlInterface beamRemoteAccess;
    private RmiLocationsHandlerInterface locations;
    private RmiWebPageHandlerInterface webPages;
    
    // Constructor ========================================================================
    public Console() {
        this.input = new InputHandler(this);
        this.helper = new HelpWriter(this);
    }    
    
    // Methods ============================================================================
    
    public static void main(String[] args) {
        ConfigContainer.parseStartArgumentsIntoConfiguration(args);
        Console console = new Console();
        ConsoleRemoteManager manager = new ConsoleRemoteManager(console);
        manager.connect();
        ConfigContainer.cancel();
        new Thread(console, "Beam_console").start();
    }    
    
    void setTaskManager(RmiTaskManagerInterface tm) {
        this.taskManager = tm;
    }

    void setExecutor(RmiExecutorInterface e) {
        this.executor = e;
    }

    void setBeamRemoteAccess(RmiRemoteControlInterface remoteAccess) {
        this.beamRemoteAccess = remoteAccess;
    }
    
    void setLocationsHandler(RmiLocationsHandlerInterface locationsHandler){
        this.locations = locationsHandler;
    }
    
    void setWebPagesHandler(RmiWebPageHandlerInterface webPagesHandler){
        this.webPages = webPagesHandler;
    }
    
    String readConsole() throws IOException{
        String info = this.reader.readLine().trim().toLowerCase();
        if ( checkOnStop(info) ){
            return "";
        } else {
            return info;
        }
    }
    
    BufferedReader reader(){
        return this.reader;
    }
    
    BufferedWriter writer(){
        return this.writer;
    }
    
    HelpWriter helpWriter(){
        return this.helper;
    }
    
    private boolean check(String s, String[] patterns){
        for (String pattern : patterns){
            if (pattern.equals(s)) return true;
        }
        return false;
    }
    
    boolean checkOnYes(String input){
        return check(input, this.yesPatterns);
    }
    
    boolean checkOnStop(String input){
        return check(input, this.stopPatterns);
    }
    
    boolean checkOnHelp(String input){
        return check(input, this.helpPatterns);
    }
    
    String format(String info, int formatLength){        
        while(formatLength <= info.length()){
            formatLength += 10;
        }
        char[] result = Arrays.copyOf(info.toCharArray(), formatLength);
        Arrays.fill(result, info.length(), formatLength, ' ');
        return new String(result);
    }
    
    void closeConsole(){
        try{
            if (this.beamRemoteAccess != null){
                this.beamRemoteAccess.setDefaultIO();
            }    
        } catch(RemoteException e){}
        System.exit(0);
    }
    
    private void exitProgram(){
        try{  
            if (this.beamRemoteAccess != null){
                this.beamRemoteAccess.exit();
            }
        } catch(RemoteException e){} 
        System.exit(0);
    }
    
    private void exitProgramIfCriticalError(boolean isCritical){
        if(isCritical){
            try{
                Thread.sleep(5000);
                this.beamRemoteAccess.exit();
                System.exit(1);
            } catch(InterruptedException|RemoteException e){}
        }
    }
    
    private boolean confirmAction(String actionDescription) throws IOException{
        this.printBeamWithMessageLn(actionDescription);
        this.printUnder("yes / no : ");
        String response = this.readConsole();
        return checkOnYes(response);
    }
    
    private void exitDialog() throws IOException{
        boolean confirm = confirmAction("Do you really want to stop me?");        
        if (confirm){
            this.writer.write(" :(");
            this.writer.newLine();
            this.writer.write("will meet you another time...");
            this.writer.flush();
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            exitProgram();
        }
    }
    
    @Override
    public void run(){
        String command;
        List<String> params = new ArrayList<>(); 
        input: while (true){
            try{
                this.printBeam();
                command = this.reader.readLine().trim().toLowerCase();
                if (command.length() == 0){
                    continue input;
                }
                this.parseCommand(command, params);                
                parsing: switch (params.get(0)){ 
                    case "help" : {
                        printHelp();
                        break parsing;
                    }
                    case "close" :{
                        closeConsole();
                    }
                    case "+" :
                    case "new" : {
                        if (params.size() < 2){
                            continue input;
                        }                        
                        switch (params.get(1)){
                            case "loc" :
                            case "location" : {
                                newLocation();
                                break parsing;
                            }
                            case "task" : {
                                newTask();
                                break parsing;
                            }
                            case "event" : {
                                newEvent();
                                break parsing;
                            }
                            case "comm" :
                            case "command" : {
                                newCommand();
                                break parsing;
                            }
                            case "page" :
                            case "web" : {
                                newWebPage();
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "li" :
                    case "list" : {
                        if (params.size() > 1){
                            this.listLocation(params.get(1));
                        }
                        break parsing;
                    }
                    case "get" : {
                        if (params.size() < 2){
                            continue input;
                        }                        
                        switch (params.get(1)){
                            case "comm" :
                            case "command" : {
                                this.getCommand();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                this.getLocation();
                                break parsing;
                            } 
                            case "web" :
                            case "page" : {
                                getPage();
                                break parsing;
                            }
                            case "cat" :
                            case "category" : {
                                getPagesOfCategory();
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "alarm" : {
                        printUnderLn(this.taskManager.getFirstAlarmTime());
                        break parsing;
                    }
                    case "r" :
                    case "run" : {
                        this.executor.run(params); 
                        break parsing;
                    }
                    case "start" : {
                        this.executor.start(params);
                        break parsing;
                    }
                    case "stop" : {
                        this.executor.stop(params);
                        break parsing;
                    }
                    case "o" :
                    case "op" :    
                    case "open" : {
                        this.executor.open(params);
                        break parsing;
                    }
                    case "exe" :
                    case "call" : {
                        this.executor.call(params);
                        break parsing;
                    }
                    case "www" :
                    case "web" :
                    case "see" : {
                        this.executor.openWebPage(params);
                    }
                    case "all" :
                    case "view" : {
                        if (params.size() < 2){
                            continue input;
                        }                        
                        switch (params.get(1)){
                            case "tasks" :
                            case "future" : {                                
                                printTasks("Future tasks:", this.taskManager.getFutureTasks());                                
                                break parsing;
                            }
                            case "past" : {
                                printTasks("Past tasks:", this.taskManager.getPastTasks());
                                break parsing;
                            }
                            case "loc" :
                            case "location" :
                            case "locations" : {
                                getLocations();
                                break parsing;
                            }
                            case "mem" :
                            case "choices" : {
                                getAllChoices();
                                break parsing;
                            }
                            case "comm" :
                            case "comms" :    
                            case "commands" : {
                                getCommands();
                                break parsing;
                            }
                            case "web" :
                            case "page" :
                            case "pages" : {
                                seeAllWebPages();
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
                        if (params.size() < 2){
                            continue input;
                        }
                        switch (params.get(1)){
                            case "page" : {
                                editPage();
                                break parsing;
                            } 
                            case "loc" :
                            case "location" : {
                                editLocation();
                                break parsing;
                            } 
                            case "comm" : 
                            case "command" : {
                                editCommand();
                                break parsing;
                            }
                            case "cat" :
                            case "category" : {
                                renameCategory();
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
                            continue input;
                        }                        
                        switch (params.get(1)){
                            case "task" : {
                                deleteTask();
                                break parsing;
                            }
                            case "event" : {
                                deleteEvent();
                                break parsing;
                            }
                            case "loc" :
                            case "location" : {
                                deleteLocation();
                                break parsing;
                            }
                            case "page" : {
                                deleteWebPage();
                                break parsing;
                            }
                            case "mem" :
                            case "memory" : {
                                deleteMem();
                                break parsing;
                            }                                
                            case "com" :
                            case "comm" :
                            case "command" : {
                                deleteCommand();
                                break parsing;
                            }
                            case "all" : {
                                if (params.size() < 3){
                                    continue input;
                                }
                                switch (params.get(2)){
                                    case "tasks" : {
                                        boolean confirm = confirmAction("Delete all tasks?");
                                        if(confirm){
                                            confirm = confirmAction("Really?");
                                            if (confirm){
                                                if (this.taskManager.removeAllTasks()){
                                                    printUnderLn("All tasks removed.");
                                                }
                                            }                                            
                                        }
                                        break parsing;
                                    }
                                    case "past" : {
                                        if (this.taskManager.removeAllPastTasks()){
                                            printUnderLn("Past tasks removed.");
                                        }
                                        break parsing;
                                    }
                                    case "future" : {
                                        boolean confirm = confirmAction("Delete all future tasks?");
                                        if (confirm){
                                            if (this.taskManager.removeAllFutureTasks()){
                                                printUnderLn("Future tasks removed.");
                                            }
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
                    case "exit" :{
                        exitDialog();
                        break parsing;
                    }
                    case "use" : {
                        if (params.size() < 2){
                            continue input;
                        }                        
                        switch (params.get(1)){
                            case "native" : {
                                this.beamRemoteAccess.useNativeShowTaskMethod();
                                printUnderLn("I will use native task`s output.");
                                break parsing;
                            }
                            case "ext" :
                            case "external" : {
                                this.beamRemoteAccess.useExternalShowTaskMethod();
                                printUnderLn("I will use Console for task`s output");
                                break parsing;
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "clear" : {
                        if (params.size() < 2) {
                            continue input;
                        }
                        switch (params.get(1)) {
                            case "tasks" : {
                                if (this.taskManager.removeAllPastTasks()) {
                                    printUnderLn("Past tasks removed.");
                                }
                            }
                            default : {
                                break parsing;
                            }
                        }
                    }
                    case "set" : {
                        if (params.size() < 2) {
                            continue input;
                        }
                        switch (params.get(1)) {
                            case "intell" :
                            case "intel" : {
                                if (params.size() < 3) {
                                    continue input;
                                }
                                switch (params.get(2)) {
                                    case "ask" : {
                                        if (params.size() < 4) {
                                            continue input;
                                        }
                                        if (this.checkOnYes(params.get(3))) {
                                            this.executor.setAskUserToRememberHisChoice(true);
                                            break parsing;
                                        }
                                        if (this.checkOnStop(params.get(3))) {
                                            this.executor.setAskUserToRememberHisChoice(false);
                                            break parsing;
                                        }
                                        break parsing;
                                    }
                                    case "active" : {
                                        if (params.size() < 4) {
                                            continue input;
                                        }
                                        if (this.checkOnYes(params.get(3))) {
                                            this.executor.setIntelligentActive(true);
                                            break parsing;
                                        }
                                        if (this.checkOnStop(params.get(3))) {
                                            this.executor.setIntelligentActive(false);
                                            break parsing;
                                        }
                                        break parsing;
                                    }
                                }
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
    
    private void parseCommand(String command, List<String> params){
        params.clear();
        params.addAll(Arrays.asList(command.split("\\s+")));
    }
    
    // Console`s methods to format it's output --------------------------------------------

    void printBeam() throws IOException{
            this.writer.write(BEAM);
            this.writer.flush();
    }
    
    void printBeamWithMessage(String s) throws IOException{
            this.writer.write(BEAM);
            this.writer.write(s);
            this.writer.flush();
    }
    
    void printBeamWithUnderLn(String... message) throws IOException{
        for (String s : message){
            this.writer.write(UNDER);
            this.writer.write(s);
            this.writer.newLine();
        }
        this.writer.flush();
    }
    
    void printBeamWithMessageLn(String... message) throws IOException{
        for (String s : message){
            this.writer.write(BEAM);
            this.writer.write(s);
            this.writer.newLine();
        }
        this.writer.flush();
    }
    
    void printUnder(String s) throws IOException{
        this.writer.write(UNDER);
        this.writer.write(s);
        this.writer.flush();
    }
    
    void printUnderLn(String s) throws IOException{
        this.writer.write(UNDER);
        this.writer.write(s);
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printSpace(String s) throws IOException{
        this.writer.write(SPACE);
        this.writer.write(s);
        this.writer.flush();
    }
    
    void printSpaceLn(String s) throws IOException{
        this.writer.write(SPACE);
        this.writer.write(s);
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printBeamErrorWithMessageLn(String[] message) throws IOException{
        for (String s : message){
            this.writer.write(ERROR);
            this.writer.write(s);
            this.writer.newLine();
        }                
        this.writer.flush();
    }
    
    void printTasks(String label, List<Task> tasks) throws IOException{
        if (tasks.isEmpty()) {
            printUnderLn(label+" there aren`t any tasks.");
            return;
        }
        printUnderLn(label);
        this.writer.write(SPACE);
        this.writer.write("=========================");
        this.writer.newLine();
        for(Task task : tasks){
            this.writer.newLine();
            this.writer.write(SPACE);
            this.writer.write("| " + task.getTimeOutputString());
            this.writer.newLine();
            for (String s : task.getContent()){
                this.writer.write(SPACE);
                this.writer.write("| " + s);
                this.writer.newLine();
            }
            this.writer.flush();    
        }
        this.writer.newLine();
        this.writer.write(SPACE);
        this.writer.write("=========================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    void printHelp(){
        
    }
    
    private void printCommands(List<StoredExecutorCommand> commands) throws IOException {
        if (commands.isEmpty()) {
            printUnderLn("There aren`t any commands.");
            return;
        }
        printUnderLn("Commands:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(StoredExecutorCommand command : commands){
            this.writer.write(SPACE);
            this.writer.write(command.getName() + ":");
            this.writer.newLine();
            this.writer.flush();
            for (String commandLine : command.getCommands()){
                this.writer.write(SPACE);
                this.writer.write("   > ");
                this.writer.write(commandLine);
                this.writer.newLine();
                this.writer.flush();
            }
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    private void printLocations(List<Location> locations) throws IOException{        
        if (locations.isEmpty()) {
            printUnderLn("There aren`t any locations.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Locations:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(Location location : locations){
            sb.append(SPACE)
                    .append(format(location.getName(), 15))
                    .append(location.getPath());
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    private void printWebPages(List<WebPage> pages, boolean compressOutput) throws IOException {
        if (pages.isEmpty()){
            printUnderLn("There aren`t any pages.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Web Pages:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(WebPage page : pages){
            sb.append(SPACE)
                    .append(format(page.getName(), 17))
                    .append(format(page.getCategory(), 10))
                    .append(page.getUrlAddress());
            if (compressOutput && sb.length() > 79){
                sb.delete(76, sb.length());
                sb.append("...");
            }
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    private void printChoices(Map<String, String> choices) throws IOException {
        if (choices.isEmpty()) {
            printUnderLn("There aren't any command choices.");
            return;
        } 
        StringBuilder sb = new StringBuilder();
        printUnderLn("Choices: ");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for (Map.Entry<String, String> entry : choices.entrySet()) {
            sb.append(SPACE)
                    .append(format(entry.getKey(), 30))
                    .append("choice--> ")
                    .append(entry.getValue());
            this.writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            this.writer.newLine();
            this.writer.flush();
        }
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        this.writer.flush();
    }
    
    private void getLocations() throws IOException {
        List<Location> locations = this.locations.getAllLocations();
        this.printLocations(locations);
    }
    
    private void getCommands() throws IOException{
        List<StoredExecutorCommand> commands = this.executor.getAllCommands();
        this.printCommands(commands);
    }    

    
    private void newLocation() throws IOException{        
        printUnder("set name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)) return;
        printUnder("set path: ");
        String location = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(location)) return;
        if ( this.locations.newLocation(location, name) ) {
            this.printUnderLn("New location created.");
        }
    }
    
    private void newTask() throws IOException{        
        String newTime = this.input.inputTime();
        if (checkOnStop(newTime)) return;
        String[] newTask = this.input.inputTask();
        this.taskManager.createNewTask(newTime, newTask);
    }
    
    private void newEvent() throws IOException{
        
    }
    
    private void newCommand() throws IOException{
        printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        } 
        List<String> commands = this.input.inputCommands();
        if (commands.size() > 0){
            this.executor.newCommand(commands, name);
        }
    }
    
    private void newWebPage() throws IOException {
        printUnder("name : ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        printUnder("url : ");
        String urlAddress = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(urlAddress)){
            return;
        }
        printUnder("category : ");
        String category = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(category)){
            category = "common";
        }
        printUnder("browser : ");
        String browser = this.reader.readLine().trim().toLowerCase();
        if(checkOnStop(browser)){
            browser = "default";
        }
        this.webPages.newWebPage(name, urlAddress, category, browser);
    }
    
    private void seeAllWebPages() throws IOException{
        List<WebPage> pages = this.webPages.getAllPages();
        this.printWebPages(pages, true);
    }
    
    private void getAllChoices() throws IOException {
        this.printChoices(this.executor.getAllChoices());
    }
    
    private void deleteLocation() throws IOException{
        printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (this.locations.deleteLocation(name)){
                printUnderLn("Location was removed.");
            }
        }        
    }
    
    private void deleteWebPage(String name){
        
    }
    
    private void deleteWebPage() throws IOException {
        printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (this.webPages.deleteWebPage(name)){
                printUnderLn("WebPage was removed.");
            }
        }
    }
    
    private void deleteTask() throws IOException{        
        String deleted = input.inputTextToDelete();
        // stop deletion if text input was incorrect or stopped
        if ( deleted.length() > 0 ){
            if (this.taskManager.deleteTaskByText(deleted)){
                printUnderLn("Task has been deleted.");
            }                                    
        }
    }
    
    private void deleteEvent() throws IOException{
        
    }
    
    private void deleteCommand() throws IOException{
        this.printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (this.executor.deleteCommand(name)){
                printUnderLn("Command was removed.");
            }
        }        
    }
    
    private void deleteMem() throws IOException {
        this.printUnder("command choice in memory: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)) {
            return;
        }
        if ( ! name.isEmpty() ) { 
            if ( this.executor.deleteMem(name) ) {
                printUnderLn("Choice removed from memory.");
            }
        }
    }
    
    private void editPage() throws IOException {
        this.printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        String[] vars = {"name", "url", "category", "browser"};
        int choosed = this.input.chooseVariants("edit: ", Arrays.asList(vars));
        if (choosed < 0){
            return;
        } else if (choosed == 1){
            this.printUnder("new name: ");
            String newName = this.reader.readLine().trim().toLowerCase();
            if (checkOnStop(newName)){
                return;
            }
            if (this.webPages.editWebPageName(name, newName)){
                printUnderLn("Page was renamed.");
            }
        } else if (choosed == 2){
            this.printUnder("new url: ");
            String newUrl = this.reader.readLine().trim().toLowerCase();
            if (checkOnStop(newUrl)){
                return;
            }
            if (this.webPages.editWebPageUrl(name, newUrl)){
                printUnderLn(name+ " URL was changed.");
            }
        } else if (choosed == 3){
            this.printUnder("new category: ");
            String newCategory = this.reader.readLine().trim().toLowerCase();
            if (checkOnStop(newCategory)){
                return;
            }
            if (this.webPages.editWebPageCategory(name, newCategory)){
                printUnderLn(name+ " page category was changed.");
            }
        } else if (choosed == 4) {
            this.printUnder("new browser: ");
            String newBrowser = this.reader.readLine().trim().toLowerCase();
            if (checkOnStop(newBrowser)) {
                return;
            }
            if (this.webPages.editWebPageBrowser(name, newBrowser)) {
                printUnderLn("New browser was assigned to "+name+".");
            }
        } 
    }
    
    private void editLocation() throws IOException {
        this.printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        String[] vars = {"name", "path"};
        int choosed = this.input.chooseVariants("edit: ", Arrays.asList(vars));
        if (choosed < 0){
            return;
        } else if (choosed == 1){
            this.printUnderLn("Not implemented yet :(");
        } else if (choosed == 2){
            this.printUnder("new path: ");
            String newPath = this.reader.readLine().trim().toLowerCase();
            if (checkOnStop(newPath)) {
                return;
            }
            if (this.locations.editLocationPath(name, newPath)) {
                printUnderLn("Path of "+name+" was changed.");
            } else {
                printUnderLn("Something is wrong: edit failed :(");
            }
        }
    }
    
    private void editCommand() throws IOException {
        this.printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        String[] vars = {"name", "commands"};
        int choosed = this.input.chooseVariants("edit: ", Arrays.asList(vars));
        if (choosed < 0){
            return;
        } else if (choosed == 1){
            this.printUnderLn("Not implemented yet :(");
        } else if (choosed == 2){
            this.printUnderLn("Not implemented yet :(");
        }
    }
    
    private void renameCategory() throws IOException {
        this.printUnder("category: ");
        String category = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(category)){
            return;
        }
        this.printUnder("new name: ");
        String newCategory = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(newCategory)){
            return;
        }
        if (this.webPages.renameCategory(category, newCategory)){
            this.printUnderLn("Category renamed.");
        }
    }
    
    private void listLocation(String locationName) throws IOException {
        List<String> locationContent = this.executor.listLocationContent(locationName);
        if (locationContent.size() > 1){
            this.printUnderLn(locationContent.get(0) + ":");
            for (int i = 1; i < locationContent.size(); i++){
                this.writer.write(SPACE+locationContent.get(i));
                this.writer.newLine();
            }
            this.writer.flush();
        } else if (locationContent.size() == 1){
            this.printUnderLn("Location " + locationContent.get(0) + " is empty.");
        }        
    }
    
    private void getLocation() throws IOException{
        this.printUnder("name: ");
        String location = this.readConsole();
        if (location.length() > 0){
            this.printLocations(this.locations.getLocations(location));
        }
    }
    
     private void getPage() throws IOException{
        this.printUnder("name: ");
        String name = this.readConsole();
        if (name.length() > 0){
            this.printWebPages(this.webPages.getWebPages(name), false);
        }
    }
     
    private void getPagesOfCategory() throws IOException {
        this.printUnder("category: ");
        String category = this.readConsole();
        if (category.length() > 0){
            this.printWebPages(this.webPages.getAllWebPagesOfCategory(category), false);
        }
    }
    
    private void getCommand() throws IOException {
        this.printUnder("name: ");
        String commandName = this.readConsole();
        if (commandName.length() > 0){            
            List<StoredExecutorCommand> commands = this.executor.getCommand(commandName);
            this.printCommands(commands);
        }
    }
    
    // ExternalIOInterface methods implementations -----------------------------------------------
    @Override
    public void showTask(Task task) throws RemoteException{
        try{
            this.writer.newLine();
            this.writer.write(SPACE);
            this.writer.write("| " + task.getTimeOutputString());
            this.writer.newLine();
            for (String s : task.getContent()){
                this.writer.write(SPACE);
                this.writer.write("| " + s);
                this.writer.newLine();
            }
            this.writer.flush();
            this.printBeam();
        } catch(IOException e){}
    }
    
    @Override
    public void isActive() throws RemoteException{
    }
    
    @Override
    public void reportInfo(String... info) throws RemoteException{
        try{
            this.printBeamWithMessageLn(info);
        }catch(IOException e){}
    }
    
    @Override
    public void reportMessage(String... info) throws RemoteException{
        try{
            this.printBeamWithUnderLn(info);
        }catch(IOException e){}
    }
    
    @Override
    public void reportError(String... error) throws RemoteException{
        try{
            this.printBeamErrorWithMessageLn(error);
        }catch(IOException e){}
    }
    
    @Override
    public void reportException (String[] description) throws RemoteException{
        try{
            this.printBeamErrorWithMessageLn(description);
        }catch(IOException ioe){}
    }
    
    @Override
    public void exitExternalIO() throws RemoteException{
        this.closeConsole();
    }
    
    @Override
    public int chooseVariants(String message, List<String> variants) throws RemoteException{
        return this.input.chooseVariants(message, variants);
    }
}
