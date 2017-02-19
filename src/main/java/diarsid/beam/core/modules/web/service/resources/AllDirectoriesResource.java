/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;
import java.util.Optional;

import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.web.core.container.Resource;
import diarsid.beam.core.modules.web.core.container.ResourceRequest;
import diarsid.beam.core.modules.web.core.container.ResourceResponse;

import static java.util.Objects.nonNull;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.modules.web.core.jsonconversion.JsonUtil.errorJson;

/**
 *
 * @author Diarsid
 */
public class AllDirectoriesResource extends Resource {
        
    public AllDirectoriesResource() {
        super("/resources/{place}/directories");
    }
    
    @Override
    protected void OPTIONS(ResourceRequest request, ResourceResponse response) throws IOException {
        
    }
    
    @Override
    protected void GET(ResourceRequest request, ResourceResponse response) throws IOException {
        Optional<String> optPlace = request.getParam("place");
        if ( optPlace.isPresent() ) {
            WebPlace place = parsePlace(optPlace.get());
            if ( nonNull(place) ) {
                response.okWithJson(this.getJsonDirectoriesFrom(place));
            } else {
                response.badRequestWithJson(errorJson("web place is not specified."));
            }            
        } else {
            response.badRequestWithJson(errorJson("web place is not specified."));
        }
    }
    
    private String getJsonDirectoriesFrom(WebPlace placement) {
        return "fake";
    }
    
    @Override
    protected void POST(ResourceRequest request, ResourceResponse response) throws IOException {
        
    }
}
