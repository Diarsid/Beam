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
    
    private final Resources resources;

    public AbstractDispatcherServlet(Resources namedResources) {
        this.resources = namedResources;
    }
    
    protected final Optional<String> getResourceNameForUrl(String url) {
        return this.resources.getMatchingResourceNameFor(url);
    }
    
    protected final void dispatchRequestForwardToNamedResource(
            HttpServletRequest req, 
            HttpServletResponse resp, 
            String name) 
                throws ServletException, IOException  {
        super.getServletContext()
                .getNamedDispatcher(name)
                .forward(req, resp);
    }
}
