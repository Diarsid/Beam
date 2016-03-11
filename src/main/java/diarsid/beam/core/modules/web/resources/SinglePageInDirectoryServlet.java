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
import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class SinglePageInDirectoryServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    private final PathResolver resolver;
    
    SinglePageInDirectoryServlet(DaoWebPages webDao, PathResolver resolver) {
        this.resolver = resolver;
        this.webDao = webDao;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
                
        List<WebPage> pages = this.webDao.getWebPagesByNameInDirAndPlace(
                this.resolver.extractPage(path),
                this.resolver.extractDirectoryBeforePages(path), 
                this.resolver.extractPlacementBeforeDirectory(path));
        JSONObject answer = new JSONObject();
        
        if (pages.size() == 1) {
            answer.put("name", pages.get(0).getName());
            answer.put("url", pages.get(0).getUrlAddress());
        } else {
            JSONArray pagesArray = new JSONArray();
            JSONObject pageJSONObject;
            for (WebPage page : pages) {
                pageJSONObject = new JSONObject();
                pageJSONObject.put("name", page.getName());
                pageJSONObject.put("url", page.getUrlAddress());
                pagesArray.add(pageJSONObject);
            }
            answer.put("webpages", pagesArray);
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
        
        if ( this.webDao.deleteWebPage(page, dir, place)) {
            response.setStatus(HttpServletResponse.SC_OK);    
            response.getWriter().close();
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);    
            response.getWriter().close();
        }
    }
}
