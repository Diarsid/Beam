/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.io.Serializable;

import static diarsid.beam.core.control.io.base.TextMessage.IoMessageType.ERROR;

/**
 *
 * @author Diarsid
 */
public class TextMessage implements Serializable {
    
    public static enum IoMessageType implements Serializable {
        NORMAL,
        ERROR
    }
    
    private final IoMessageType type;
    private final String[] text;
    
    public TextMessage(IoMessageType type, String... text) {
        this.type = type;
        this.text = text;
    }
    
    public TextMessage(Exception exception) {
        this.type = ERROR;
        this.text = new String[] {exception.getMessage()};
    }

    public IoMessageType getType() {
        return type;
    }

    public String[] getText() {
        return text;
    }
}
