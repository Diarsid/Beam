/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.domain.entities;

/**
 *
 * @author Diarsid
 */
public class WebPages {
    
    private WebPages() {
    }
    
    public static WebPage newWebPage(
            String name, 
            String shortcuts,             
            String url, 
            WebPlace place,
            int directoryId,
            String directoryName) {        
        return new WebPage(name, shortcuts, url, place, directoryId, directoryName);
    }
    
    public static WebPage restorePage(
            String name, 
            String shortcuts,             
            String url, 
            WebPlace place,
            int directoryId,
            String directoryName,
            int pageOrder,
            int directoryOrder,
            String browser) {        
        return new WebPage(
                name, 
                shortcuts, 
                url, 
                pageOrder, 
                place, 
                directoryId, 
                directoryName, 
                directoryOrder);
    }
}
