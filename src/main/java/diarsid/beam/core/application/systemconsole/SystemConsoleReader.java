/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import diarsid.beam.core.base.control.io.base.console.ConsoleReader;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Diarsid
 */
class SystemConsoleReader implements ConsoleReader {
    
    private final BufferedReader reader;   
    
    SystemConsoleReader(BufferedReader reader) {
        this.reader = reader;
    }
    
    @Override
    public String readLine() throws IOException {
        return this.reader.readLine().trim();
    }
}
