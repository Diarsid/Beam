/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.io.File;
import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;

/**
 *
 * @author Diarsid
 */
enum ItemType {
    
    FILE,
    FOLDER;
    
    static ItemType fileItemTypeOf(Path path) {
        if ( isDirectory(path) ) {
            return FOLDER;
        } else {
            return FILE;
        }
    }
    
    static ItemType fileItemTypeOf(File file) {
        if ( file.isDirectory() ) {
            return FOLDER;
        } else {
            return FILE;
        }
    }
}
