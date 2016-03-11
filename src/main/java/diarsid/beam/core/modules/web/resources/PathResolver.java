/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.web.resources;

import javax.servlet.http.HttpServletRequest;

import diarsid.beam.core.entities.WebPagePlacement;

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
    
    WebPagePlacement extractPlacement(String path) {
        return WebPagePlacement.valueOf(
                path.substring(
                path.lastIndexOf("resources/") + "resources/".length())
                .toUpperCase()
        );
    }
    
    WebPagePlacement extractPlacementBeforeDirectory(String path) {
        return WebPagePlacement.valueOf(
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
                path.indexOf("/"));
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
        return entity.matches("[a-zA-Z0-9-_>\\s]+");
    }
}
