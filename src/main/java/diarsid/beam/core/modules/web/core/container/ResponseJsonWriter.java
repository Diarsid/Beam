/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import diarsid.beam.core.modules.web.core.jsonconversion.Jsonizer;

import static java.lang.String.valueOf;

/**
 *
 * @author Diarsid
 */
public class ResponseJsonWriter {
    
    private static final String JSON_ERROR_TEMPLATE;
    private static final String JSON_ERROR_REPLACEABLE_STATUS;
    private static final String JSON_ERROR_REPLACEABLE_MESSAGE;
    private static final String JSON_MEDIATYPE;
    
    static {
        JSON_MEDIATYPE = "application/json";
        JSON_ERROR_REPLACEABLE_STATUS = "[STATUS]";
        JSON_ERROR_REPLACEABLE_MESSAGE = "[MESSAGE]";
        JSON_ERROR_TEMPLATE = 
                "{" +
                    "\"type\":\"error\", " +
                    "\"status\":\"" + JSON_ERROR_REPLACEABLE_STATUS + "\"," +
                    "\"message\":\"" + JSON_ERROR_REPLACEABLE_MESSAGE + "\"" +
                "}";
    }
    
    private final Jsonizer jsonizer;
    
    public ResponseJsonWriter(Jsonizer jsonizer) {
        this.jsonizer = jsonizer;
    }
    
    public void writeJsonErrorAndClose(int status, String message, HttpServletResponse response) 
            throws IOException {
        response.setContentType(JSON_MEDIATYPE);
        response.getWriter().write(this.prepareErrorJson(status, message));
        response.getWriter().close();
    }
    
    public void writeJsonAnswerAndClose(Object o, HttpServletResponse response) 
            throws IOException {
        response.setContentType(JSON_MEDIATYPE);
        response.getWriter().write(this.jsonizer.jsonize(o));
        response.getWriter().close();
    }
    
    private String prepareErrorJson(int status, String message) {
        return JSON_ERROR_TEMPLATE
                .replace(JSON_ERROR_REPLACEABLE_MESSAGE, message)
                .replace(JSON_ERROR_REPLACEABLE_STATUS, valueOf(status));
    }
}
