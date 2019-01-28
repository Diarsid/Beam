/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import diarsid.beam.core.base.analyze.similarity.Similarity;
import diarsid.beam.core.base.analyze.variantsweight.Analyze;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;
import diarsid.beam.core.base.os.treewalking.base.FolderTypeDetector;
import diarsid.beam.core.modules.responsivedata.ResponsiveDaoPatternChoices;
import diarsid.support.objects.Pool;
import diarsid.support.objects.Pools;

/**
 *
 * @author Diarsid
 */
public interface Walker {
    
    static Walker newWalker(
            InnerIoEngine ioEngine, 
            FolderTypeDetector folderTypeDetector,
            Analyze analyze,
            Similarity similarity,
            Pools pools) {
        Pool<WalkState> walkStatePool = pools.createPool(
                WalkState.class, 
                () -> new WalkState(analyze));
        return new FileTreeWalker(ioEngine, similarity, folderTypeDetector, walkStatePool);
    }
    
    static Walker newWalker(
            InnerIoEngine ioEngine, 
            ResponsiveDaoPatternChoices daoPatternChoices, 
            FolderTypeDetector folderTypeDetector,
            Analyze analyze,
            Similarity similarity,
            Pools pools) {
        Pool<WalkState> walkStatePool = pools.createPool(
                WalkState.class, 
                () -> new WalkState(analyze));
        return new FileTreeWalker(
                ioEngine, similarity, daoPatternChoices, folderTypeDetector, walkStatePool);
    }
    
    WalkingToFind walkToFind(String pattern);
    
    Walker lookingFor(FileSearchMode mode);    
}
