/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Diarsid
 */
public class TimeMessage implements Serializable {
    
    private final String time;
    private final List<String> content;

    public TimeMessage(String time, List<String> content) {
        this.time = time;
        this.content = content;
    }
        
    public String stringifyTime() {
        return this.time;
    }

    public List<String> getContent() {
        return this.content;
    }
}
