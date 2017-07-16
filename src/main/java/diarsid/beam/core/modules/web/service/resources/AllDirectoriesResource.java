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
        this.directoriesKeeper.getAllDirectoriesInPlace(webRequest);
    }
    
    @Override
    protected void POST(WebRequest webRequest) throws IOException {
        this.directoriesKeeper.createWebDirectory(webRequest);
    }
}
