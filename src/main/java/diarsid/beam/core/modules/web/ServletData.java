/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web;

import java.util.Objects;

import javax.servlet.http.HttpServlet;

/**
 *
 * @author Diarsid
 */
public class ServletData {
    
    private final HttpServlet servlet;
    private final String servletMapping;
    private final String servletName;
    
    public ServletData(HttpServlet serv, String name, String url) {
        this.servlet = serv;
        this.servletName = name;
        this.servletMapping = url;
    }

    public HttpServlet getServlet() {
        return this.servlet;
    }

    public String getServletMapping() {
        return this.servletMapping;
    }

    public String getServletName() {
        return this.servletName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.servlet);
        hash = 37 * hash + Objects.hashCode(this.servletMapping);
        hash = 37 * hash + Objects.hashCode(this.servletName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServletData other = (ServletData) obj;
        if (!Objects.equals(this.servlet, other.servlet)) {
            return false;
        }
        if (!Objects.equals(this.servletMapping, other.servletMapping)) {
            return false;
        }
        if (!Objects.equals(this.servletName, other.servletName)) {
            return false;
        }
        return true;
    }  
}
