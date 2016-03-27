/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import diarsid.beam.core.modules.HandlerManagerModule;
import diarsid.beam.core.modules.handlers.WebPagesHandler;
import diarsid.beam.core.modules.web.ResourcesProvider;
import diarsid.beam.core.modules.web.ServletData;

import static diarsid.beam.core.modules.web.resources.RestResources.ALL_PAGES_IN_DIR_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.DIR_FIELDS_FROM_DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.DIR_FROM_DIRS_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.PAGE_FIELDS_FROM_DIR_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.PAGE_FROM_DIR_IN_PLACEMENT;
import static diarsid.beam.core.modules.web.resources.RestResources.ROOT;

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
        this.assembleServlets();
    }
    
    private void assembleServlets() {
        
        HttpServlet restRootPathDispatcherServlet = new DispatcherServlet();
        this.servlets.add(new ServletData(
                restRootPathDispatcherServlet, 
                ROOT.servletName(), 
                ROOT.servletMapping()));
        
        HttpServlet directoriesServlet = new AllDirectoriesServlet(
                this.produceHandler(),
                this.producePathResolver()
                );
        this.servlets.add(new ServletData(
                directoriesServlet, 
                DIRS_IN_PLACEMENT.servletName(), 
                DIRS_IN_PLACEMENT.servletMapping()));
        
        HttpServlet singleDirServlet = new SingleDirectoryServlet(
                this.produceHandler(),
                this.producePathResolver()
                );
        this.servlets.add(new ServletData(
                singleDirServlet, 
                DIR_FROM_DIRS_IN_PLACEMENT.servletName(), 
                DIR_FROM_DIRS_IN_PLACEMENT.servletMapping()));
        
        HttpServlet pageInDirServlet = new AllPagesInDirectoryServlet(
                this.produceHandler(),
                this.producePathResolver()
                );
        this.servlets.add(new ServletData(
                pageInDirServlet, 
                ALL_PAGES_IN_DIR_IN_PLACEMENT.servletName(), 
                ALL_PAGES_IN_DIR_IN_PLACEMENT.servletMapping()));  
        
        HttpServlet singlePageServlet = new SinglePageInDirectoryServlet(
                this.produceHandler(),
                this.producePathResolver()
                );
        this.servlets.add(new ServletData(
                singlePageServlet, 
                PAGE_FROM_DIR_IN_PLACEMENT.servletName(), 
                PAGE_FROM_DIR_IN_PLACEMENT.servletMapping()));
        
        HttpServlet fieldsServlet = new PageFieldsServlet(
                this.produceHandler(),
                this.producePathResolver()
                );
        this.servlets.add(new ServletData(
                fieldsServlet,
                PAGE_FIELDS_FROM_DIR_IN_PLACEMENT.servletName(),
                PAGE_FIELDS_FROM_DIR_IN_PLACEMENT.servletMapping()));
        
        HttpServlet dirFieldsServlet = new DirectoryFieldsServlet(
                this.produceHandler(), 
                this.producePathResolver());
        this.servlets.add(new ServletData(
                dirFieldsServlet,
                DIR_FIELDS_FROM_DIRS_IN_PLACEMENT.servletName(),
                DIR_FIELDS_FROM_DIRS_IN_PLACEMENT.servletMapping()));
    }
    
    private WebPagesHandler produceHandler() {
        return this.handlers.getWebPagesHandler();
    }
    
    private PathResolver producePathResolver() {
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
