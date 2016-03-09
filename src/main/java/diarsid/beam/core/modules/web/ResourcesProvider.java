/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web;

import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Diarsid
 */
public interface ResourcesProvider {
    
    Set<ServletData> getServlets();
    
    Set<Filter> getFilters();
}
