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

import diarsid.beam.core.entities.WebPage;
import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.data.DaoWebPages;

import static diarsid.beam.core.entities.WebPage.newPage;

/**
 *
 * @author Diarsid
 */
class AllPagesInDirectoryServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    private final PathResolver resolver;  
    private final JSONParser json;
    
    AllPagesInDirectoryServlet(DaoWebPages webDao, PathResolver resolver) {
        this.webDao = webDao;
        this.resolver = resolver;
        this.json = new JSONParser();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = this.resolver.getNormalizedPath(request);
        System.out.println("ALL PAGES IN DIR pathInfo = " + path);
        List<WebPage> pages = this.webDao.getAllWebPagesInDirectoryAndPlacement(
                this.resolver.extractDirectoryBeforePages(path), 
                this.resolver.extractPlacementBeforeDirectory(path),
                true);
        JSONObject answer = new JSONObject();
        JSONArray pagesArray = new JSONArray();
        JSONObject pageJSONObject;
        for (WebPage page : pages) {
            pageJSONObject = new JSONObject();
            pageJSONObject.put("name", page.getName());
            pageJSONObject.put("order", page.getPageOrder());
            pageJSONObject.put("url", page.getUrlAddress());
            pagesArray.add(pageJSONObject);
        }
        answer.put("webpages", pagesArray);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(answer.toString());       
        response.getWriter().close();    
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String path = this.resolver.getNormalizedPath(request);
            String directory = this.resolver.extractDirectoryBeforePages(path);
            WebPagePlacement place = 
                    this.resolver.extractPlacementBeforeDirectory(path);

            JSONObject postedPage = (JSONObject) this.json.parse(request.getReader());
            String name = (String) postedPage.get("name");
            String shortcuts = (String) postedPage.get("shortcuts");
            String url = (String) postedPage.get("url");
            
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
            
            WebPage page = newPage(
                    name, 
                    shortcuts, 
                    url, 
                    place, 
                    directory,
                    "default");
            if ( this.webDao.saveWebPage(page) ) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.setContentType("application/json");
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
