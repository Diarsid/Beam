/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web;

import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

/**
 *
 * @author Diarsid
 */
public interface ServletContainer {
            
    void startServer();
    
    void stopServer();
    
    void addServlets(Set<ServletData> servlets);
    
    void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types);
}
