/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.core.container;

import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import diarsid.beam.core.base.util.Pair;


/**
 *
 * @author Diarsid
 */
public interface ResourceServletContainer {
            
    void startServer();
    
    void stopServer();
    
    void install(
            ResourceDispatcherServlet dispatcher,
            Map<Integer, String> redirections,
            Pair<String, String> staticContent);
    
    void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types);
}
