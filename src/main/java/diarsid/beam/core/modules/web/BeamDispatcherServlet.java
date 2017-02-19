/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import diarsid.beam.core.modules.web.core.container.AbstractDispatcherServlet;
import diarsid.beam.core.modules.web.core.container.Resources;

/**
 *
 * @author Diarsid
 */
public class BeamDispatcherServlet extends AbstractDispatcherServlet {
    
    public BeamDispatcherServlet(Resources resources) {
        super(resources);
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
        //System.out.println("[DISPATCHER SERVLET] PathInfo: " + req.getPathInfo());
        //System.out.println("[DISPATCHER SERVLET] dispatch name: " + dispatchedServletName);
        
        Optional<String> resourceName = super.getResourceNameForUrl(req.getPathInfo());        
        if ( resourceName.isPresent() ) {         
            super.dispatchRequestForwardToNamedResource(req, resp, resourceName.get());            
            if ( ! resp.isCommitted() ) {
                this.sendUnprocessedRequestError(resp);
            }
        } else {     
            this.sendUnrecognizableUrlError(resp);
        } 
    }

    private void sendUnrecognizableUrlError(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        JSONObject answer = new JSONObject();
        answer.put("type", "error");
        answer.put("text",
                       "Malformed or incorrect request URL. " +
                               "This request can not be dispatched to any of existing resources.");
        resp.getWriter().write(answer.toString());
        resp.getWriter().close();
    }

    private void sendUnprocessedRequestError(HttpServletResponse resp) throws IOException {
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
