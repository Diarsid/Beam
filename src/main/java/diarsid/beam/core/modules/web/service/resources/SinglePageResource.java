/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class SinglePageResource extends Resource {
    
    private final WebPagesKeeper webPagesKeeper;
    
    public SinglePageResource(WebPagesKeeper webPagesKeeper) {
        super("/resources/{place}/directories/{dirName}/pages/{pageName}");
        this.webPagesKeeper = webPagesKeeper;
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        String directoryName = webRequest.pathParam("dirName");
        String pageName = webRequest.pathParam("pageName");
        
        WebResponse webResponse = this.webPagesKeeper.getWebPage(place, directoryName, pageName);
        
        webRequest.send(webResponse);
    }
    
    @Override
    protected void DELETE(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        String directoryName = webRequest.pathParam("dirName");
        String pageName = webRequest.pathParam("pageName");
        
        WebResponse webResponse = this.webPagesKeeper.deleteWebPage(place, directoryName, pageName);
        
        webRequest.send(webResponse);
    }
}
