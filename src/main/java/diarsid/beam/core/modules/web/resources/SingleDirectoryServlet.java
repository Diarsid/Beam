/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import diarsid.beam.core.domain.entities.WebPageDirectory;
import diarsid.beam.core.domain.entities.WebPagePlacement;
import diarsid.beam.core.modules.PagesManagerModule;

/**
 *
 * @author Diarsid
 */
class SingleDirectoryServlet extends HttpServlet {
    
    private final PagesManagerModule pagesModule;
    private final PathResolver resolver;    
    
    SingleDirectoryServlet(PagesManagerModule pagesModule, PathResolver resolver) {
        this.pagesModule = pagesModule;
        this.resolver = resolver;
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
                        
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Allow", "GET, DELETE, HEAD, TRACE, OPTIONS");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, DELETE, TRACE, OPTIONS");
            
        response.getWriter().close();    
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
        WebPageDirectory dir = this.pagesModule.getDirectoryExact(
                this.resolver.extractPlacementBeforeDirectory(path), 
                this.resolver.extractDirectory(path));
        
        JSONObject answer = new JSONObject();
        if ( dir != null ) {            
            answer.put("name", dir.getName());
            answer.put("ordering", dir.getOrder());
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().close(); 
        }   
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(answer.toString());       
        response.getWriter().close();    
    }    
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = this.resolver.getNormalizedPath(request);
        String dir = this.resolver.extractDirectory(path);
        WebPagePlacement place = 
                this.resolver.extractPlacementBeforeDirectory(path);
        
        if ( this.pagesModule.deleteDirectoryAndPages(dir, place) ) {
            response.setStatus(HttpServletResponse.SC_OK);  
                response.setHeader("Access-Control-Allow-Origin", "*");
            response.getWriter().close();
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);    
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.getWriter().close();
        }
    }
}
