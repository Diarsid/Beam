/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.resources;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServlet;

import diarsid.beam.core.util.Logs;
import diarsid.beam.core.exceptions.WorkflowBrokenException;

import old.diarsid.beam.core.modules.data.HandlerWebPages;

import diarsid.beam.core.modules.web.ServletData;

import static diarsid.beam.core.domain.entities.WebPage.WEB_NAME_REGEXP;

/**
 * Enum that contains full description of WebPages related web resources. 
 * 
 * @see {@link ResourcesProviderWorker } for usage details.
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
    
    /**
     * Enum fields.
     * Contain full information for each particular 
     * resource - its mapping-model, regexp that a request should match
     * in order to be mapped to this resource, servlet name used by 
     * the DispatcherServlet to forward the incoming request to 
     * this resource appropriate servlet.
     */
    private final String resourceServletName;
    private final String urlMapping;
    private final String urlRegexp;    
    private final Class resourceServletClass;
    
    /**
     * Assemble and return a ServletData object for a particular resource.
     * Objects, produced and returned by this method will be used to
     * inject servlets into underlying servlet container serving as a
     * server.
     * 
     * @param   handler     New handler instance as data source.
     * @param   resolver    New path resolver responsible to extract
     *                      necessary data from request path.
     * @return  ServletData object that represents full information 
     *                      about servlet as a representation of resource.
     */
    ServletData resourceServletData(
            HandlerWebPages handler, PathResolver resolver) {
        
        return new ServletData(
                this.servlet(handler, resolver), 
                resourceServletName, 
                urlMapping);
    }
    
    /**
     * Assemble concrete HttpServlet by a resource appropriate servlet 
     * class name using given handler and resolver.
     * 
     * @param handler
     * @param resolver
     * @return 
     */
    private HttpServlet servlet(HandlerWebPages handler, PathResolver resolver) {        
        try {
            Constructor cons = this.resourceServletClass
                    .getDeclaredConstructor(HandlerWebPages.class, PathResolver.class);
            cons.setAccessible(true);
            return (HttpServlet) cons.newInstance(handler, resolver);
        } catch (NoSuchMethodException|
                InstantiationException|
                IllegalAccessException|
                InvocationTargetException e) {
            Logs.logError(this.getClass(), "Resource HTTP Servlet creation failure.", e);
            throw new WorkflowBrokenException();
        }      
    }
    
    /**
     * The pivotal method of this enum.
     * 
     * Accepts a request url and returns a registration name 
     * of particular servlet that is responsible for serving requests 
     * related to this requested resource.
     * 
     * For example, for request url such as 
     * "host:port/app/resources/entities/123/order/5/content" 
     * this method will return servlet name looks like:
     * "OrderContent".
     * 
     * @param   requestedURL    Request url that needs to be dispatched to
     *                          the particular servlet.
     * @return                  Registration name of the particular servlet
     *                          represented the requested resource.
     */
    static String getDispatchedServletNameOfResource(String requestedURL) {
        for ( RestResourcesForWebPages resource : values() ) {
            if ( requestedURL.matches(resource.urlRegexp) ) {
                return resource.resourceServletName;
            }
        }
        return "";
    }   
}
