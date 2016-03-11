/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import diarsid.beam.core.entities.WebPagePlacement;
import diarsid.beam.core.modules.data.DaoWebPages;

/**
 *
 * @author Diarsid
 */
class PageFieldsServlet extends HttpServlet {
    
    private final DaoWebPages webDao;
    private final PathResolver resolver;
    private final JSONParser json;
            
    PageFieldsServlet(DaoWebPages webDao, PathResolver resolver) {
        this.webDao = webDao;
        this.resolver = resolver;
        this.json = new JSONParser();
    }    
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            String path = this.resolver.getNormalizedPath(request);
            String field = this.resolver.extractPageField(path);
            String page = this.resolver.extractPageBeforeField(path);
            String dir = this.resolver.extractDirectoryBeforePages(path);
            WebPagePlacement place = this.resolver.extractPlacementBeforeDirectory(path);
            JSONObject newValueObj = (JSONObject) this.json.parse(request.getReader());
            
            switch (field) {
                
                case "name" : {
                    String newName = (String) newValueObj.get("name");
                    if ( ! this.resolver.check(newName) ) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable page name format - contains forbidden charecters.");
                        response.getWriter().close();
                        return;
                    }
                    if (this.webDao.editWebPageName(page, newName)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("WebPage name has not been edited due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }
                
                case "url" : {
                    String newUrl = (String) newValueObj.get("url");
                    if (this.webDao.editWebPageUrl(page, newUrl)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("WebPage url has not been edited due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }
                
                case "directory_and_placement" : {
                    String newPlace = (String) newValueObj.get("placement");
                    WebPagePlacement placement;
                    if ( newPlace.matches("WEBPANEL|BOOKMARKS|webpanel|bookmarks") ) {
                        placement = WebPagePlacement.valueOf(newPlace.toUpperCase());
                    } else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable placement format.");
                        response.getWriter().close();
                        return;
                    }
                    String newDir = (String) newValueObj.get("directory");
                    if ( ! this.resolver.check(newDir) ) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable directory name format - contains forbidden charecters.");
                        response.getWriter().close();
                        return;
                    }
                    if (this.webDao.moveWebPageToPlacementAndDirectory(
                            page, newDir, placement)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("WebPage has not been moved to another location due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }
                
                case "order" : {
                    String newOrderStr = (String) newValueObj.get("order");
                    if ( ! newOrderStr.matches("[0-9]+")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Unaccaptable page order format - must contain only digits.");
                        response.getWriter().close();
                        return;
                    }
                    int newOrder = Integer.parseInt(newOrderStr);
                    if (this.webDao.editWebPageOrder(page, dir, place, newOrder)) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().close();
                        return;
                    } else {
                        response.setStatus(HttpServletResponse.SC_ACCEPTED);
                        response.setContentType("text/plain");
                        response.getWriter()
                                .write("WebPage order has not been edited due to unknown reasons.");
                        response.getWriter().close();
                        return;
                    }
                }
                
                default : {
                    //
                }
            }
            
        } catch (ParseException|NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write(e.toString());
            response.getWriter().close();
        }
    }
}
