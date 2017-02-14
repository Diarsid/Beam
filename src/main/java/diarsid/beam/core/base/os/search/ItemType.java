/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.search;

import java.nio.file.Path;

import static java.nio.file.Files.isDirectory;

/**
 *
 * @author Diarsid
 */
enum ItemType {
    FILE,
    FOLDER;
    
    static ItemType typeOf(Path path) {
        if ( isDirectory(path) ) {
            return FOLDER;
        } else {
            return FILE;
        }
    }
}
