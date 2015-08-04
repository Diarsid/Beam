/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.external.console;

import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.modules.tasks.Task;
import com.drs.beam.util.config.ConfigContainer;
import java.io.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/*
 * Class represents external console.
 * Reads commands, sends them to main program and prints output. It can be closed 
 * without exit of main program.
 */
public class Console implements Runnable, ExternalIOIF{
    // Fields =============================================================================
    private final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final InputHandler input;
    private final HelpWriter help;
    
    private final String BEAM =     "Beam > ";    
    private final String UNDER =    "     > ";
    private final String SPACE =    "       ";
    private final String ERROR =    "Beam error > ";
    
    private final String[] yesPatterns = {"y", "+", "yes", "ye"};
    private final String[] stopPatterns = {".", "", "s", " "};
    private final String[] helpPatterns = {"h", "help", "hlp", "hp"};
    
    private static TaskManagerIF taskManager;
    private static ExecutorIF executor;
    private static OrgIOIF orgIO;
    
    // Constructor ========================================================================
    public Console() {
        this.input = new InputHandler(this);
        this.help = new HelpWriter(this);
    }    
    
    // Methods ============================================================================
    
    public static void main(String[] args) {
        ConfigContainer.parseStartArgumentsIntoConfiguration(args);
        Console console = new Console();
        ConsoleManager manager = new ConsoleManager(console);
        manager.connect();
        ConfigContainer.cancel();
        new Thread(console, "Org_console").start();
    }    
    
    void setTaskManager(TaskManagerIF tm) {
        taskManager = tm;
    }

    void setOsExecutor(ExecutorIF e) {
        executor = e;
    }

    void setOrgIO(OrgIOIF io) {
        orgIO = io;
    }
    
    BufferedReader reader(){
        return this.reader;
    }
    
    BufferedWriter writer(){
        return this.writer;
    }
    
    HelpWriter helpWriter(){
        return this.help;
    }
    
    private boolean check(String s, String[] patterns){
        for (String pattern : patterns){
            if (pattern.equals(s)) return true;
        }
        return false;
    }
    
    boolean checkOnYes(String input){
        return check(input, yesPatterns);
    }
    
    boolean checkOnStop(String input){
        return check(input, stopPatterns);
    }
    
    boolean checkOnHelp(String input){
        return check(input, helpPatterns);
    }
    
    String format(String info, int length){
        char[] result = Arrays.copyOf(info.toCharArray(), length);
        Arrays.fill(result, info.length(), length, ' ');
        return new String(result);
    }
    
    void close(){
        try{
            if (orgIO != null){
                orgIO.setDefaultIO();
            }            
            System.exit(0);
        } catch(RemoteException e){}        
    }
    
    private void exitProgram(){
        try{  
            if (orgIO != null){
                orgIO.exit();
            }
            System.exit(0);
        } catch(RemoteException e){}  
    }
    
    private void exitProgramIfCriticalError(boolean isCritical){
        if(isCritical){
            try{
                Thread.sleep(5000);
                orgIO.exit();
                System.exit(1);
            } catch(InterruptedException|RemoteException e){}
        }
    }
    
    private boolean confirmAction(String actionDescription) throws IOException{
        printBeamWithMessageLn(actionDescription);
        printUnder("yes / no : ");
        String response = reader.readLine().trim().toLowerCase();
        if (checkOnYes(response)){
            return true;
        } else {
            return false;
        }
    }
    
    private void exitDialog() throws IOException{
        boolean confirm = confirmAction("Do you really want to stop me?");        
        if (confirm){
            writer.write(" :(");
            writer.newLine();
            writer.write("will meet you another time...");
            writer.flush();
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
        String[] params; 
        input: while (true){
            try{
                this.printBeam();
                command = reader.readLine().trim().toLowerCase();
                params = command.split(" ");
                if (params.length == 0){
                    continue input;
                }
                parsing: switch (params[0]){ 
                    case "help" : {
                        printHelp();
                        break parsing;
                    }
                    case "close" :{
                        close();
                    }
                    case "+" :
                    case "new" : {
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
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
                    case "alarm" : {
                        printUnderLn(taskManager.getFirstAlarmTime());
                        break parsing;
                    }
                    case "r" :
                    case "run" : {
                        executor.run(command); 
                        break parsing;
                    }
                    case "start" : {
                        executor.start(command);
                        break parsing;
                    }
                    case "stop" : {
                        executor.stop(command);
                        break parsing;
                    }
                    case "o" :
                    case "op" :    
                    case "open" : {
                        executor.open(command);
                        break parsing;
                    }
                    case "exe" :
                    case "c" :
                    case "call" : {
                        executor.call(command);
                        break parsing;
                    }
                    case "all" :
                    case "view" : {
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
                            case "tasks" :
                            case "future" : {                                
                                printTasks("Future tasks:", taskManager.getFutureTasks());                                
                                break parsing;
                            }
                            case "past" : {
                                printTasks("Past tasks:", taskManager.getPastTasks());
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
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
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
                                if (params.length < 3){
                                    continue input;
                                }
                                switch (params[2]) {
                                    case "tasks" : {
                                        boolean confirm = confirmAction("Delete all tasks?");
                                        if(confirm){
                                            confirm = confirmAction("Really?");
                                            if (confirm){
                                                if (taskManager.removeAllTasks()){
                                                    printUnderLn("All tasks removed.");
                                                }
                                            }                                            
                                        }
                                        break parsing;
                                    }
                                    case "past" : {
                                        if (taskManager.removeAllPastTasks()){
                                            printUnderLn("Past tasks removed.");
                                        }
                                        break parsing;
                                    }
                                    case "future" : {
                                        boolean confirm = confirmAction("Delete all future tasks?");
                                        if (confirm){
                                            if (taskManager.removeAllFutureTasks()){
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
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
                            case "native" : {
                                orgIO.useNativeShowTaskMethod();
                                printUnderLn("I will use native task`s output.");
                                break parsing;
                            }
                            case "ext" :
                            case "external" : {
                                orgIO.useExternalShowTaskMethod();
                                printUnderLn("I will use Console for task`s output");
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
                command = "";
                params = null;             
            } catch (IOException e) {
                System.out.println("Exception:");
                System.out.println(e.getMessage());
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    // Console`s methods to format it's output --------------------------------------------

    void printBeam() throws IOException{
            writer.write(BEAM);
            writer.flush();
    }
    
    void printBeamWithMessage(String s) throws IOException{
            writer.write(BEAM);
            writer.write(s);
            writer.flush();
    }
    
    void printBeamWithMessageLn(String s) throws IOException{
            writer.write(BEAM);
            writer.write(s);
            writer.newLine();
            writer.flush();
    }
    
    void printUnder(String s) throws IOException{
        writer.write(UNDER);
        writer.write(s);
        writer.flush();
    }
    
    void printUnderLn(String s) throws IOException{
        writer.write(UNDER);
        writer.write(s);
        writer.newLine();
        writer.flush();
    }
    
    void printSpace(String s) throws IOException{
        writer.write(SPACE);
        writer.write(s);
        writer.flush();
    }
    
    void printSpaceLn(String s) throws IOException{
        writer.write(SPACE);
        writer.write(s);
        writer.newLine();
        writer.flush();
    }
    
    void printBeamErrorWithMessageLn(String s) throws IOException{
            writer.write(ERROR);
            writer.write(s);
            writer.newLine();
            writer.flush();
    }
    
    void printTasks(String label, List<Task> tasks) throws IOException{
        if (tasks.isEmpty()) {
            printUnderLn(label+" there aren`t any tasks.");
            return;
        }
        printUnderLn(label);
        writer.write(SPACE);
        writer.write("=========================");
        writer.newLine();
        for(Task task : tasks){
            writer.newLine();
            writer.write(SPACE);
            writer.write("| " + task.getTimeOutputString());
            writer.newLine();
            for (String s : task.getContent()){
                writer.write(SPACE);
                writer.write("| " + s);
                writer.newLine();
            }
            writer.flush();    
        }
        writer.newLine();
        writer.write(SPACE);
        writer.write("=========================");
        writer.newLine();
        writer.flush();
    }
    
    void printHelp(){
        
    }
    
    private void printLocations() throws IOException{
        Map<String, String> locations = executor.getLocations();
        if (locations.isEmpty()) {
            printUnderLn("There aren`t any locations.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        printUnderLn("Locations:");
        writer.write(SPACE);
        writer.write("==================================================");
        writer.newLine();
        for(Map.Entry<String, String> entry : locations.entrySet()){
            sb.append(SPACE)
                    .append(format(entry.getKey(), 15))
                    .append(entry.getValue());
            writer.write(sb.toString());
            sb = sb.delete(0, sb.length());
            writer.newLine();
            writer.flush();
        }
        writer.write(SPACE);
        writer.write("==================================================");
        writer.newLine();
        writer.flush();
    }
    
    private void printCommands() throws IOException{
        Map<String, List<String>> commands = executor.getCommands();
        if (commands.isEmpty()) {
            printUnderLn("There aren`t any commands.");
            return;
        }
        printUnderLn("Commands:");
        writer.write(SPACE);
        writer.write("==================================================");
        writer.newLine();
        for(Map.Entry<String, List<String>> entry : commands.entrySet()){
            writer.write(SPACE);
            writer.write(entry.getKey()+":");
            writer.newLine();
            writer.flush();
            for (String commandLine : entry.getValue()){
                writer.write(SPACE);
                writer.write("   > ");
                writer.write(commandLine);
                writer.newLine();
                writer.flush();
            }
        }
        writer.write(SPACE);
        writer.write("==================================================");
        writer.newLine();
        writer.flush();
    }    

    
    private void newLocation() throws IOException{        
        printUnder("set name: ");
        String name = reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)) return;
        printUnder("set path: ");
        String location = reader.readLine().trim().toLowerCase();
        if (checkOnStop(location)) return;
        executor.newLocation(location, name);
    }
    
    private void newTask() throws IOException{        
        String newTime = input.inputTime();
        if (checkOnStop(newTime)) return;
        String[] newTask = input.inputTask();
        taskManager.createNewTask(newTime, newTask);
    }
    
    private void newEvent() throws IOException{
        
    }
    
    private void newCommand() throws IOException{
        printUnder("name: ");
        String name = reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        } 
        List<String> commands = input.inputCommands();
        if (commands.size() > 0){
            executor.newCommand(commands, name);
        }
    }
    
    private void deleteLocation() throws IOException{
        printUnder("name: ");
        String name = reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (executor.deleteLocation(name)){
                printUnderLn("Location was removed.");
            }
        }        
    }
    
    private void deleteTask() throws IOException{        
        String deleted = input.inputTextToDelete();
        // stop deletion if text input was incorrect or stopped
        if ( deleted.length() > 0 ){
            if (taskManager.deleteTaskByText(deleted)){
                printBeamWithMessageLn("Task has been deleted.");
            }                                    
        }
    }
    
    private void deleteEvent() throws IOException{
        
    }
    
    private void deleteCommand() throws IOException{
        printUnder("name: ");
        String name = reader.readLine().trim().toLowerCase();
        if (checkOnStop(name)){
            return;
        }
        if (name.length() > 0){
            if (executor.deleteCommand(name)){
                printUnderLn("Command was removed.");
            }
        }        
    }
    
    // ExternalIOIF methods implementations -----------------------------------------------
    @Override
    public void showTask(Task task) throws RemoteException{
        try{
            writer.newLine();
            writer.write(SPACE);
            writer.write("| " + task.getTimeOutputString());
            writer.newLine();
            for (String s : task.getContent()){
                writer.write(SPACE);
                writer.write("| " + s);
                writer.newLine();
            }
            writer.flush();
            printBeam();
        } catch(IOException e){}
    }
    
    @Override
    public void isActive() throws RemoteException{
    }
    
    @Override
    public void informAbout(String info) throws RemoteException{
        try{
            printBeamWithMessageLn(info);
        }catch(IOException e){}
    }
    
    @Override
    public void informAboutError(String error, boolean isCritical) throws RemoteException{
        try{
            printBeamErrorWithMessageLn(error);
        }catch(IOException e){}
        exitProgramIfCriticalError(isCritical);
    }
    
    @Override
    public void informAboutException (Exception e, boolean isCritical) throws RemoteException{
        try{
            writer.newLine();
            printBeamErrorWithMessageLn(e.getClass().getName());
            printBeamErrorWithMessageLn(e.getMessage());
            printBeamErrorWithMessageLn("-------> stack trace:");
            for (StackTraceElement element : e.getStackTrace()){
                printBeamErrorWithMessageLn(SPACE + element.toString());
            }
        }catch(IOException ioe){}
        exitProgramIfCriticalError(isCritical);
    }
    
    @Override
    public int chooseVariants(String message, List<String> variants) throws RemoteException{
        int choosed = -1;
        StringJoiner sj;
        try{
            printUnderLn(message);
            for (int i = 0; i < variants.size(); i++){
                sj = new StringJoiner("");
                sj
                        .add(String.valueOf(i+1))
                        .add(" : ")
                        .add(variants.get(i));
                printSpaceLn(sj.toString());
                sj = null;
            } 
            String input;
            while (true){
                printUnder("choose: ");
                input = reader.readLine();
                if (checkOnStop(input)){
                    return -1;
                }
                try{
                    choosed = Integer.parseInt(input);
                    if (0 < choosed && choosed <= variants.size()){
                        break;
                    } else {
                        printUnderLn("Out of variants range.");
                        continue;
                    }
                } catch (NumberFormatException nfe){
                    printUnderLn("Not a number.");
                    continue;
                }
            }
        }catch(IOException ioe){}        
        return choosed;
    }
}
