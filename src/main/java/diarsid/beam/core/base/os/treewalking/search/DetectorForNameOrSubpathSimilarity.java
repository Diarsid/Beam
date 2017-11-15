/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import diarsid.beam.core.base.analyze.similarity.SimilarityCheckSession;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class DetectorForNameOrSubpathSimilarity extends NameDetector<String> {

    private final SimilarityCheckSession session;
    
    public DetectorForNameOrSubpathSimilarity(String nameToFind) {
        super(nameToFind);
        this.session = new SimilarityCheckSession();
    }

    @Override
    boolean isMatch(Path testedPath) {                
        String testedName = asName(testedPath);
        if ( containsIgnoreCase(testedName, super.itemToFind) ) {
            return true;
        } else {
            if ( isSimilar(testedName, super.itemToFind) ) {
                return true;
            } else {
                String testedPathString = removeSeparators(testedPath.toString());
                if ( containsIgnoreCase(testedPathString, super.itemToFind) ) {
                    return true;
                } else {
                    return isSimilar(testedPathString, super.itemToFind);
                }
            }
        }
    }
}