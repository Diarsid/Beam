/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.web.resources;

import static diarsid.beam.core.entities.WebPage.WEB_NAME_REGEXP;

/**
 *
 * @author Diarsid
 */
enum RestResources {
    
    ROOT (
            "dispatcher", 
            "/*", 
            "/*"),
    PLACEMENT (
            "placements",
            "/resources/{placement}",
            "/resources/(webpanel|bookmarks)"
    ),
    DIRS_IN_PLACEMENT (
            "allDirectories",
            "/resources/{placement}/dirs",
            "/resources/(webpanel|bookmarks)/dirs"),
    DIR_FROM_DIRS_IN_PLACEMENT (
            "singleDirInDirs",
            "/resources/{placement}/dirs/{dir_name}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP),
    DIR_FIELDS_FROM_DIRS_IN_PLACEMENT (
            "dirFields",
            "/resources/{placement}/dirs/{dir_name}/{dir_field}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/(name|order)"),
    ALL_PAGES_IN_DIR_IN_PLACEMENT (
            "pagesInDir",
            "/resources/{placement}/dirs/{dir_name}/pages",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages"),
    PAGE_FROM_DIR_IN_PLACEMENT (
            "singlePageInDir",
            "/resources/{placement}/dirs/{dir_name}/pages/{page_name}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages/"+WEB_NAME_REGEXP),
    PAGE_FIELDS_FROM_DIR_IN_PLACEMENT (
            "pageFields",
            "/resources/{placement}/dirs/{dir_name}/pages/{page_name}/{page_field}",
            "/resources/(webpanel|bookmarks)/dirs/"+WEB_NAME_REGEXP+"/pages/"+WEB_NAME_REGEXP+"/(name|url|directory_and_placement|order)");
    
    private RestResources(String servlet, String mapping, String regexp) {
        this.urlMapping = mapping;
        this.urlRegexp = regexp;
        this.resourceServletName = servlet;
    }
    
    private final String urlMapping;
    private final String urlRegexp;
    private final String resourceServletName;
    
    String servletMapping() {
        return urlMapping;
    }
    
    String urlRegex() {
        return urlRegexp;
    }
    
    String servletName() {
        return resourceServletName;
    }
    
    static String getDispatchedServletNameOfResource(String requestedURL) {
        for (RestResources resource : values()) {
            if (requestedURL.matches(resource.urlRegexp)) {
                return resource.resourceServletName;
            }
        }
        return "";
    }   
}
