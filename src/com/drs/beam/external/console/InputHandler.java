/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.external.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public class InputHandler {
    // Fields =============================================================================
    private final Console console;
    
    // Constructor ========================================================================
    InputHandler(Console console) {
        this.console = console;
    }
    
    // Methods ============================================================================
    
    String inputTime() throws IOException{
        String input = "";
        while (true){
            console.printUnder("Input time: ");
            input = console.reader().readLine().trim().toLowerCase();
            if (console.checkOnHelp(input)){
                console.helpWriter().printTimeFormats();
                continue;
            } else {
                break;
            }
        }
        return input;
    }

    String[] inputTask() throws IOException{
        String taskLine;
        ArrayList<String> taskContent = new ArrayList<>();
        // endless input loop until '.' will be entered which means end of task
        taskInput: while (true){
            console.printUnder("Input task line: ");
            taskLine = console.reader().readLine().trim();
            // symbol '.' finishes input and breaks input loop
            if ((".").equals(taskLine)){
                // if user doesn't specify any text
                if (taskContent.size()==0){
                    // it will be filled only with "empty" value and break input loop
                    taskContent.add("empty");
                    break taskInput;
                } else
                    break taskInput;
            } else
                // in other cases entered string is added to the list
                taskContent.add(taskLine);
        }
        return taskContent.toArray(new String[taskContent.size()]);
    }

    String inputTextToDelete() throws IOException{
        String text = "";
        // begin of input loop
        inputText: while (true){
            console.printUnder("Input text to delete: ");
            text = console.reader().readLine().trim();
            // symbol '.' finishes input and breaks input loop, method returns null
            if (text.equals("."))
                break inputText;
            if (text.length()==0)
                continue inputText;
            // if given string satisfied all requirements input loop ends, method returns it's value
            break inputText;
        }
        return text;
    }
    
    List<String> inputCommands() throws IOException{
        List<String> commands = new ArrayList<>();
        String command;
        input: while(true){
            console.printUnder("command: ");
            command = console.reader().readLine().trim().toLowerCase();
            if (console.checkOnStop(command)){
                break input;
            }
            commands.add(command);
        }
        return commands;
    }
}
