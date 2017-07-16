/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;

import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.modules.domainkeeper.WebDirectoriesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static diarsid.beam.core.base.util.JsonUtil.errorJson;

/**
 *
 * @author Diarsid
 */
public class SingleDirectoryPropertyResource extends Resource {
    
    private final WebDirectoriesKeeper directoriesKeeper;
    
    public SingleDirectoryPropertyResource(WebDirectoriesKeeper directoriesKeeper) {
        super("resources/{place}/directories/{name}/{property}");
        this.directoriesKeeper = directoriesKeeper;
    }
    
    @Override
    protected void PUT(WebRequest webRequest) throws IOException {        
        switch ( webRequest.pathParam("property") ) {
            case "name" : {
                this.directoriesKeeper.editWebDirectoryName(webRequest);
                break;
            }
            case "placement" : {
                this.directoriesKeeper.editWebDirectoryPlace(webRequest);
                break;
            }
            case "order" : {
                this.directoriesKeeper.editWebDirectoryOrder(webRequest);
                break;
            }
            default : {
                webRequest.sendBadRequestWithJson(errorJson("directory property is empty!"));
            }
        }
    }
}
