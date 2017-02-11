/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.core.container;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import static java.lang.String.format;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 *
 * @author Diarsid
 */
public class ResourceResponse {
    
    private final HttpServletResponse servletResponse;

    public ResourceResponse(HttpServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }
    
    void sendHttpMethodNotSupported(String method) throws IOException {
        this.servletResponse.setStatus(SC_METHOD_NOT_ALLOWED);
        this.servletResponse.setContentType("text/html;charset=UTF-8");
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
    
    void writeJson(String json) throws IOException {
        this.servletResponse.setStatus(SC_OK);
        this.servletResponse.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = this.servletResponse.getWriter()) {
            writer.println(format("{\"type\":\"%s\"}", json));
        }
    }
}
