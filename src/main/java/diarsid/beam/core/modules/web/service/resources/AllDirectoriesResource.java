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
import diarsid.beam.core.modules.web.core.rest.RestUrlParamsParser;

import static java.util.Objects.nonNull;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import static diarsid.beam.core.domain.entities.WebPlace.argToPlacement;

/**
 *
 * @author Diarsid
 */
public class AllDirectoriesResource extends Resource {
        
    public AllDirectoriesResource(RestUrlParamsParser parser) {
        super(
                "ALL_DIRECTORIES_RESOURCE", 
                "/resources/{placement}/directories", 
                parser);
    }
    
    @Override
    protected void OPTIONS(
            ResourceRequest request, 
            ResourceResponse response)
                throws IOException {
        
    }
    
    @Override
    protected void GET(
            ResourceRequest request, 
            ResourceResponse response)
                throws IOException {
        Optional<String> optPlace = request.getParam("placement");
        if ( optPlace.isPresent() ) {
            WebPlace place = argToPlacement(optPlace.get());
            if ( nonNull(place) ) {
                response.writeOkJson(this.getJsonDirectoriesFrom(place));
            } else {
                response.writeErrorJson(json, SC_BAD_REQUEST);
            }            
        } else {
            response.writeErrorJson(SC_BAD_REQUEST);
        }
    }
    
    private String getJsonDirectoriesFrom(WebPlace placement) {
        return "fake";
    }
    
    @Override
    protected void POST(
            ResourceRequest request, 
            ResourceResponse response)
                throws IOException {
        
    }
}
