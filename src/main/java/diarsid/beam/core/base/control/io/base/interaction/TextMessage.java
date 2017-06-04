/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

import static java.util.Arrays.asList;

import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.ERROR;
import static diarsid.beam.core.base.control.io.base.interaction.Message.MessageType.INFO;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;

/**
 *
 * @author Diarsid
 */
public class TextMessage implements Message {
    
    private final MessageType type;
    private final List<String> text;
    
    public TextMessage(String text1, String... text) {
        this.type = INFO;
        this.text = arrayListOf(text);
        this.text.add(0, text1);
    }
    
    public TextMessage(List<String> text) {
        this.type = INFO;
        this.text = text;
    }
    
    public TextMessage(MessageType type, String text1, String... text) {
        this.type = type;
        this.text = arrayListOf(text);
        this.text.add(0, text1);
    }
    
    public TextMessage(MessageType type, List<String> text) {
        this.type = type;
        this.text = text;
    }
    
    public TextMessage(Exception exception) {
        this.type = ERROR;
        this.text = asList(exception.getMessage());
    }

    @Override
    public Message addHeader(String header) {
        for (int i = 0; i < this.text.size(); i++) {
            this.text.set(i, this.inline(this.text.get(i)));
        }
        this.text.add(0, header);
        return this;
    }

    private String inline(String line) {
        return "   " + line;
    }

    @Override
    public MessageType getType() {
        return this.type;
    }

    @Override
    public List<String> toText() {
        return this.text;
    }
}
