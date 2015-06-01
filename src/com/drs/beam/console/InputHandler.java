/*
 * project: Beam
 * author: Diarsid
 */
package com.drs.beam.console;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Diarsid
 */
public class InputHandler {
    // Fields =============================================================================
    private final Console console;
    
    // Constructor ========================================================================
    public InputHandler(Console console) {
        this.console = console;
    }
    
    // private console methods to handle with it's data input ----------------------------------------------------------
    String inputTime() throws IOException{
        // different time formats which may be entered and parsed by program
        console.printBeamWithMessageLn("Input time: YYYY-MM-DD HH:MM \r\n" +
          "                  +HH:MM       - timer \r\n" +
          "                  HH:MM        - today task \r\n" +
          "                  dd HH:MM     - this month task \r\n" +
          "                  dd-mm HH:MM  - this year task");
        console.printBeamWithMessage("Input time: ");
        return console.reader().readLine().trim().toLowerCase();
    }

    String[] inputTask() throws IOException{
        String taskLine;
        ArrayList<String> taskContent = new ArrayList<>();
        // endless input loop until '.' will be entered which means end of task
        taskInput: while (true){
            console.printBeamWithMessage("Input task line: ");
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
            console.printBeamWithMessage("Input text to delete: ");
            text = console.reader().readLine().trim();
            // symbol '.' finishes input and breaks input loop, method returns null
            if (text.equals("."))
                break inputText;
            if (text.length()==0)
                // if nothing was entered, input continues
                continue inputText;
            // if given string satisfied all requirements input loop ends, method returns it's value
            break inputText;
        }
        return text;
    }
}
