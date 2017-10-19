/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.nio.file.Path;

/**
 *
 * @author Diarsid
 */
abstract class NameDetector <TYPE_TO_FIND> {
    
    final TYPE_TO_FIND itemToFind;

    NameDetector(TYPE_TO_FIND itemToFind) {
        this.itemToFind = itemToFind;
    }
    
    abstract boolean isMatch(Path testedPath);
}
