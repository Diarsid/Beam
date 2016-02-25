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

import diarsid.beam.core.entities.WebPageDirectory;
import diarsid.beam.core.entities.WebPagePlacement;

import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class AllDirectoriesServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    private final String servletUrlMapping;
    
    AllDirectoriesServlet(DaoWebPages webDao) {
        this.webDao = webDao;
        this.servletUrlMapping = "resources/webpanel/dirs";
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<WebPageDirectory> dirs = this.webDao.getAllDirectoriesIn(WebPagePlacement.WEBPANEL);
        JSONArray directoriesArray = new JSONArray();
        JSONObject directoryObj;
        for (WebPageDirectory dir : dirs) {
            directoryObj = new JSONObject();
            directoryObj.put("name", dir.getName());
            directoryObj.put("ordering", dir.getOrder());
            directoriesArray.add(directoryObj);
        }
        JSONObject answer = new JSONObject();
        answer.put("webpanel_directories", directoriesArray);
        
        PrintWriter writer = response.getWriter();
        response.setStatus(200);
        response.setContentType("application/json");
        writer.write(answer.toString());       
        writer.close();
    }
    
    public String getUrlMapping() {
        return this.servletUrlMapping;
    }
}
