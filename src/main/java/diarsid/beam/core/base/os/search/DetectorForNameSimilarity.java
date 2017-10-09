/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.nio.file.Path;

import static diarsid.beam.core.base.analyze.similarity.Similarity.isSimilar;
import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class DetectorForNameSimilarity extends Detector<String> {

    public DetectorForNameSimilarity(String nameToFind) {
        super(nameToFind);
    }

    @Override
    boolean isMatch(Path testedPath) {                
        String testedName = asName(testedPath);
        if ( containsIgnoreCase(testedName, super.itemToFind) ) {
            return true;
        } else {
            return isSimilar(testedName, super.itemToFind);
        }
    }
}
