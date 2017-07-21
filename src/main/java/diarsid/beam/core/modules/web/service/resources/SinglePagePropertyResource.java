/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.Json;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class SinglePagePropertyResource extends Resource {
    
    private final WebPagesKeeper webPagesKeeper;
    
    public SinglePagePropertyResource(WebPagesKeeper webPagesKeeper) {
        super("/resources/{place}/directories/{dirName}/pages/{pageName}/{property}");
        this.webPagesKeeper = webPagesKeeper;
    }
    
    @Override
    protected void PUT(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        String directoryName = webRequest.pathParam("dirName");
        String pageName = webRequest.pathParam("pageName");
        String property = webRequest.pathParam("property");
        Json json = webRequest.json();
        
        WebResponse webResponse;
        switch ( property ) {
            case "name" : {
                String pageNewName = json.stringOf("payload");
                webResponse = this.webPagesKeeper
                        .editWebPageName(place, directoryName, pageName, pageNewName);
                break;
            }
            case "url" : {
                String pageUrl = json.stringOf("payload");
                webResponse = this.webPagesKeeper
                        .editWebPageUrl(place, directoryName, pageName, pageUrl);
                break;
            }
            case "order" : {
                int newOrder = json.intOf("payload");
                webResponse = this.webPagesKeeper
                        .editWebPageOrder(place, directoryName, pageName, newOrder);
                break;
            }
            case "directory" : {
                String newDirectoryName = json.stringOf("payload");
                webResponse = this.webPagesKeeper
                        .editWebPageDirectory(place, directoryName, pageName, newDirectoryName);
                break;
            }
            case "place-and-directory" : {
                String newDirectoryName = json.stringOf("dir");
                WebPlace newPlace = parsePlace(json.stringOf("place"));
                webResponse = this.webPagesKeeper
                        .editWebPageDirectoryAndPlace(
                                place, directoryName, pageName, newPlace, newDirectoryName);
                break;
            }
            default : {
                webResponse = badRequestWithJson("page property is not defined!");
            }
        }
        
        webRequest.send(webResponse);
    }
    
}
