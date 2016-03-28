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

import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.handlers.WebPagesHandler;

/**
 *
 * @author Diarsid
 */
class AllDirectoriesServlet extends HttpServlet {
    
    private final WebPagesHandler pagesHandler;
    private final PathResolver resolver;
    private final JSONParser json;
    
    AllDirectoriesServlet(WebPagesHandler handler, PathResolver resolver) {
        this.pagesHandler = handler;
        this.resolver = resolver;
        this.json = new JSONParser();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = this.resolver.getNormalizedPath(request);
        
        List<WebPageDirectory> dirs = this.pagesHandler.getAllDirectoriesIn(
                this.resolver.extractPlacementBeforeDirectory(path));
        JSONArray directoriesArray = new JSONArray();
        JSONObject directoryObj;
        for (WebPageDirectory dir : dirs) {
            directoryObj = new JSONObject();
            directoryObj.put("name", dir.getName());
            directoryObj.put("ordering", dir.getOrder());
            directoriesArray.add(directoryObj);
        }
        
        response.setStatus(HttpServletResponse.SC_OK);
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
            String dir = (String) postedDir.get("name");
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
            
            if ( this.pagesHandler.createEmptyDirectory(place, dir) ) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/json");
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
