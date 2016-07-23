/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import diarsid.beam.core.entities.global.WebPage;

/**
 *
 * @author Diarsid
 */
public class WebPageCreatedEvent extends WebPageEvent {
    
    private final static String CREATED_PAGE = "created_page_attribute";   
    
    protected WebPageCreatedEvent() {
        super();
    }
    
    public static PrecompiledEvent<WebPageCreatedEvent> createWith(WebPage page) {
        WebPageCreatedEvent event = new WebPageCreatedEvent();
        event.set(CREATED_PAGE, page);                
        return new PrecompiledEvent(event);
    }
    
    @Override
    public WebPage getCause() {
        return (WebPage) super.get(CREATED_PAGE);
    }
}
