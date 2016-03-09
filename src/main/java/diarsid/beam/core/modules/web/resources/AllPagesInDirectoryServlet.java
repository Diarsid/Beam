/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;
import java.io.PrintWriter;
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
class AllPagesInDirectoryServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    
    AllPagesInDirectoryServlet(DaoWebPages webDao) {
        this.webDao = webDao;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
        
        String path = request.getRequestURL().toString();
        System.out.println("ALL PAGES IN DIR pathInfo = " + path);
        List<WebPage> pages = this.webDao.getAllWebPagesInDirectoryAndPlacement(
                this.extractDirectoryFromPath(path), 
                WebPagePlacement.valueOf(this.extractPlacementFromPath(path)));
        JSONObject answer = new JSONObject();
        JSONArray pagesArray = new JSONArray();
        JSONObject pageJSONObject;
        for (WebPage page : pages) {
            pageJSONObject = new JSONObject();
            pageJSONObject.put("name", page.getName());
            pageJSONObject.put("url", page.getUrlAddress());
            pagesArray.add(pageJSONObject);
        }
        answer.put("webpages", pagesArray);
        
        PrintWriter writer = response.getWriter();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        writer.write(answer.toString());       
        writer.close();    
    }
    
    private String extractPlacementFromPath(String path) {
        return path.substring(
                path.lastIndexOf("resources/") + "resources/".length(), 
                path.indexOf("/dirs"))
                .toUpperCase();
    }
    
    private String extractDirectoryFromPath(String path) {
        return path.substring(
                path.lastIndexOf("dirs/") + "dirs/".length(),
                path.indexOf("/pages"));
    }
}
