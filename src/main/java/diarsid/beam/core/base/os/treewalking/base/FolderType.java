/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.base;

/**
 *
 * @author Diarsid
 */
public enum FolderType { 
    
    PROGRAM_FOLDER (true),
    PROJECT_FOLDER (true),
    RESTRICTED_FOLDER (true),
    LIST_OF_EXECUTABLES (false),
    USUAL_FOLDER (false);
    
    private final boolean isRestricted;
    
    private FolderType(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }
    
    public boolean isNotRestricted() {
        return ! this.isRestricted;
    }
    
    public boolean isRestricted() {
        return this.isRestricted;
    }
}
