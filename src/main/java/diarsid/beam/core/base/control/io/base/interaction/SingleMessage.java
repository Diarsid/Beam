/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.control.io.base.interaction;

import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.inline;
import static diarsid.beam.core.base.util.CollectionsUtils.arrayListOf;

/**
 *
 * @author Diarsid
 */
class SingleMessage implements Message {
    
    private final MessageType type;
    private final List<String> text;
    private String header;
    
    SingleMessage(MessageType type, String header, String... text) {
        this.type = type;
        this.header = header;
        this.text = arrayListOf(text);
    }
    
    SingleMessage(MessageType type, String header, List<String> text) {
        this.type = type;
        this.header = header;
        this.text = text;
    }
    
    @Override
    public boolean hasHeader() {
        return nonNull(this.header) && ! this.header.isEmpty();
    }

    @Override
    public Message addHeader(String header) {
        this.header = header;
        return this;
    }

    @Override
    public MessageType type() {
        return this.type;
    }
    
    @Override
    public List<String> lines() {
        return this.text;        
    }

    @Override
    public List<String> allLines() {
        if ( this.hasHeader() ) {
            List<String> allLines = this.text
                    .stream()
                    .map(line -> inline(line))
                    .collect(toList());

            allLines.add(0, this.header);
            return allLines;
        } else {
            return this.text;
        }        
    }

    @Override
    public String header() {
        return this.header;
    }
}
