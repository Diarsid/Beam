/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.service.resources;

import java.io.IOException;
import java.lang.reflect.Method;

import diarsid.beam.core.base.control.io.base.interaction.Json;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;
import diarsid.beam.core.domain.entities.WebPlace;
import diarsid.beam.core.modules.domainkeeper.WebPagesKeeper;
import diarsid.beam.core.modules.web.core.container.Resource;

import static java.util.Arrays.stream;

import static diarsid.beam.core.domain.entities.WebPlace.parsePlace;

/**
 *
 * @author Diarsid
 */
public class AllPagesResource extends Resource {
    
    private final WebPagesKeeper webPagesKeeper;
    
    public AllPagesResource(WebPagesKeeper webPagesKeeper) {
        super("/resources/{place}/directories/{dirName}/pages");
        this.webPagesKeeper = webPagesKeeper;
    }
    
    @Override
    protected void GET(WebRequest webRequest) throws IOException {
        String directoryName = webRequest.pathParam("dirName");
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        
        WebResponse webResponse = this.webPagesKeeper.getWebPagesInDirectory(
                place, directoryName);
        
        webRequest.send(webResponse);
    }
    
    @Override
    protected void POST(WebRequest webRequest) throws IOException {
        Json json = webRequest.json();
        String name = json.stringOf("name");
        String url = json.stringOf("url");
        String directoryName = webRequest.pathParam("dirName");
        WebPlace place = parsePlace(webRequest.pathParam("place"));
        
        WebResponse webResponse = this.webPagesKeeper.createWebPage(
                place, directoryName, name, url);
        
        webRequest.send(webResponse);
    }
    
    public static void main(String[] args) {
        AllPagesResource res = new AllPagesResource(null);
        Method[] methods = res.getClass().getDeclaredMethods();
        stream(methods).forEach(method -> System.out.println(method.getName()));
    }
}
