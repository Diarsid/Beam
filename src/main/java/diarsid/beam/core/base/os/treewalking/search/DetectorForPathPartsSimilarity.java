/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.util.PathUtils.splitToParts;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class DetectorForPathPartsSimilarity extends NameDetector<String[]> {

    public DetectorForPathPartsSimilarity(String[] pathPartsToFind) {
        super(pathPartsToFind);
    }

    @Override
    boolean isMatch(Path testedPath) {
        return this.filterByPathPartsSimilarity(splitToParts(testedPath), super.itemToFind);
    }
    
    private boolean filterByPathPartsSimilarity(
            String[] realPathParts, String[] searchedPathParts) {
        int counter = 0;
        String searchedPart;
        if ( realPathParts.length == 0 ) {
            return false;
        }
        if ( realPathParts.length < searchedPathParts.length ) {
            return false;
        }
        
        for (String realPart : realPathParts) {
            if ( counter == searchedPathParts.length ) {
                break;
            }
            searchedPart = searchedPathParts[counter];
            if ( containsIgnoreCase(realPart, searchedPart) || 
                 isSimilar(realPart, searchedPart) ) {
                counter++;
            } 
        }
        
        return ( counter == searchedPathParts.length );
    }
}
