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

/**
 *
 * @author Diarsid
 */
public class SingleDirectoryResource extends Resource {
    
    private final WebDirectoriesKeeper directoriesKeeper;
    
    public SingleDirectoryResource(WebDirectoriesKeeper webDirectoriesKeeper) {
        super("resources/{place}/directories/{name}");
        this.directoriesKeeper = webDirectoriesKeeper;
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        this.directoriesKeeper.getWebDirectoryPages(webRequest);
    }
    
    @Override
    protected void DELETE(WebRequest webRequest) throws IOException {
        this.directoriesKeeper.deleteWebDirectory(webRequest);
    }
}
