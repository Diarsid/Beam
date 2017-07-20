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
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class AllDirectoriesResource extends Resource {
    
    private final WebDirectoriesKeeper directoriesKeeper;
        
    public AllDirectoriesResource(WebDirectoriesKeeper directoriesKeeper) {
        super("/resources/{place}/directories");
        this.directoriesKeeper = directoriesKeeper;
    }
    
    @Override
    protected void OPTIONS(WebRequest webRequest) throws IOException {
        
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        
        WebResponse webResponse = this.directoriesKeeper.getAllDirectoriesInPlace(place);
        
        webRequest.send(webResponse);
    }
    
    @Override
    protected void POST(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        Json json = webRequest.json();
        String name = json.stringOf("name");
        
        WebResponse webResponse = this.directoriesKeeper.createWebDirectory(place, name);
        
        webRequest.send(webResponse);
    }
}
