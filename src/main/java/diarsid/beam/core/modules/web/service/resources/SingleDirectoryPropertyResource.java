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

import static diarsid.beam.core.base.control.io.base.interaction.WebResponse.badRequestWithJson;
import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;
import static diarsid.beam.core.domain.entities.validation.Validation.asName;
import static diarsid.beam.core.domain.entities.validation.Validation.asNames;
import static diarsid.beam.core.domain.entities.validation.Validation.asOrder;
import static diarsid.beam.core.domain.entities.validation.Validation.validate;

/**
 *
 * @author Diarsid
 */
public class SingleDirectoryPropertyResource extends Resource {
    
    private final WebDirectoriesKeeper directoriesKeeper;
    
    public SingleDirectoryPropertyResource(WebDirectoriesKeeper directoriesKeeper) {
        super("/{place}/directories/{dirName}/{property}");
        this.directoriesKeeper = directoriesKeeper;
    }
    
    @Override
    protected void PUT(WebRequest webRequest) throws IOException {
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        String directoryName = webRequest.pathParam("dirName");
        String property = webRequest.pathParam("property");
        Json json = webRequest.json();

        WebResponse webResponse;
        switch ( property ) {
            case "name" : {
                String newDirectoryName = json.stringOf("payload");
                validate(place, asNames(newDirectoryName, directoryName));
                webResponse = this.directoriesKeeper
                        .editWebDirectoryName(place, directoryName, newDirectoryName);
                break;
            }
            case "place" : {
                WebPlace newPlace = parsePlace(json.stringOf("payload"));
                validate(place, newPlace, asName(directoryName));
                webResponse = this.directoriesKeeper
                        .editWebDirectoryPlace(place, directoryName, newPlace);
                break;
            }
            case "order" : {
                int newOrder = json.intOf("payload");
                validate(place, asName(directoryName), asOrder(newOrder));
                webResponse = this.directoriesKeeper
                        .editWebDirectoryOrder(place, directoryName, newOrder);
                break;
            }
            default : {
                webResponse = badRequestWithJson("directory property is not defined!");
            }
        }
        
        webRequest.send(webResponse);
    }
}
