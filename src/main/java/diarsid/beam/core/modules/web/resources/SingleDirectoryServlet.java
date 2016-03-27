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

import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.handlers.WebPagesHandler;

/**
 *
 * @author Diarsid
 */
class SingleDirectoryServlet extends HttpServlet {
    
    private final WebPagesHandler pagesHandler;
    private final PathResolver resolver;    
    
    SingleDirectoryServlet(WebPagesHandler handler, PathResolver resolver) {
        this.pagesHandler = handler;
        this.resolver = resolver;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
        WebPageDirectory dir = this.pagesHandler.getDirectoryExact(
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
        
        if ( this.pagesHandler.deleteDirectoryAndPages(dir, place) ) {
            response.setStatus(HttpServletResponse.SC_OK);    
            response.getWriter().close();
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);    
            response.getWriter().close();
        }
    }
}
