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
public interface Message extends Serializable {
    
    public static enum MessageType implements Serializable {
        INFO,
        ERROR
    }
    
    List<String> toText();
    
    MessageType getType();
}
