/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;

/**
 *
 * @author Diarsid
 */
public class TimeMessage implements Serializable {
    
    private final String time;
    private final String[] content;

    public TimeMessage(String time, String[] content) {
        this.time = time;
        this.content = content;
    }
        
    public String stringifyTime() {
        return this.time;
    }

    public String[] getContent() {
        return this.content;
    }
}
