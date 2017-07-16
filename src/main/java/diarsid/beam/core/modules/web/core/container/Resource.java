/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.base.control.io.base.interaction.WebRequest;


public abstract class Resource 
        extends HttpServlet 
        implements ResourceData {
    
    private static final String PARAMETER_REGEXP;
    static {        
        PARAMETER_REGEXP = "[a-zA-Z0-9-_\\.>\\s]+";
    }
    
    private final String name;
    private final String mappingUrlSchema;
    private final String mappingUrlRegexp;

    protected Resource(String mappingUrlSchema) {        
        this.mappingUrlSchema = mappingUrlSchema;
        this.mappingUrlRegexp = mappingUrlSchema
                .replaceAll("\\{[a-zA-Z0-9-_\\.>\\s]+\\}", PARAMETER_REGEXP);
        this.name = "[RESOURCE " + 
                this.getClass().getCanonicalName() + " " + this.mappingUrlSchema + "]";
    }
    
    @Override
    public final String name() {
        return this.name;
    }
    
    @Override
    public final String url() {
        return this.mappingUrlSchema;
    }
    
    public final boolean matchesUrl(String url) {
        return url.matches(this.mappingUrlRegexp);
    }
    
    @Override
    protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.TRACE(wrap(req, resp));
    }

    @Override
    protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.OPTIONS(wrap(req, resp));
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.DELETE(wrap(req, resp));
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.PUT(wrap(req, resp));
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.POST(wrap(req, resp));
    }

    @Override
    protected final void doHead(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.HEAD(wrap(req, resp));
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.GET(wrap(req, resp));
    }
    
    private WebRequestImpl wrap(
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        return new WebRequestImpl(servletRequest, servletResponse, this.mappingUrlSchema);
    }
    
    protected void GET(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("GET");
    }
    
    protected void POST(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("POST");
    }
    
    protected void PUT(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("PUT");
    }
    
    protected void DELETE(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("DELETE");
    }
    
    protected void OPTIONS(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("OPTIONS");
    }
    
    protected void TRACE(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("TRACE");
    }
    
    protected void HEAD(WebRequest webRequest) throws IOException {
        ((WebRequestImpl) webRequest).sendHttpMethodNotSupported("HEAD");
    }
}
