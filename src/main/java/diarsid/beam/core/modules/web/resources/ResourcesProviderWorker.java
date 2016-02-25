/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

import diarsid.beam.core.modules.DataModule;

import diarsid.beam.core.modules.web.ResourcesProvider;

/**
 *
 * @author Diarsid
 */
public class ResourcesProviderWorker implements ResourcesProvider {
    
    private final DataModule data;
    
    public ResourcesProviderWorker(DataModule data) {
        this.data = data;
    }
    
    @Override
    public Map<String, HttpServlet> getServlets() {
        Map<String, HttpServlet> servlets = new HashMap<>();
        
        HttpServlet serv = new AllDirectoriesServlet(data.getWebPagesDao());
        servlets.put("/resources/webpanel/dirs", serv);
        return servlets;
    }
    
    @Override
    public Set<Filter> getFilters() {
        return null;
    }
}
