/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.modules.web.core.rest.RestUrlParamsParser;

import static java.util.Collections.emptyMap;


public abstract class Resource 
        extends HttpServlet {
    
    private static final String PARAMETER_REGEXP;
    static {        
        PARAMETER_REGEXP = "[a-zA-Z0-9-_\\.>\\s]+";
    }
    
    private final String name;
    private final String mappingUrlSchema;
    private final String mappingUrlRegexp;
    private final RestUrlParamsParser paramsParser;

    public Resource(
            String name, 
            String mappingUrlSchema,
            RestUrlParamsParser paramsParser) {
        this.name = name;
        this.mappingUrlSchema = mappingUrlSchema;
        this.mappingUrlRegexp = mappingUrlSchema
                .replaceAll("\\{[a-zA-Z0-9-_\\.>\\s]+\\}", PARAMETER_REGEXP);
        this.paramsParser = paramsParser;
    }
    
    public String name() {
        return this.name;
    }
    
    public boolean matchesUrl(String url) {
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
        return new ResourceRequest(servletRequest, this.provideParameters(servletRequest));
    }
    
    private ResourceResponse wrap(HttpServletResponse servletResponse) {
        return new ResourceResponse(servletResponse);
    }

    private Map<String, String> provideParameters(HttpServletRequest servletRequest) {
        String requestUrl = servletRequest.getRequestURL().toString();
        if ( this.paramsParser.ifHasParams(requestUrl) ) {
            return this.paramsParser.parse(this.mappingUrlSchema, requestUrl);
        } else {
            return emptyMap();
        }        
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
