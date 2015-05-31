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
    
    private final String ORG =      "Beam > ";
    private final String ERROR =    "Beam error > ";
    private final String SPACE =    "       ";
    
    private TaskManagerIF taskManager;
    private ExecutorIF osExecutor;
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
        this.osExecutor = osExecutor;
    }

    void setOrgIO(OrgIOIF orgIO) {
        this.orgIO = orgIO;
    }
    
    BufferedReader reader(){
        return reader;
    }
    
    public void close(){
        try{
            orgIO.setDefaultIO();
            System.exit(0);
        } catch(RemoteException e){}        
    }
    
    public void exitProgram(){
        try{            
            orgIO.exit();
            System.exit(0);
        } catch(RemoteException e){}  
    }
    
    private void exitIfCritical(boolean isCritical){
        if(isCritical){
            try{
                Thread.sleep(5000);
                orgIO.exit();
                System.exit(0);
            } catch(InterruptedException|RemoteException ie){}
        }
    }
    
    @Override
    public void run(){
        String timeBuffer;
        String stringBuffer;
        String[] strArrBuffer;
        try{
            // endless loop to await commands
            input: while (true){
                timeBuffer = null;
                stringBuffer = null;
                strArrBuffer = null;
                this.print();
                // switch operator
                // obtain new string as command to choose between cases
                command:
                switch (reader.readLine().trim()){
                    // command to close console
                    case "close" :{
                        close();
                    }
                    // command to view the earliest task's time
                    case "alarm" :{
                        printLn(taskManager.getFirstAlarmTime());
                        break command;
                    }
                    // command to create new task
                    case "new" :{
                        // input new task's time
                        timeBuffer = input.inputTime();
                        // stop creation if time input was incorrect or stopped
                        if ( ".".equals(timeBuffer) || "".equals(timeBuffer))
                            break command;
                        strArrBuffer = input.inputTask();
                        taskManager.createNewTask(timeBuffer, strArrBuffer);                        
                        break command;
                    }
                    // test whether everything is ok and console works
                    case "listener" :{
                        this.printLn("listener ready");
                        break command;
                    }
                    // print all future active tasks
                    case ("tasks") :{
                        writer.write(SPACE);
                        writer.write("========= Tasks =========");
                        writer.newLine();
                        printTasks(taskManager.getFutureTasks());
                        writer.newLine();
                        writer.write(SPACE);
                        writer.write("=========================");
                        writer.newLine();
                        writer.flush();
                        break command;
                    }
                    // print all past tasks
                    case ("past") :{
                        writer.write(SPACE);
                        writer.write("========= Past ==========");
                        writer.newLine();
                        printTasks(taskManager.getPastTasks());
                        writer.newLine();
                        writer.write(SPACE);
                        writer.write("=========================");
                        writer.newLine();
                        writer.flush();
                        break command;
                    }
                    // deletion of some task
                    case ("delete") :{                        
                        stringBuffer = input.inputTextToDelete();
                        // stop deletion if text input was incorrect or stopped
                        if ( stringBuffer == null )
                            break command;
                        // perform deletion
                        if (taskManager.deleteTaskByText(stringBuffer))
                            printLn("Task has been deleted.");                        
                        break command;
                    }
                    case ("exit") :{
                        printLn("Do you really want to stop me?");
                        print("y/n > ");
                        stringBuffer = reader.readLine().trim();
                        if (stringBuffer.equals("y") || stringBuffer.equals("yes")){
                            writer.write(" :(");
                            writer.newLine();
                            writer.write("will meet you another time...");
                            writer.flush();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {
                            }
                            exitProgram();
                        }  else
                            break command;
                    }
                    case ("run") : {
                        //
                        break command;
                    }
                    case ("r") : {
                        //
                        break command;
                    }
                    case ("open") : {
                        //
                        break command;
                    }
                    case ("o") : {
                        //
                        break command;
                    }
                    case ("install") : {
                        //
                        break command;
                    }
                    case ("i") : {
                        //
                        break command;
                    }
                    case ("use native task showing") : {
                        orgIO.useNativeShowTaskMethod();
                        break command;
                    }
                    case ("use external task showing") : {
                        orgIO.useExternalShowTaskMethod();
                        break command;
                    }
                }
            }
        } catch (IOException e) {}
    }

    // Console`s methods to format it's output --------------------------------------------

    void print() throws IOException{
            writer.write(ORG);
            writer.flush();
    }
    
    void print(String s) throws IOException{
            writer.write(ORG);
            writer.write(s);
            writer.flush();
    }
    
    void printLn(String s) throws IOException{
            writer.write(ORG);
            writer.write(s);
            writer.newLine();
            writer.flush();
    }
    
    void printLnError(String s) throws IOException{
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
            print();
        } catch(IOException e){}
    }
    
    @Override
    public void isActive() throws RemoteException{
    }
    
    @Override
    public void informAbout(String info) throws RemoteException{
        try{
            writer.newLine();
            printLn(info);
            writer.write(ORG);
            writer.flush();
        }catch(IOException e){}
    }
    
    @Override
    public void informAboutError(String error, boolean isCritical) throws RemoteException{
        try{
            writer.newLine();
            printLnError(error);
            writer.write(ORG);
            writer.flush();
        }catch(IOException e){}
        exitIfCritical(isCritical);
    }
    
    @Override
    public void informAboutException (Exception e, boolean isCritical) throws RemoteException{
        try{
            writer.newLine();
            printLnError(e.getMessage());
            printLnError("-------> stack trace:");
            for (StackTraceElement element : e.getStackTrace()){
                printLnError(SPACE + element.toString());
            }
            writer.write(ORG);
            writer.flush();
        }catch(IOException ioe){}
        exitIfCritical(isCritical);
    }
}
