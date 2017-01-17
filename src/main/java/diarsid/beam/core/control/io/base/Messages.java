/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.control.io.base;

import java.util.List;

import static diarsid.beam.core.control.io.base.Message.MessageType.ERROR;
import static diarsid.beam.core.control.io.base.Message.MessageType.INFO;

/**
 *
 * @author Diarsid
 */
public class Messages {
    
    private Messages() {
    }
    
    public static Message error(String... lines) {
        return new TextMessage(ERROR, lines);
    }
    
    public static Message text(String... lines) {
        return new TextMessage(INFO, lines);
    }
    
    public static Message text(List<String> lines) {
        return new TextMessage(INFO, lines);
    }
    
    public static Message fromException(Exception e) {
        return new TextMessage(e);
    }
}
