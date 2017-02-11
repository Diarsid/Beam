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

/**
 *
 * @author Diarsid
 */
public abstract class AbstractDispatcherServlet extends HttpServlet {
    
    private final NamedResources namedResources;

    public AbstractDispatcherServlet(NamedResources namedResources) {
        this.namedResources = namedResources;
    }
    
    public final Optional<String> getResourceNameForUrl(String url) {
        return this.namedResources.getResourceNameOf(url);
    }
    
    public final void dispatchRequestForwardToNamedResource(
            HttpServletRequest req, 
            HttpServletResponse resp, 
            String resourceName) 
                throws ServletException, IOException  {
        super.getServletContext().getNamedDispatcher(resourceName).forward(req, resp);
    }
}
