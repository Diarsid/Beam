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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.domain.entities.WebPlacement;
import diarsid.beam.core.modules.data.HandlerWebPages;

/**
 *
 * @author Diarsid
 */
class AllPagesInDirectoryServlet extends HttpServlet {
    
    private final HandlerWebPages pagesHandler;
    private final PathResolver resolver;  
    private final JSONParser json;
    
    AllPagesInDirectoryServlet(HandlerWebPages handler, PathResolver resolver) {
        this.pagesHandler = handler;
        this.resolver = resolver;
        this.json = new JSONParser();
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
                        
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Allow", "GET, HEAD, POST, TRACE, OPTIONS");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, TRACE, OPTIONS");
            
        response.getWriter().close();    
    }
        
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
        List<WebPage> pages = this.pagesHandler.getAllWebPagesInDirectoryAndPlacement(
                this.resolver.extractDirectoryBeforePages(path), 
                this.resolver.extractPlacementBeforeDirectory(path),
                true);
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
            response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.getWriter().write(pagesArray.toString());       
        response.getWriter().close();    
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String path = this.resolver.getNormalizedPath(request);
            String directory = this.resolver.extractDirectoryBeforePages(path);
            WebPlacement place = 
                    this.resolver.extractPlacementBeforeDirectory(path);

            JSONObject postedPage = (JSONObject) this.json.parse(request.getReader());
            String name = postedPage.get("name").toString();
            String shortcuts = "";
            String url = postedPage.get("url").toString();
            
            if ( shortcuts == null ) {
                shortcuts = "";
            }
            if (name == null || url == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Unaccaptable JSON format - name or url is NULL.");
                response.getWriter().close();
                return;
            }
            if ( ! this.resolver.check(name) ) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Unaccaptable page name format - contains forbidden charecters.");
                response.getWriter().close();
                return;
            }
            if ( ! this.resolver.check(directory) ) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Unaccaptable directory name format - contains forbidden charecters.");
                response.getWriter().close();
                return;
            }
            
            boolean created = this.pagesHandler.saveWebPage(
                    name, 
                    shortcuts, 
                    url, 
                    place, 
                    directory,
                    "default");
            if ( created ) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                    response.setHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Location", path + "/" + name);
                response.getWriter().close();
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.setContentType("text/plain");
                response.getWriter()
                        .write("WebPage has not been saved due to unknown reasons.");
                response.getWriter().close();
                return;
            }
        } catch (ParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write(e.toString());
            response.getWriter().close();
        }        
    }
}
