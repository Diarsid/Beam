/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;


/**
 *
 * @author Diarsid
 */
public interface ResourceServletContainer {
            
    void startServer();
    
    void stopServer();
    
    void install(ResourceDispatcherServlet dispatcher, Resources resources);
    
    void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types);
}
