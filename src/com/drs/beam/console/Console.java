/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.console;

import com.drs.beam.console.Console;
import com.drs.beam.remote.codebase.ExternalIOIF;
import com.drs.beam.remote.codebase.ExecutorIF;
import com.drs.beam.remote.codebase.OrgIOIF;
import com.drs.beam.remote.codebase.TaskManagerIF;
import com.drs.beam.tasks.Task;
import java.io.*;
import java.rmi.RemoteException;
import java.util.List;

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
    
    private final String BEAM =     "Beam > ";
    private final String ERROR =    "Beam error > ";
    private final String SPACE =    "       ";
    private final String UNDER =    "     > ";
    private final String[] yesPatterns = {"y", "+", "yes", "ye"};
    private final String[] stopPatterns = {".", "", "s", " "};
    
    private TaskManagerIF taskManager;
    private ExecutorIF executor;
    private OrgIOIF orgIO;
    
    // Constructor ========================================================================
    public Console() {
        this.input = new InputHandler(this);
    }    
    
    // Methods ============================================================================
    
    public static void main(String[] args) {
        Console console = new Console();
        ConsoleManager manager = new ConsoleManager(console);
        manager.connect();
        new Thread(console, "Org_console").start();
    }    
    
    void setTaskManager(TaskManagerIF taskManager) {
        this.taskManager = taskManager;
    }

    void setOsExecutor(ExecutorIF osExecutor) {
        this.executor = osExecutor;
    }

    void setOrgIO(OrgIOIF orgIO) {
        this.orgIO = orgIO;
    }
    
    BufferedReader reader(){
        return reader;
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
                System.exit(0);
            } catch(InterruptedException|RemoteException e){}
        }
    }
    
    private void exitDialog() throws IOException{
        printBeamWithMessageLn("Do you really want to stop me?");
        printUnder("yes / no : ");
        String com = reader.readLine().trim().toLowerCase();
        if (check(com, yesPatterns)){
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
        try{            
            input: while (true){
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
                    case "new" : {
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
                            case ("location") : {
                                newLocation();
                                break parsing;
                            }
                            case ("task") : {
                                newTask();
                                break parsing;
                            }
                            case ("event") : {
                                newEvent();
                                break parsing;
                            }
                            case ("command") : {
                                newCommand();
                                break parsing;
                            }
                        }
                    }
                    case "alarm" :{
                        printBeamWithMessageLn(taskManager.getFirstAlarmTime());
                        break parsing;
                    }                    
                    case "run" : {
                        executor.run(command); 
                        break parsing;
                    }
                    case "open" : {
                        executor.open(command);
                        break parsing;
                    }
                    case "call" : {
                        executor.call(command);
                        break parsing;
                    }
                    case "view" : {
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
                            case "tasks" : {
                                writer.write(SPACE);
                                writer.write("========= Tasks =========");
                                writer.newLine();
                                printTasks(taskManager.getFutureTasks());
                                writer.newLine();
                                writer.write(SPACE);
                                writer.write("=========================");
                                writer.newLine();
                                writer.flush();
                                break parsing;
                            }
                            case "past" : {
                                writer.write(SPACE);
                                writer.write("========= Past ==========");
                                writer.newLine();
                                printTasks(taskManager.getPastTasks());
                                writer.newLine();
                                writer.write(SPACE);
                                writer.write("=========================");
                                writer.newLine();
                                writer.flush();
                                break parsing;
                            }
                            case "locations" : {
                                
                                break parsing;
                            }
                            case "commands" : {
                                
                                break parsing;
                            }
                        }                        
                    }
                    case "del" :{
                        if (params.length < 2){
                            continue input;
                        }                        
                        switch (params[1]){
                            case "task" : {
                                deleteTask();
                                break parsing;
                            }
                            case "past" : {
                                deleteTask();
                                break parsing;
                            }
                            case "event" : {
                                deleteEvent();
                                break parsing;
                            }
                            case "location" : {
                                deleteLocation();
                                break parsing;
                            }
                            case "command" : {
                                deleteCommand();
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
                                break parsing;
                            }
                            case "external" : {
                                orgIO.useExternalShowTaskMethod();
                                break parsing;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {}
        command = "";
        params = null;
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
    
    void printBeamErrorWithMessageLn(String s) throws IOException{
            writer.write(ERROR);
            writer.write(s);
            writer.newLine();
            writer.flush();
    }
    
    void printTasks(List<Task> tasks) throws IOException{
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
    }
    
    private void printHelp(){
        
    }     
    
    private boolean check(String s, String[] patterns){
        for (String pattern : patterns){
            if (pattern.equals(s)) return true;
        }
        return false;
    }
    
    private void newLocation() throws IOException{        
        printUnder("set name: ");
        String name = reader.readLine().trim().toLowerCase();
        if (check(name, stopPatterns)) return;
        printUnder("set path: ");
        String location = reader.readLine().trim().toLowerCase();
        if (check(location, stopPatterns)) return;
        executor.newLocation(location, name);
    }
    
    private void newTask() throws IOException{        
        String newTime = input.inputTime();
        if (check(newTime, stopPatterns)) return;
        String[] newTask = input.inputTask();
        taskManager.createNewTask(newTime, newTask);
    }
    
    private void newEvent() throws IOException{
        
    }
    
    private void newCommand() throws IOException{
        
    }
    
    private void deleteLocation() throws IOException{
        
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
            writer.flush();
        }catch(IOException e){}
    }
    
    @Override
    public void informAboutError(String error, boolean isCritical) throws RemoteException{
        try{
            printBeamErrorWithMessageLn(error);
            writer.flush();
        }catch(IOException e){}
        exitProgramIfCriticalError(isCritical);
    }
    
    @Override
    public void informAboutException (Exception e, boolean isCritical) throws RemoteException{
        try{
            writer.newLine();
            printBeamErrorWithMessageLn(e.getMessage());
            printBeamErrorWithMessageLn("-------> stack trace:");
            for (StackTraceElement element : e.getStackTrace()){
                printBeamErrorWithMessageLn(SPACE + element.toString());
            }
            writer.write(BEAM);
            writer.flush();
        }catch(IOException ioe){}
        exitProgramIfCriticalError(isCritical);
    }
}
