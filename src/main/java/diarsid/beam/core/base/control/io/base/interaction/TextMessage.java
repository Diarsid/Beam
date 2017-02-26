/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;

/**
 *
 * @author Diarsid
 */
public class TextMessage implements Message {
    
    private final MessageType type;
    private final String[] text;
    
    public TextMessage(MessageType type, String... text) {
        this.type = type;
        this.text = text;
    }
    
    public TextMessage(MessageType type, List<String> text) {
        this.type = type;
        this.text = (String[]) text.toArray();
    }
    
    public TextMessage(Exception exception) {
        this.type = ERROR;
        this.text = new String[] {exception.getMessage()};
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

    @Override
    public String[] toText() {
        return this.text;
    }
}