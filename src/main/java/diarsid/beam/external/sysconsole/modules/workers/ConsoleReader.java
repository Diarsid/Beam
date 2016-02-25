/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.external.sysconsole.modules.workers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import diarsid.beam.external.sysconsole.modules.ConsoleReaderModule;

/**
 *
 * @author Diarsid
 */
class ConsoleReader implements ConsoleReaderModule {
    
    private final BufferedReader reader;
    
    private final String[] yesPatterns = {"y", "+", "yes", "ye", "true", "enable"};
    private final String[] stopPatterns = {".", "", "s", " ", "-", "false", "disable"};
    private final String[] helpPatterns = {"h", "help", "hlp", "hp"};
    
    ConsoleReader() {
        this.reader = new BufferedReader(System.console().reader());
    }
    
    @Override
    public String read() throws IOException {
        String info = this.reader.readLine().trim().toLowerCase();
        if ( this.checkOnStop(info) ){
            return "";
        } else {
            return info;
        }
    }
    
    private boolean checkOnStop(String input) {
        return check(input, this.stopPatterns);
    }
    
    private boolean check(String s, String[] patterns) {
        for (String pattern : patterns) {
            if (pattern.equals(s)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String readRaw() throws IOException {
        return this.reader.readLine().trim().toLowerCase();
    }
}
