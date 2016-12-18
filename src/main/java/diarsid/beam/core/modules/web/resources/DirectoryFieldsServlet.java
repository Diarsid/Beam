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

import diarsid.beam.core.domain.entities.WebPlacement;
import diarsid.beam.core.modules.data.HandlerWebPages;

/**
 *
 * @author Diarsid
 */
class DirectoryFieldsServlet extends HttpServlet {
    
    private final HandlerWebPages pagesHandler;
    private final PathResolver resolver;
    private final JSONParser json;
    
    DirectoryFieldsServlet(HandlerWebPages handler, PathResolver res) {
        this.pagesHandler = handler;
        this.resolver = res;
        this.json = new JSONParser();
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setStatus(HttpServletResponse.SC_OK);
                        
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Allow", "GET, HEAD, PUT, TRACE, OPTIONS");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, PUT, TRACE, OPTIONS");
            
        response.getWriter().close();    
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            
            String path = this.resolver.getNormalizedPath(request);
            String fieldToPut = this.resolver.extractDirectoryField(path);
            String dirName = this.resolver.extractDirectoryBeforeFields(path);
            WebPlacement place = 
                    this.resolver.extractPlacementBeforeDirectory(path);
            JSONObject newValueJson = 
                    (JSONObject) this.json.parse(request.getReader().readLine());

            switch (fieldToPut) {

                case "name" : {
                    String newName = newValueJson.get("name").toString();
                    if ( ! this.resolver.check(newName) ) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable directory name format - contains forbidden character.");
                        response.getWriter().close();
                        return;
                    }
                    
                    if ( this.pagesHandler.renameDirectoryInPlacement(
                            dirName, newName, place) ) {
                        response.setStatus(HttpServletResponse.SC_OK);
                            response.setHeader("Access-Control-Allow-Origin", "*");
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
                    String newOrderStr = newValueJson.get("order").toString();
                    if ( ! newOrderStr.matches("[0-9]+")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable directory order format - must contain only digits.");
                        response.getWriter().close();
                        return;
                    }
                    int newOrder = Integer.parseInt(newOrderStr);
                    
                    if ( this.pagesHandler.editDirectoryOrder(place, dirName, newOrder) ) {
                        response.setStatus(HttpServletResponse.SC_OK);
                            response.setHeader("Access-Control-Allow-Origin", "*");
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
