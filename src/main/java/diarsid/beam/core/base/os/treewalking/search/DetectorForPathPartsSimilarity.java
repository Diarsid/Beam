/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import diarsid.beam.core.base.analyze.similarity.SimilarityCheckSession;

import static java.lang.System.arraycopy;
import static java.util.Objects.nonNull;

import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

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
        this.session = new SimilarityCheckSession();
    }

    @Override
    boolean isMatch(Path testedPath) {
        return this.filterByPathPartsSimilarity(splitToParts(testedPath));
    }
    
    @Override
    void close() {
        this.session.close();
    }
    
    private void fillSearchedPathPartsLocal() {
        arraycopy(super.itemToFind, 0, this.searchedPathPartsCopy, 0, super.itemToFind.length);
    }
    
    private boolean filterByPathPartsSimilarity(String[] realPathParts) {
        if ( realPathParts.length == 0 ) {
            return false;
        }
        if ( realPathParts.length < this.searchedPathPartsCopy.length ) {
            return false;
        }
        
        this.fillSearchedPathPartsLocal();        
        String searchedPart;
        int counter = 0;
        
        for (String realPart : realPathParts) {
            if ( counter == this.searchedPathPartsCopy.length ) {
                break;
            }
            for (int i = 0; i < this.searchedPathPartsCopy.length; i++) {
                searchedPart = this.searchedPathPartsCopy[i];
                if ( nonNull(searchedPart) ) {
                    if ( containsIgnoreCase(realPart, searchedPart) || 
                            this.session.isSimilar(realPart, searchedPart) ) {
                        counter++;
                        this.searchedPathPartsCopy[i] = null;
                    }  
                }                                
            }            
        }
        
        return ( counter == this.searchedPathPartsCopy.length );
    }
}
