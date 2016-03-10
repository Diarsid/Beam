/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Diarsid
 */
class DispatcherServlet extends HttpServlet {
    
    DispatcherServlet() {
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
                
        String dispatchedServletName = RestResources
                .getDispatchedServletNameOfResource(req.getPathInfo());
        
        System.out.println("[DISPATCHER SERVLET] PathInfo: " + req.getPathInfo());
        System.out.println("[DISPATCHER SERVLET] dispatch name: " + dispatchedServletName);
        
        if ( dispatchedServletName.isEmpty() ) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter writer = resp.getWriter();
            resp.setContentType("text/plain");
            writer.write("unspecified url.");       
            writer.close();
        } else {            
            this.getServletContext()
                    .getNamedDispatcher(dispatchedServletName)
                    .forward(req, resp);
        }        
        
        if ( ! resp.isCommitted() ) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter writer = resp.getWriter();
            resp.setContentType("text/plain");
            writer.write("REST Servlet dispatch logic error.");       
            writer.close();
        } 
    }
}
