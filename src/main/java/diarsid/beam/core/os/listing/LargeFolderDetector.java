/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.os.listing;

import java.nio.file.Path;

/**
 *
 * @author Diarsid
 */
class LargeFolderDetector {
    
    private final int sizeToCountFolderAsLarge;
    
    LargeFolderDetector(int largeSize) {
        this.sizeToCountFolderAsLarge = largeSize;
    }
    
    boolean examine(Path folder) {
        int size = folder.toFile().list().length;
        return ( size >= this.sizeToCountFolderAsLarge );
    }
}
