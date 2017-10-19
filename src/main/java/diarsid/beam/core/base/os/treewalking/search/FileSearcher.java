/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;

import static diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector.getFolderTypeDetector;

/**
 *
 * @author Diarsid
 */
public interface FileSearcher {
    
    public static FileSearcher searcherWithDepthsOf(int depthOfSearch) {
        return new FileSearcherService(new FilesCollectorByVisitor(
                depthOfSearch, getFolderTypeDetector()));
    } 
    
    FileSearchResult find(
            String target, String location, FileSearchMatching matching, FileSearchMode mode);
    
    FileSearchResult find(
            String target, Path location, FileSearchMatching matching, FileSearchMode mode);
}
