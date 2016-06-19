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
public class WebPageCreatedInternally extends WebPageInternalEvent {
    
    protected WebPageCreatedInternally() {
        super();
    }
    
    public static WebPageCreatedInternally newEvent() {
        return new WebPageCreatedInternally();
    }
    
    @Override
    public WebPageCreatedInternally withPage(WebPage createdPage) {
        super.withPage(createdPage);
        return this;
    }
    
    public WebPage getPage() {
        return super.getPage();
    }
}
