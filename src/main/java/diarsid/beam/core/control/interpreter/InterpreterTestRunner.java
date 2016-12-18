/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import diarsid.beam.core.control.commands.Command;

import static diarsid.beam.core.control.commands.CommandType.EXIT;

/**
 *
 * @author Diarsid
 */
public class InterpreterTestRunner {
    
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    static Interpreter interpreter = new Interpreter();
    
    public InterpreterTestRunner() {
    }
    
    public static void main(String[] args) throws Exception {
        String commandLine;
        Command command;
        boolean work = true;
        while ( work ) {
            System.out.print(" > ");
            commandLine = reader.readLine(); 
            command = interpreter.interprete(commandLine);
            System.out.println(command.getType());
            work = ! command.getType().equals(EXIT);
        }
    }
}
