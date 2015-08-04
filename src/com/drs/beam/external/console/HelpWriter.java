/*
 * project: Beam
 * author: Diarsid
 */

package com.drs.beam.external.console;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author Diarsid
 */
public class HelpWriter {
    // Fields =============================================================================
    private final Console console;
    
    private final String HELP_START =   "   +---- HELP ------------------------------------------";
    private final String HELP =         "   |  ";
    private final String HELP_END =     "   +----------------------------------------------------";
    
    // Constructors =======================================================================
    public HelpWriter(Console console) {
        this.console = console;
    }
    
    // Methods ============================================================================
    
    void printTimeFormats() throws IOException{
        String[] timeFormats = {
            "Possible time formats:",
            "+MM              - timer, minutes",
            "+HH:MM           - timer, houres and minutes",
            "HH:MM            - today task",
            "DD HH:MM         - this month task",
            "DD-MM HH:MM      - this year task",
            "YYYY-MM-DD HH:MM - full date format"
        };
        printHelpText(timeFormats);
    }
    
    private void printHelpText(String[] text) throws IOException{
        BufferedWriter writer = console.writer();
        writer.newLine();
        writer.write(HELP_START);
        writer.newLine();
        writer.flush();
        for (String line : text){
            writer.write(HELP+line);
            writer.newLine();
            writer.flush();
        }
        writer.write(HELP_END);
        writer.newLine();
        writer.newLine();
        writer.flush();
    }

}
