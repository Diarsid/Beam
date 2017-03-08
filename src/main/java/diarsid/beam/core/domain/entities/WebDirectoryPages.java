/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;
import diarsid.beam.core.base.control.io.base.interaction.TextMessage;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class WebDirectoryPages extends WebDirectory {    
    
    private final List<WebPage> pages;
    
    WebDirectoryPages(int id, String name, WebPlace place, int order, List<WebPage> pages) {
        super(id, name, place, order);        
        this.pages = unmodifiableList(pages);
    }

    @Override
    public Message toMessage() {
        List<String> message = this.pages
                .stream()
                .map(page -> format("  - %s (%d)", page.name(), page.order()))
                .collect(toList());
        message.add(0, format(
                "%s (%d) %s :", this.name(), this.order(), lower(this.place().name())));
        return new TextMessage(message);
    }

    public List<WebPage> pages() {
        return this.pages;
    }
}
