/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.base;

import java.io.File;
import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;

/**
 *
 * @author Diarsid
 */
public enum ItemType {
    
    FILE,
    FOLDER;
    
    public static ItemType itemTypeOf(Path path) {
        if ( isDirectory(path) ) {
            return FOLDER;
        } else {
            return FILE;
        }
    }
    
    public static ItemType itemTypeOf(File file) {
        if ( file.isDirectory() ) {
            return FOLDER;
        } else {
            return FILE;
        }
    }
}
