/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import javax.servlet.http.HttpServletRequest;

import diarsid.beam.core.domain.entities.WebPlacement;

import static diarsid.beam.core.domain.entities.WebPage.WEB_NAME_REGEXP;

/**
 *
 * @author Diarsid
 */
class PathResolver {
    
    PathResolver() {
    }
    
    String getNormalizedPath(HttpServletRequest request) {
        return request.getRequestURL()
                .toString()
                .replace("%20"," ")
                .replace("%3E", ">");
    }
    
    WebPlacement extractPlacement(String path) {
        return WebPlacement.valueOf(
                path.substring(
                path.lastIndexOf("resources/") + "resources/".length())
                .toUpperCase()
        );
    }
    
    WebPlacement extractPlacementBeforeDirectory(String path) {
        return WebPlacement.valueOf(
                path.substring(
                path.lastIndexOf("resources/") + "resources/".length(), 
                path.indexOf("/dirs"))
                .toUpperCase()
        );
    }
    
    String extractDirectory(String path) {
        return path.substring(
                path.lastIndexOf("dirs/") + "dirs/".length());
    }
    
    String extractDirectoryBeforePages(String path) {
        return path.substring(
                path.lastIndexOf("dirs/") + "dirs/".length(),
                path.indexOf("/pages"));
    }
    
    String extractDirectoryBeforeFields(String path) {
        return path.substring(
                path.lastIndexOf("dirs/") + "dirs/".length(),
                path.lastIndexOf("/"));
    }
    
    String extractPage(String path) {
        return path.substring(
                path.lastIndexOf("pages/") + "pages/".length());
    }
    
    String extractPageBeforeField(String path) {
        return path.substring(
                path.lastIndexOf("pages/") + "pages/".length(),
                path.lastIndexOf("/"));
    }
    
    String extractPageField(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
    
    String extractDirectoryField(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
    
    boolean check(String entity) {
        if (entity != null) {
            return entity.matches(WEB_NAME_REGEXP);
        } else {
            return false;
        }
    }
}
