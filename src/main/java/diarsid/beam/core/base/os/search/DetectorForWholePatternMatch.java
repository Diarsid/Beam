/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.nio.file.Path;

import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCase;

/**
 *
 * @author Diarsid
 */
class DetectorForWholePatternMatch extends Detector<String> {

    public DetectorForWholePatternMatch(String nameToFind) {
        super(nameToFind);
    }

    @Override
    boolean isMatch(Path testedPath) {
        return containsIgnoreCase(asName(testedPath), super.itemToFind);
    }
}
