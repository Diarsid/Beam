/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.advanced;

import java.io.File;
import java.util.List;

/**
 *
 * @author Diarsid
 */
class WalkUtil {
    
    static void addListedFilesTo(List<File> whereToAdd, File[] filesToAdd) {
        if ( filesToAdd.length == 0 ) {
            return;
        }
        for (File file : filesToAdd) {
            whereToAdd.add(file);
        }
    }
    
}
