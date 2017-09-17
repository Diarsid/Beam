/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import diarsid.beam.core.base.control.io.commands.Command;

import static diarsid.beam.core.base.control.io.commands.CommandType.EXIT;
import static diarsid.beam.core.base.control.io.commands.EmptyCommand.undefinedCommand;

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
        Command command = undefinedCommand();
        while ( command.type().isNot(EXIT) ) {
            System.out.print(" > ");
            commandLine = reader.readLine(); 
            command = interpreter.interprete(commandLine);
            System.out.println(command.type());
        }
    }
}
