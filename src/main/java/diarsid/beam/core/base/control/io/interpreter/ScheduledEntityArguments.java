/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.interpreter;

/**
 *
 * @author Diarsid
 */
public class ScheduledEntityArguments {
    
    private final String time;
    private final String text;
    
    public ScheduledEntityArguments(String time, String text) {
        this.time = time;
        this.text = text;
    }

    public String getTime() {
        return this.time;
    }

    public String getText() {
        return this.text;
    }
}
