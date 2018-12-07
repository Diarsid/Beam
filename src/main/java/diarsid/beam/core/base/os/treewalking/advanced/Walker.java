/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;

/**
 *
 * @author Diarsid
 */
public interface Walker {
    
    static Walker newWalker(
            InnerIoEngine ioEngine, 
            FolderTypeDetector folderTypeDetector) {
        return new FileTreeWalker(ioEngine, folderTypeDetector);
    }
    
    static Walker newWalker(
            InnerIoEngine ioEngine, 
            ResponsiveDaoPatternChoices daoPatternChoices, 
            FolderTypeDetector folderTypeDetector) {
        return new FileTreeWalker(ioEngine, daoPatternChoices, folderTypeDetector);
    }
    
    WalkingToFind walkToFind(String pattern);
    
    Walker lookingFor(FileSearchMode mode);    
}
