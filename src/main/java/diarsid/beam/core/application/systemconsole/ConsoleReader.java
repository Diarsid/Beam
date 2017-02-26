/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.systemconsole;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public class ConsoleReader {
    
    private final BufferedReader reader;   
    
    public ConsoleReader(BufferedReader reader) {
        this.reader = reader;
    }
    
    String readLine() throws IOException {
        return this.reader.readLine().trim();
    }
}
