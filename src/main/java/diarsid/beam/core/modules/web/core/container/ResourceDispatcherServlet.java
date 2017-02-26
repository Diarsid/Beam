/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;


import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static diarsid.beam.core.modules.web.core.jsonconversion.JsonUtil.errorJson;

/**
 *
 * @author Diarsid
 */
public class ResourceDispatcherServlet extends HttpServlet {
    
    private final Resources resources;
    private final ExceptionToJsonMapper exceptionMapper;

    public ResourceDispatcherServlet(
            Resources namedResources, 
            ExceptionToJsonMapper exceptionMapper) {
        this.resources = namedResources;
        this.exceptionMapper = exceptionMapper;
    }
    
    @Override
    protected final void service(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {        
        Optional<String> resourceName = this.getResourceNameForUrl(req.getPathInfo());        
        if ( resourceName.isPresent() ) {   
            try {
                this.dispatchRequestForwardToNamedResource(req, resp, resourceName.get());      
            } catch (Exception e) {
                this.sendAppropriateError(resp, e);
            } 
            if ( ! resp.isCommitted() ) {
                this.sendUnprocessedRequestError(resp);
            }
        } else {     
            this.sendUnrecognizableUrlError(resp);
        } 
    }
    
    private Optional<String> getResourceNameForUrl(String url) {
        return this.resources.getMatchingResourceNameFor(url);
    }
    
    private void dispatchRequestForwardToNamedResource(
            HttpServletRequest req, 
            HttpServletResponse resp, 
            String name) 
                throws ServletException, IOException  {        
        super.getServletContext()
                .getNamedDispatcher(name)
                .forward(req, resp);               
    }
    
    private void sendAppropriateError(HttpServletResponse resp, Exception e) throws IOException {
        JsonError jsonError = this.exceptionMapper.map(e);
        resp.setStatus(jsonError.status());
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");     
        resp.getWriter().write(jsonError.json());
        resp.getWriter().close();
    }

    private void sendUnrecognizableUrlError(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");     
        resp.getWriter().write(errorJson(
                "Malformed or incorrect request URL. " +
                "This request can not be dispatched to any of existing resources."));
        resp.getWriter().close();
    }

    private void sendUnprocessedRequestError(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");        
        resp.getWriter().write(errorJson(
                "Unknown server error: " +
                "Request has been dispatched to appropriate resource " +
                "but has not been committed."));
        resp.getWriter().close();
    }
}
