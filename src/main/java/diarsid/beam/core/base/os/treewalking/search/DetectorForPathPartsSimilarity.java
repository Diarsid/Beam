/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import diarsid.beam.core.base.analyze.similarity.SimilarityCheckSession;

import static java.lang.System.arraycopy;

import static diarsid.beam.core.base.util.MathUtil.halfRoundUp;
import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;
import static diarsid.beam.core.base.util.StringUtils.joining;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.support.objects.Pools.giveBackToPool;
import static diarsid.support.objects.Pools.takeFromPool;

/**
 *
 * @author Diarsid
 */
class DetectorForPathPartsSimilarity extends NameDetector<String[]> {
    
    private final String[] searchedPathPartsCopy;
    private final SimilarityCheckSession session;

    DetectorForPathPartsSimilarity(String[] searchedPthParts) {
        super(searchedPthParts);
        this.searchedPathPartsCopy = new String[searchedPthParts.length];
        this.session = takeFromPool(SimilarityCheckSession.class);
    }

    @Override
    boolean isMatch(Path testedPath) {
        return this.filterByPathPartsSimilarity(splitToParts(testedPath));
    }
    
    @Override
    void close() {
        giveBackToPool(this.session);
    }
    
    private void fillSearchedPathPartsCopy() {
        arraycopy(super.itemToFind, 0, this.searchedPathPartsCopy, 0, super.itemToFind.length);
    }
    
    private boolean filterByPathPartsSimilarity(String[] realPathParts) {
        if ( realPathParts.length == 0 ) {
            return false;
        }
        if ( realPathParts.length < this.searchedPathPartsCopy.length ) {
            return false;
        }
        
        this.fillSearchedPathPartsCopy();
        String realPart;
        String searchedPart;
        int foundQty = 0;
        
        for (int realIndex = 0; realIndex < realPathParts.length; realIndex++) {
            realPart = realPathParts[realIndex];
            if ( foundQty == this.searchedPathPartsCopy.length ) {
                break;
            }
            for (int searchedIndex = 0; searchedIndex < this.searchedPathPartsCopy.length; searchedIndex++) {
                searchedPart = this.searchedPathPartsCopy[searchedIndex];
                if ( nonEmpty(searchedPart) ) {
                    if ( containsIgnoreCase(realPart, searchedPart) || 
                            this.session.isSimilar(realPart, searchedPart) ) {
                        foundQty++;
                        realPathParts[realIndex] = "";
                        this.searchedPathPartsCopy[searchedIndex] = "";
                    }  
                }                                
            }            
        }
        
        if ( foundQty == this.searchedPathPartsCopy.length ) {
            return true;
        }
        if (    (this.searchedPathPartsCopy.length - foundQty) <= 
                halfRoundUp(this.searchedPathPartsCopy.length) ) {
            boolean similar = this.session.isSimilar(
                    joining(realPathParts), joining(this.searchedPathPartsCopy));
            return similar;            
        }
        return false;
    }
}
