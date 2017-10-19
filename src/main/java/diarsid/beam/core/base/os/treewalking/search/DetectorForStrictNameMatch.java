/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

import static diarsid.beam.core.base.util.PathUtils.asName;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
class DetectorForStrictNameMatch extends NameDetector<String> {

    public DetectorForStrictNameMatch(String nameToFind) {
        super(nameToFind);
    }

    @Override
    public boolean isMatch(Path testedPath) {
        return lower(asName(testedPath)).startsWith(lower(super.itemToFind));
    }
}
