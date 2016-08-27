/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.search;

import diarsid.beam.core.modules.executor.os.search.result.FileSearchResult;

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
        return new FileIntelligentSearcher(
                depthOfSearchByName, depthOfSearchByPath, visitorByName, visitorByPath);
    } 

    FileSearchResult findTarget(String target, String location);    
}
