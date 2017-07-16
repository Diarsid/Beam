/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import diarsid.beam.core.modules.web.core.jsonconversion.Objectivizer;


/**
 *
 * @author Diarsid
 */
public abstract class ResourceServletContainer {
    
    private static Objectivizer objectivizer;
    
    protected ResourceServletContainer(Objectivizer jsonizer) {
        ResourceServletContainer.objectivizer = jsonizer;
    }
        
    static Objectivizer objectivizer() {
        return objectivizer;
    }
            
    public abstract void startServer();
    
    public abstract void stopServer();
    
    public abstract void install(ResourceDispatcherServlet dispatcher, Resources resources);
    
    public abstract void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types);
}
