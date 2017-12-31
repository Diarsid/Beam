/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;

import diarsid.beam.core.base.control.io.base.interaction.Message;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.control.io.base.interaction.Messages.infoWithHeader;
import static diarsid.beam.core.base.util.JsonUtil.asJson;
import static diarsid.beam.core.base.util.JsonUtil.convertablesAsJsonArray;

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
        List<String> lines = this.pages
                .stream()
                .sorted()
                .map(page -> format("%d) %s ", page.order(), page.name()))
                .collect(toList());
        if ( lines.isEmpty() ) {
            lines.add("(empty)");
        }
        String header = format(
                "%s > %s (order: %d)", this.place().displayName(), this.name(), this.order());
        return infoWithHeader(header, lines);
    }

    @Override
    public String toJson() {
        return asJson(
                "name", this.name(), 
                "place", this.place().name(), 
                "order", String.valueOf(this.order()), 
                "id", String.valueOf(this.id()),
                "pages", convertablesAsJsonArray(this.pages));
    }

    public List<WebPage> pages() {
        return this.pages;
    }
}
