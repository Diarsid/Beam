/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.drs.beam.core.modules.web;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Diarsid
 */
public interface ServletContainer {
            
    void startServer();
    
    void addServlet(HttpServlet servlet, String servletUrlMapping);
    
    void addFilter(
            Filter filter, 
            String filterUrlMapping, 
            DispatcherType t1, 
            DispatcherType... types);
}
