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

import static diarsid.beam.core.util.StringUtils.randomString;


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

    public Resource(String mappingUrlSchema) {
        this.name = randomString(13);
        this.mappingUrlSchema = mappingUrlSchema;
        this.mappingUrlRegexp = mappingUrlSchema
                .replaceAll("\\{[a-zA-Z0-9-_\\.>\\s]+\\}", PARAMETER_REGEXP);
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
        this.TRACE(wrap(req), wrap(resp));
    }

    @Override
    protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.OPTIONS(wrap(req), wrap(resp));
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.DELETE(wrap(req), wrap(resp));
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.PUT(wrap(req), wrap(resp));
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.POST(wrap(req), wrap(resp));
    }

    @Override
    protected final void doHead(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.HEAD(wrap(req), wrap(resp));
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        this.GET(wrap(req), wrap(resp));
    }
    
    private ResourceRequest wrap(HttpServletRequest servletRequest) {
        return new ResourceRequest(servletRequest, this.mappingUrlSchema);
    }
    
    private ResourceResponse wrap(HttpServletResponse servletResponse) {
        return new ResourceResponse(servletResponse);
    }
    
    protected void GET(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("GET");
    }
    
    protected void POST(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("POST");
    }
    
    protected void PUT(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("PUT");
    }
    
    protected void DELETE(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("DELETE");
    }
    
    protected void OPTIONS(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("OPTIONS");
    }
    
    protected void TRACE(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("TRACE");
    }
    
    protected void HEAD(ResourceRequest request, ResourceResponse response) throws IOException {
        response.sendHttpMethodNotSupported("HEAD");
    }
}
