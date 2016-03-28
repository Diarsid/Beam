/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServlet;

import diarsid.beam.core.exceptions.WorkflowBrokenException;
import diarsid.beam.core.modules.handlers.WebPagesHandler;
import diarsid.beam.core.modules.web.ServletData;

import static diarsid.beam.core.entities.WebPage.WEB_NAME_REGEXP;

/**
 *
 * @author Diarsid
 */
enum RestResourcesForWebPages {
    
    DIRS_IN_PLACEMENT (
            "allDirectories",
            "/resources/{placement}/dirs",
            "/resources/(webpanel|bookmarks)/dirs",
            AllDirectoriesServlet.class),
    
    DIR_FROM_DIRS_IN_PLACEMENT (
            "singleDirInDirs",
            "/resources/{placement}/dirs/{dir_name}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP,
            SingleDirectoryServlet.class),
    
    DIR_FIELDS_FROM_DIRS_IN_PLACEMENT (
            "dirFields",
            "/resources/{placement}/dirs/{dir_name}/{dir_field}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/(name|order)",
            DirectoryFieldsServlet.class),
    
    ALL_PAGES_IN_DIR_IN_PLACEMENT (
            "pagesInDir",
            "/resources/{placement}/dirs/{dir_name}/pages",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages",
            AllPagesInDirectoryServlet.class),
    
    PAGE_FROM_DIR_IN_PLACEMENT (
            "singlePageInDir",
            "/resources/{placement}/dirs/{dir_name}/pages/{page_name}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages/"+WEB_NAME_REGEXP,
            SinglePageInDirectoryServlet.class),
    
    PAGE_FIELDS_FROM_DIR_IN_PLACEMENT (
            "pageFields",
            "/resources/{placement}/dirs/{dir_name}/pages/{page_name}/{page_field}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages/"+WEB_NAME_REGEXP+"/(name|url|directory_and_placement|order)",
            PageFieldsServlet.class);
    
    private RestResourcesForWebPages(
            String servletName, String mapping, String regexp, Class servletClass) {
        this.urlMapping = mapping;
        this.urlRegexp = regexp;
        this.resourceServletName = servletName;
        this.resourceServletClass = servletClass;
    }
    
    private final String resourceServletName;
    private final String urlMapping;
    private final String urlRegexp;    
    private final Class resourceServletClass;
    
    ServletData resourceServletData(
            WebPagesHandler handler, PathResolver resolver) {
        
        return new ServletData(
                this.servlet(handler, resolver), 
                resourceServletName, 
                urlMapping);
    }
    
    private HttpServlet servlet(WebPagesHandler handler, PathResolver resolver) {        
        try {
            Constructor cons = this.resourceServletClass
                    .getDeclaredConstructor(
                            WebPagesHandler.class, PathResolver.class);
            cons.setAccessible(true);
            return (HttpServlet) cons.newInstance(handler, resolver);
        } catch (NoSuchMethodException|
                InstantiationException|
                IllegalAccessException|
                InvocationTargetException e) {
            e.printStackTrace();
            throw new WorkflowBrokenException();
        }      
    }
    
    static String getDispatchedServletNameOfResource(String requestedURL) {
        for (RestResourcesForWebPages resource : values()) {
            if (requestedURL.matches(resource.urlRegexp)) {
                return resource.resourceServletName;
            }
        }
        return "";
    }   
}
