/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 *
 * @author Diarsid
 */
public class WebDirectoryPages extends WebDirectory {    
    
    private final List<WebPage> pages;
    
    WebDirectoryPages(String name, WebPlace place, int order, List<WebPage> pages) {
        super(name, place, order);        
        this.pages = unmodifiableList(pages);
    }

    public List<WebPage> pages() {
        return this.pages;
    }
}
