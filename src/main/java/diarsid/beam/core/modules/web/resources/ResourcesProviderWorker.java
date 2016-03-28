/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;

import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.handlers.WebPagesHandler;
import diarsid.beam.core.modules.web.ResourcesProvider;
import diarsid.beam.core.modules.web.ServletData;

import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.ALL_PAGES_IN_DIR_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.DIR_FIELDS_FROM_DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.DIR_FROM_DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.PAGE_FIELDS_FROM_DIR_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResourcesForWebPages.PAGE_FROM_DIR_IN_PLACEMENT;

/**
 *
 * @author Diarsid
 */
public class ResourcesProviderWorker implements ResourcesProvider {
    
    private final HandlerManagerModule handlers;
    private final Set<ServletData> servlets;
    
    public ResourcesProviderWorker(HandlerManagerModule handlers) {
        this.handlers = handlers;
        this.servlets = new HashSet<>();
        
        this.servlets.add(new ServletData(
                new DispatcherServlet(), 
                "dispatcher", 
                "/*"));
        
        this.assembleWebPageServlets();
    }
    
    private void assembleWebPageServlets() {  
                
        this.servlets.add(DIRS_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));        
        
        this.servlets.add(DIR_FROM_DIRS_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));        
        
        this.servlets.add(ALL_PAGES_IN_DIR_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));        
        
        this.servlets.add(PAGE_FROM_DIR_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));        
        
        this.servlets.add(PAGE_FIELDS_FROM_DIR_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));        
        
        this.servlets.add(DIR_FIELDS_FROM_DIRS_IN_PLACEMENT
                .resourceServletData(this.newHandler(), this.newPathResolver()));
    }
    
    private WebPagesHandler newHandler() {
        return this.handlers.getWebPagesHandler();
    }
    
    private PathResolver newPathResolver() {
        return new PathResolver();
    }
    
    @Override
    public Set<ServletData> getServlets() {
        return this.servlets;
    }
    
    @Override
    public Set<Filter> getFilters() {
        return null;
    }
}
