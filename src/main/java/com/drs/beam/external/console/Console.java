/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.external.console;

import com.drs.beam.external.ExternalIOInterface;
import com.drs.beam.modules.executor.ExecutorInterface;
import com.drs.beam.modules.io.RemoteAccessInterface;
import com.drs.beam.modules.tasks.TaskManagerInterface;
import com.drs.beam.modules.tasks.Task;
import com.drs.beam.util.config.ConfigContainer;
import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/*
 * Class represents external console.
 * Reads commands, sends them to main program and prints output. It can be closed 
 * without exit of main program.
 */
public class Console implements Runnable, ExternalIOInterface{
    // Fields =============================================================================
    private final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final InputHandler input;
    private final HelpWriter helper;
    
    private final String BEAM =     "Beam > ";    
    private final String UNDER =    "     > ";
    private final String SPACE =    "       ";
    private final String ERROR =    "Beam error > ";
    
    private final String[] yesPatterns = {"y", "+", "yes", "ye"};
    private final String[] stopPatterns = {".", "", "s", " "};
    private final String[] helpPatterns = {"h", "help", "hlp", "hp"};
    
    private TaskManagerInterface taskManager;
    private ExecutorInterface executor;
    private RemoteAccessInterface beamRemoteAccess;
    
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
        new Thread(console, "Org_console").start();
    }    
    
    void setTaskManager(TaskManagerInterface tm) {
        this.taskManager = tm;
    }

    void setOsExecutor(ExecutorInterface e) {
        this.executor = e;
    }

    void setOrgIO(RemoteAccessInterface io) {
        this.beamRemoteAccess = io;
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
    
    String format(String info, int length){
        char[] result = Arrays.copyOf(info.toCharArray(), length);
        Arrays.fill(result, info.length(), length, ' ');
        return new String(result);
    }
    
    void closeConsole(){
        try{
            if (this.beamRemoteAccess != null){
                this.beamRemoteAccess.setDefaultIO();
            }            
            System.exit(0);
        } catch(RemoteException e){}        
    }
    
    private void exitProgram(){
        try{  
            if (this.beamRemoteAccess != null){
                this.beamRemoteAccess.exit();
            }
            System.exit(0);
        } catch(RemoteException e){}  
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
        String response = this.reader.readLine().trim().toLowerCase();
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
                    case "c" :
                    case "call" : {
                        this.executor.call(params);
                        break parsing;
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
                                printLocations();
                                break parsing;
                            }
                            case "comm" :
                            case "comms" :    
                            case "commands" : {
                                printCommands();
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
                        if (params.size() < 2){
                            continue input;
                        }
                        switch (params.get(1)){
                            case "tasks" : {
                                this.taskManager.removeAllPastTasks();
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
    
    void printBeamWithMessageLn(String s) throws IOException{
            this.writer.write(BEAM);
            this.writer.write(s);
            this.writer.newLine();
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
    
    void printBeamErrorWithMessageLn(String s) throws IOException{
        this.writer.write(ERROR);
        this.writer.write(s);
        this.writer.newLine();
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
    
    private void printLocations() throws IOException{
        Map<String, String> locations = this.executor.getAllLocations();
        if (locations.isEmpty()) {
            printUnderLn("There aren`t any locations.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Locations:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(Map.Entry<String, String> entry : locations.entrySet()){
            sb.append(SPACE)
                    .append(format(entry.getKey(), 15))
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
    
    private void printCommands() throws IOException{
        Map<String, List<String>> commands = this.executor.getAllCommands();
        if (commands.isEmpty()) {
            printUnderLn("There aren`t any commands.");
            return;
        }
        printUnderLn("Commands:");
        this.writer.write(SPACE);
        this.writer.write("==================================================");
        this.writer.newLine();
        for(Map.Entry<String, List<String>> entry : commands.entrySet()){
            this.writer.write(SPACE);
            this.writer.write(entry.getKey()+":");
            this.writer.newLine();
            this.writer.flush();
            for (String commandLine : entry.getValue()){
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

    
    private void newLocation() throws IOException{        
        printUnder("set name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)) return;
        printUnder("set path: ");
        String location = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(location)) return;
        this.executor.newLocation(location, name);
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
    
    private void deleteLocation() throws IOException{
        printUnder("name: ");
        String name = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (this.executor.deleteLocation(name)){
                printUnderLn("Location was removed.");
            }
        }        
    }
    
    private void deleteTask() throws IOException{        
        String deleted = input.inputTextToDelete();
        // stop deletion if text input was incorrect or stopped
        if ( deleted.length() > 0 ){
            if (this.taskManager.deleteTaskByText(deleted)){
                printBeamWithMessageLn("Task has been deleted.");
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
        String location = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(location)){
            return;
        }
        if (location.length() > 0){
            String foundLocation = this.executor.getLocation(location);
            if (foundLocation.length() > 0){
                this.printSpaceLn(foundLocation);
            }
        }
    }
    
    private void getCommand() throws IOException {
        this.printUnder("name: ");
        String command = this.reader.readLine().trim().toLowerCase();
        if (checkOnStop(command)){
            return;
        }
        if (command.length() > 0){            
            List<String> commandContent = this.executor.getCommand(command);
            if (commandContent.size() > 1){
                this.printSpaceLn(commandContent.get(0) + ":");
                for (int i = 1; i < commandContent.size(); i++){
                    this.writer.write(SPACE + "   > " + commandContent.get(i));
                    this.writer.newLine();
                }
                this.writer.flush();
            } else if (commandContent.size() == 1){
                this.printUnderLn("Command " + commandContent.get(0) + " is empty.");
            }
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
    public void informAbout(String info) throws RemoteException{
        try{
            this.printBeamWithMessageLn(info);
        }catch(IOException e){}
    }
    
    @Override
    public void informAboutError(String error, boolean isCritical) throws RemoteException{
        try{
            this.printBeamErrorWithMessageLn(error);
        }catch(IOException e){}
        this.exitProgramIfCriticalError(isCritical);
    }
    
    @Override
    public void informAboutException (Exception e, boolean isCritical) throws RemoteException{
        try{
            this.writer.newLine();
            this.printBeamErrorWithMessageLn(e.getClass().getName());
            this.printBeamErrorWithMessageLn(e.getMessage());
            this.printBeamErrorWithMessageLn("-------> stack trace:");
            for (StackTraceElement element : e.getStackTrace()){
                this.printBeamErrorWithMessageLn(SPACE + element.toString());
            }
        }catch(IOException ioe){}
        this.exitProgramIfCriticalError(isCritical);
    }
    
    @Override
    public int chooseVariants(String message, List<String> variants) throws RemoteException{
        return this.input.chooseVariants(message, variants);
    }
}
