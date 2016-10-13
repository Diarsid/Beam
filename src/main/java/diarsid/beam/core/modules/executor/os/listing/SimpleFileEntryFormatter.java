/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.listing;

import java.nio.file.Path;

/**
 *
 * @author Diarsid
 */
public class SimpleFileEntryFormatter {
    
    public SimpleFileEntryFormatter() {
    }
    
    String format(Path root, Path target, boolean isDir) {
        return this.format(
                target.getFileName().toString(), 
                isDir, 
                this.getIndentFor(root, target));
    }
    
    private String format(String name, boolean isDir, int indent) {
        
    }
    
    private int getIndentFor(Path root, Path target) {
        
    }
}
