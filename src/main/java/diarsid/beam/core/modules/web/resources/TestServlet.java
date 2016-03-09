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
class TestServlet extends HttpServlet {
    
    private final DaoWebPages webDao;    
    
    TestServlet(DaoWebPages webDao) {
        this.webDao = webDao;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
              
        List<WebPage> pages = this.webDao.getAllWebPagesInDirectoryAndPlacement(
                "common",
                WebPagePlacement.WEBPANEL);
        JSONObject answer = new JSONObject();
        JSONArray pagesArray = new JSONArray();
        JSONObject pageJSONObject;
        for (WebPage page : pages) {
            pageJSONObject = new JSONObject();
            pageJSONObject.put("name", page.getName());
            pageJSONObject.put("url", page.getUrlAddress());
            pagesArray.add(pageJSONObject);
        }
        answer.put("TEST_SERVLET_RESPONSE", pagesArray);
        
        PrintWriter writer = response.getWriter();
        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding(null);
        writer.write(answer.toString());       
        writer.close();
    }
}
