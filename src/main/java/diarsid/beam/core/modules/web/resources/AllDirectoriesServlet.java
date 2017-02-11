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
import diarsid.beam.core.domain.entities.WebPageDirectory;
import diarsid.beam.core.domain.entities.WebPagePlacement;
import diarsid.beam.core.modules.PagesManagerModule;

/**
 *
 * @author Diarsid
 */
class AllDirectoriesServlet extends HttpServlet {
    
    private final PagesManagerModule pagesModule;
    private final PathResolver resolver;
    private final JSONParser json;
    
    AllDirectoriesServlet(PagesManagerModule pagesModule, PathResolver resolver) {
        this.pagesModule = pagesModule;
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
        WebPagePlacement place = 
                this.resolver.extractPlacementBeforeDirectory(path);
        List<WebPageDirectory> dirs = this.pagesModule
                .getAllDirectoriesIn(place);
        JSONArray directoriesArray = new JSONArray();
        JSONObject directoryObj;
        JSONArray pagesInDirArray;
        JSONObject singlePageInDirObj;
        List<WebPage> pages;
        for (WebPageDirectory dir : dirs) {
            directoryObj = new JSONObject();
            directoryObj.put("name", dir.getName());
            directoryObj.put("order", dir.getOrder());
            pages = this.pagesModule.getAllWebPagesInDirectoryAndPlacement(
                    dir.getName(), place, true);
            pagesInDirArray = new JSONArray();
            for (WebPage page : pages) {
                singlePageInDirObj = new JSONObject();
                singlePageInDirObj.put("order", page.getPageOrder());
                singlePageInDirObj.put("name", page.getName());
                singlePageInDirObj.put("url", page.getUrlAddress());
                pagesInDirArray.add(singlePageInDirObj);
            }
            directoryObj.put("pages", pagesInDirArray);
            directoriesArray.add(directoryObj);
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.getWriter().write(directoriesArray.toString());       
        response.getWriter().close();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {            
            String path = this.resolver.getNormalizedPath(request);
            JSONObject postedDir = (JSONObject) this.json.parse(request.getReader());
            
            WebPagePlacement place = this.resolver.extractPlacementBeforeDirectory(path);
            String dir = postedDir.get("name").toString();
            if ( dir == null ) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Unaccaptable JSON format - name is NULL.");
                response.getWriter().close();
                return;
            }
            if ( ! this.resolver.check(dir) ) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Unaccaptable directory name format - contains forbidden charecters.");
                response.getWriter().close();
                return;
            }
            
            if ( this.pagesModule.createEmptyDirectory(place, dir) ) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Location", path + "/" + dir);
                response.getWriter().close();
                return;
            } else {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                response.setContentType("text/plain");
                response.getWriter()
                        .write("WebPageDirectory has not been created due to unknown reasons.");
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
