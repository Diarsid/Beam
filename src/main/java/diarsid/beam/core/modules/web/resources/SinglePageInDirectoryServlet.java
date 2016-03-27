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

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.handlers.WebPagesHandler;

/**
 *
 * @author Diarsid
 */
class SinglePageInDirectoryServlet extends HttpServlet {
    
    private final WebPagesHandler pagesHandler;
    private final PathResolver resolver;
    
    SinglePageInDirectoryServlet(WebPagesHandler handler, PathResolver resolver) {
        this.resolver = resolver;
        this.pagesHandler = handler;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
                
        List<WebPage> pages = this.pagesHandler.getWebPagesByNameInDirAndPlace(
                this.resolver.extractPage(path),
                this.resolver.extractDirectoryBeforePages(path), 
                this.resolver.extractPlacementBeforeDirectory(path));
        JSONObject answer = new JSONObject();
        
        if ( pages.size() == 1 ) {
            answer.put("name", pages.get(0).getName());
            answer.put("order", pages.get(0).getPageOrder());
            answer.put("url", pages.get(0).getUrlAddress());
        } else if ( pages.size() > 1 ) {
            JSONArray pagesArray = new JSONArray();
            JSONObject pageJSONObject;
            for (WebPage page : pages) {
                pageJSONObject = new JSONObject();
                pageJSONObject.put("name", page.getName());
                pageJSONObject.put("order", pages.get(0).getPageOrder());
                pageJSONObject.put("url", page.getUrlAddress());
                pagesArray.add(pageJSONObject);
            }
            answer.put("webpages", pagesArray);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json");       
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
        
        String page = this.resolver.extractPage(path);
        String dir = this.resolver.extractDirectoryBeforePages(path);
        WebPagePlacement place = this.resolver.extractPlacementBeforeDirectory(path);
        
        if ( this.pagesHandler.deleteWebPage(page, dir, place)) {
            response.setStatus(HttpServletResponse.SC_OK);    
            response.getWriter().close();
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);    
            response.getWriter().close();
        }
    }
}
