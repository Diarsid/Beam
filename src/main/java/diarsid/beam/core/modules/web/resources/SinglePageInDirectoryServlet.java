/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import diarsid.beam.core.entities.global.WebPage;
import diarsid.beam.core.entities.global.WebPagePlacement;
import diarsid.beam.core.modules.data.HandlerWebPages;

/**
 *
 * @author Diarsid
 */
class SinglePageInDirectoryServlet extends HttpServlet {
    
    private final HandlerWebPages pagesHandler;
    private final PathResolver resolver;
    
    SinglePageInDirectoryServlet(HandlerWebPages handler, PathResolver resolver) {
        this.resolver = resolver;
        this.pagesHandler = handler;
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
                        
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Allow", "GET, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
            
        response.getWriter().close();    
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request); 
        List<WebPage> pages = this.pagesHandler.getWebPagesByNameInDirAndPlace(
                this.resolver.extractPage(path),
                this.resolver.extractDirectoryBeforePages(path), 
                this.resolver.extractPlacementBeforeDirectory(path));                
        
        if ( pages.size() > 0 ) {            
            JSONArray pagesArray = new JSONArray();
            JSONObject pageJSONObject;
            for (WebPage page : pages) {
                pageJSONObject = new JSONObject();
                pageJSONObject.put("name", page.getName());
                pageJSONObject.put("order", page.getPageOrder());
                pageJSONObject.put("url", page.getUrlAddress());
                pagesArray.add(pageJSONObject);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(pagesArray.toString());       
            response.getWriter().close(); 
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");       
            response.getWriter().close();
        }       
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = this.resolver.getNormalizedPath(request);
        
        String page = this.resolver.extractPage(path);
        String dir = this.resolver.extractDirectoryBeforePages(path);
        WebPagePlacement place = this.resolver.extractPlacementBeforeDirectory(path);
        
        if ( this.pagesHandler.deleteWebPage(page, dir, place)) {
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
