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
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class SingleDirectoryResource extends Resource {
    
    private final WebDirectoriesKeeper directoriesKeeper;
    
    public SingleDirectoryResource(WebDirectoriesKeeper webDirectoriesKeeper) {
        super("/resources/{place}/directories/{dirName}");
        this.directoriesKeeper = webDirectoriesKeeper;
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        String directoryName = webRequest.pathParam("dirName");
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        
        WebResponse webResponse = this.directoriesKeeper.getWebDirectoryPages(place, directoryName);
        
        webRequest.send(webResponse);
    }
    
    @Override
    protected void DELETE(WebRequest webRequest) throws IOException {
        String directoryName = webRequest.pathParam("dirName");
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        
        WebResponse webResponse = this.directoriesKeeper.deleteWebDirectory(place, directoryName);
        
        webRequest.send(webResponse);
    }
}
