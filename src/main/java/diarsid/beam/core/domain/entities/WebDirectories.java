/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

import java.util.List;

/**
 *
 * @author Diarsid
 */
public class WebDirectories {
    
    private WebDirectories() {
    }
    
    public static WebDirectory newDirectory(String name, WebPlace place) {
        return new WebDirectory(name, place);
    }
    
    public static WebDirectory restoreDirectory(
            String name, WebPlace place, int order) {
        return new WebDirectory(name, place, order);
    }
    
    public static WebDirectoryPages restoreDirectoryPages(
            int id, String name, WebPlace place, int order, List<WebPage> pages) {
        return new WebDirectoryPages(name, place, order, pages);
    }
}
