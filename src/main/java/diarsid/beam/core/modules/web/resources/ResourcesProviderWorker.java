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

import diarsid.beam.core.modules.DataModule;

import diarsid.beam.core.modules.web.ResourcesProvider;
import diarsid.beam.core.modules.web.ServletData;

import static diarsid.beam.core.modules.web.resources.RestResources.*;

/**
 *
 * @author Diarsid
 */
public class ResourcesProviderWorker implements ResourcesProvider {
    
    private final DataModule data;
    private final Set<ServletData> servlets;
    
    public ResourcesProviderWorker(DataModule data) {
        this.data = data;
        this.servlets = new HashSet<>();
        this.assembleServlets();
    }
    
    private void assembleServlets() {
        
        HttpServlet restRootPathDispatcherServlet = new DispatcherServlet();
        this.servlets.add(new ServletData(
                restRootPathDispatcherServlet, 
                ROOT.servletName(), 
                ROOT.servletMapping()));
        
        HttpServlet directoriesServlet = new AllDirectoriesServlet(data.getWebPagesDao());
        this.servlets.add(new ServletData(
                directoriesServlet, 
                DIRS_IN_PLACEMENT.servletName(), 
                DIRS_IN_PLACEMENT.servletMapping()));
        
        HttpServlet singleDirServlet = new SingleDirectoryServlet(data.getWebPagesDao());
        this.servlets.add(new ServletData(
                singleDirServlet, 
                DIR_FROM_DIRS_IN_PLACEMENT.servletName(), 
                DIR_FROM_DIRS_IN_PLACEMENT.servletMapping()));
        
        HttpServlet pageInDirServlet = new AllPagesInDirectoryServlet(data.getWebPagesDao());
        this.servlets.add(new ServletData(
                pageInDirServlet, 
                ALL_PAGES_IN_DIR_IN_PLACEMENT.servletName(), 
                ALL_PAGES_IN_DIR_IN_PLACEMENT.servletMapping()));        
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
