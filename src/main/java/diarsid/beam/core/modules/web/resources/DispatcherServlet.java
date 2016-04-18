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

/**
 * Request dispatcher.
 * Accepts all incoming requests due to it has /* mapping.
 * Obtains resource servlet name for corresponding request URL and dispatches
 * this request to appropriate servlet using its name.
 * 
 * @author Diarsid
 */
class DispatcherServlet extends HttpServlet {
    
    DispatcherServlet() {
    }
    
    /**
     * This method handles all incoming requests to the server.
     * 
     * It does not respond them, just dispatches incoming requests
     * to appropriate resource servlets using RestResources enums 
     * static methods to obtain an appropriate servlet name.
     * 
     * It responds the request itself only in case when the appropriate
     * servlet name has not been found. It usually means that request 
     * URL is malformed or incorrect.
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
                
        String dispatchedServletName = RestResourcesForWebPages
                .getDispatchedServletNameOfResource(req.getPathInfo());
        
        //System.out.println("[DISPATCHER SERVLET] PathInfo: " + req.getPathInfo());
        //System.out.println("[DISPATCHER SERVLET] dispatch name: " + dispatchedServletName);
        
        if ( dispatchedServletName.isEmpty() ) {            
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            JSONObject answer = new JSONObject();
            answer.put("type", "error");
            answer.put("text", 
                    "Malformed or incorrect request URL. " +
                    "This request can not be dispatched to any of existing resources.");
            resp.getWriter().write(answer.toString());       
            resp.getWriter().close();
        } else {            
            this.getServletContext()
                    .getNamedDispatcher(dispatchedServletName)
                    .forward(req, resp);
            
            if ( ! resp.isCommitted() ) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                JSONObject answer = new JSONObject();
                answer.put("type", "error");
                answer.put("text", "Unknown server error: " +
                        "Request has been dispatched to appropriate resource " +
                        "but has not been committed.");
                resp.getWriter().write(answer.toString());       
                resp.getWriter().close();
            }
        } 
    }
}
