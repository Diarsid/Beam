/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

/**
 *
 * @author Diarsid
 */
public class AllPagesResource extends Resource {
    
    private final WebPagesKeeper webPagesKeeper;
    
    public AllPagesResource(WebPagesKeeper webPagesKeeper) {
        super("resources/{place}/directories/{dirName}/pages");
        this.webPagesKeeper = webPagesKeeper;
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        this.webPagesKeeper.getWebPagesInDirectory(webRequest);
    }
    
    @Override
    protected void POST(WebRequest webRequest) throws IOException {
        this.webPagesKeeper.createWebPage(webRequest);
    }
}
