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
    
    public static FileSearcher searcherWithDepthsOf(
            int depthOfSearchByName, int depthOfSearchByPath) {
        FilesCollector filesCollector = new FilesCollector(depthOfSearchByName, depthOfSearchByPath);
        return new FileSearcherService(filesCollector);
    } 
    
    FileSearchResult find(
            String target, String location, FileSearchMatching matching, FileSearchMode mode);
    
    FileSearchResult find(
            String target, Path location, FileSearchMatching matching, FileSearchMode mode);
}
