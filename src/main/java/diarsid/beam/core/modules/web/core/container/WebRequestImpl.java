/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.base.control.io.base.interaction.ConvertableToJson;
import diarsid.beam.core.base.control.io.base.interaction.WebRequest;

import static java.lang.String.format;
import static java.util.Objects.isNull;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import static diarsid.beam.core.base.util.JsonUtil.convertablesAsJsonArray;
import static diarsid.beam.core.modules.web.core.container.ResourceServletContainer.objectivizer;
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
    public void sendOk() throws IOException {
        this.servletResponse.setStatus(SC_OK);
        this.servletResponse.getWriter().close();
    }
    
    @Override
    public void sendBadRequest() throws IOException {
        this.servletResponse.setStatus(SC_BAD_REQUEST);
        this.servletResponse.getWriter().close();
    }
    
    @Override
    public void sendStatus(int status) throws IOException {
        this.servletResponse.setStatus(status);
        this.servletResponse.getWriter().close();
    }
    
    @Override
    public void sendOkWithJson(String json) throws IOException {
        this.servletResponse.setStatus(SC_OK);
        this.servletResponse.setContentType("application/json");
        this.servletResponse.setCharacterEncoding("UTF-8");
        this.servletResponse.setContentLength(json.length());
        this.servletResponse.getWriter().write(json);
        this.servletResponse.getWriter().close();
    }
    
    @Override
    public void sendOkWithJson(ConvertableToJson convertable) throws IOException {
        this.sendOkWithJson(convertable.toJson());
    }
    
    @Override
    public void sendOptionalOkWithJson(Optional<? extends ConvertableToJson> convertable) 
            throws IOException {
        if ( convertable.isPresent() ) {
            this.sendOkWithJson(convertable.get().toJson());
        } else {
            this.sendNotFound();
        }        
    }
    
    @Override
    public void sendOkWithJson(Collection<? extends ConvertableToJson> convertables) 
            throws IOException {
        this.sendOkWithJson(convertablesAsJsonArray(convertables));
    }
    
    @Override
    public void sendBadRequestWithJson(String json) throws IOException {
        sendStatusWithJson(json, SC_BAD_REQUEST);
    }
    
    @Override
    public void sendNotFoundWithJson(String json) throws IOException {
        sendStatusWithJson(json, SC_NOT_FOUND);
    }
    
    @Override
    public void sendNotFound() throws IOException {
        this.servletResponse.setStatus(SC_NOT_FOUND);
        this.servletResponse.getWriter().close();
    }
    
    @Override
    public void sendStatusWithJson(String json, int status) throws IOException {
        this.servletResponse.setStatus(status);
        this.servletResponse.setContentType("application/json");
        this.servletResponse.setCharacterEncoding("UTF-8");
        this.servletResponse.setContentLength(json.length());
        this.servletResponse.getWriter().write(json);
        this.servletResponse.getWriter().close();
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
    public Object bodyOf(Class clazz) throws IOException {
        return objectivizer().objectivize(clazz, this.servletRequest.getReader());
    }
}
