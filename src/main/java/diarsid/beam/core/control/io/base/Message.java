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
public interface Message extends Serializable {
    
    public static enum MessageType implements Serializable {
        INFO,
        ERROR
    }
    
    String[] toText();
    
    MessageType getType();
}
