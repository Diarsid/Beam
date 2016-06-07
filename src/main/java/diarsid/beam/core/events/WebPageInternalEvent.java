/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.events;

import diarsid.beam.core.entities.WebPage;

/**
 *
 * @author Diarsid
 */
abstract class WebPageInternalEvent extends BeamEvent {
    
    private static final String pageAttribute = "page";
    
    protected WebPageInternalEvent() {
        super();
    }
    
    protected WebPageInternalEvent withPage(WebPage createdPage) {
        super.with(pageAttribute, createdPage);
        return this;
    }
    
    protected WebPage getPage() {
        return (WebPage) super.get(pageAttribute);
    }
}
