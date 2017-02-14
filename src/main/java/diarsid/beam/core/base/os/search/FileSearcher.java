/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.nio.file.Path;

import diarsid.beam.core.base.os.search.result.FileSearchResult;

/**
 *
 * @author Diarsid
 */
public interface FileSearcher {
    
    public static FileSearcher getSearcherWithDepthsOf(
            int depthOfSearchByName, int depthOfSearchByPath) {
        FileSearchByPathPatternReusableFileVisitor visitorByPath = 
                new FileSearchByPathPatternReusableFileVisitor();
        FileSearchByNamePatternReusableFileVisitor visitorByName = 
                new FileSearchByNamePatternReusableFileVisitor();
        return new FileSearcherService(
                depthOfSearchByName, depthOfSearchByPath, visitorByName, visitorByPath);
    } 
    
    FileSearchResult findStrictly(String strictTarget, String location, FileSearchMode mode);
    
    FileSearchResult findStrictly(String strictTarget, Path location, FileSearchMode mode);

    FileSearchResult find(String target, String location, FileSearchMode mode);

    FileSearchResult find(String target, Path location, FileSearchMode mode);    
}
