/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class DirectoryFieldsServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    private final PathResolver resolver;
    private final JSONParser json;
    
    DirectoryFieldsServlet(DaoWebPages dao, PathResolver res) {
        this.webDao = dao;
        this.resolver = res;
        this.json = new JSONParser();
    }
    
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            
            String path = this.resolver.getNormalizedPath(request);
            String fieldToPut = this.resolver.extractDirectoryField(path);
            String dirName = this.resolver.extractDirectoryBeforeFields(path);
            WebPagePlacement place = 
                    this.resolver.extractPlacementBeforeDirectory(path);
            JSONObject newValueJson = 
                    (JSONObject) this.json.parse(request.getReader());

            switch (fieldToPut) {

                case "name" : {
                    String newName = (String) newValueJson.get("name");
                    if ( ! this.resolver.check(newName) ) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable directory name format - contains forbidden character.");
                        response.getWriter().close();
                        return;
                    }
                    
                    if ( this.webDao.editDirectoryNameInPlacement(
                            dirName, newName, place) ) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("Directory name has not been edited due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }

                case "order" : {
                    String newOrderStr = (String) newValueJson.get("order");
                    if ( ! newOrderStr.matches("[0-9]+")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable directory order format - must contain only digits.");
                        response.getWriter().close();
                        return;
                    }
                    int newOrder = Integer.parseInt(newOrderStr);
                    
                    if ( this.webDao.editDirectoryOrder(place, dirName, newOrder) ) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("Directory order has not been edited due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }
                
                default : {
                    //
                }
            }
            
        } catch (ParseException|NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write(e.toString());
            response.getWriter().close();
        }
    }
}
