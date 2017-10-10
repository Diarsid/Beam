/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.nio.file.Path;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.util.PathUtils.removeSeparators;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class DetectorForSubpathSimilarity extends Detector<String> {

    public DetectorForSubpathSimilarity(String nameToFind) {
        super(nameToFind);
    }

    @Override
    boolean isMatch(Path testedPath) {
        String testedPathString = removeSeparators(testedPath.toString());
        if ( containsIgnoreCase(testedPathString, super.itemToFind) ) {
            return true;
        } else {
            return isSimilar(testedPathString, super.itemToFind);
        }
    }
}