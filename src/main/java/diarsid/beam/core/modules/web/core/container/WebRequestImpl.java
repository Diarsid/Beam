/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.base.control.io.base.interaction.Json;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;
import diarsid.beam.core.base.control.io.base.interaction.WebResponse;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

import static diarsid.beam.core.modules.web.core.container.JsonConversion.toJsonObject;
import static diarsid.beam.core.modules.web.core.container.RestUrlParametersUtil.parse;

/**
 *
 * @author Diarsid
 */
class WebRequestImpl implements WebRequest {
    
    private final HttpServletRequest servletRequest;
    private final HttpServletResponse servletResponse;
    private final String urlSchema;
    private Map<String, String> params;

    WebRequestImpl(
            HttpServletRequest servletRequest, 
            HttpServletResponse servletResponse, 
            String urlSchema) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.urlSchema = urlSchema;
        this.params = null;
    }
    
    void sendHttpMethodNotSupported(String method) throws IOException {
        this.servletResponse.setStatus(SC_METHOD_NOT_ALLOWED);
        this.servletResponse.setContentType("text/html");
        this.servletResponse.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = this.servletResponse.getWriter()) {
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Error 405 </title>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<h2>HTTP ERROR: 405</h2>");
            writer.println(format("<p> HTTP Method %s is not supported by this resource</p>", method));
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    @Override
    public void send(WebResponse webResponse) throws IOException {
        if ( webResponse.hasBody() ) {
            if ( webResponse.isBodyJson() ) {
                this.sendStatusWithJson(webResponse.status(), webResponse.jsonBody());
            } else {
                this.sendStatusWithBytes(webResponse.status(), webResponse.binaryBody());
            }    
        } else {
            this.sendStatus(webResponse.status());
        }
    }
    
    private void sendStatus(int status) throws IOException {
        this.servletResponse.setStatus(status);
        this.servletResponse.getWriter().close();
    }
    
    private void sendStatusWithJson(int status, String json) throws IOException {
        this.servletResponse.setStatus(status);
        this.servletResponse.setContentType("application/json");
        this.servletResponse.setCharacterEncoding("UTF-8");
        this.servletResponse.setContentLength(json.length());
        this.servletResponse.getWriter().write(json);
        this.servletResponse.getWriter().close();
    }
    
    private void sendStatusWithBytes(int status, byte[] bytes) throws IOException {
        this.servletResponse.setStatus(status);
//        this.servletResponse.setContentType("application/json");
        this.servletResponse.setCharacterEncoding("UTF-8");
        this.servletResponse.setContentLength(bytes.length);
        // TODO MIDDLE
        this.servletResponse.getOutputStream().write(bytes);
        this.servletResponse.getOutputStream().flush();
        this.servletResponse.getOutputStream().close();
    }
    
    private void parseParams() throws ResourceUrlParsingException {
        this.params = parse(this.urlSchema, this.servletRequest.getPathInfo());
    }
    
    @Override
    public String pathParam(String param) throws ResourceUrlParsingException {
        if ( isNull(this.params) ) {
            this.parseParams();
        }
        String value = this.params.get(param);
        if ( isNull(value) || value.isEmpty() ) {
            throw new ResourceUrlParsingException(param + " not found in request.");
        }
        return value;
    }
    
    @Override
    public Json json() throws IOException {
        return new JsonImpl(toJsonObject(this.servletRequest.getReader()));
    }
}
