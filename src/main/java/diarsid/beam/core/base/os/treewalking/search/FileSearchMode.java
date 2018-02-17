/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

import static diarsid.beam.core.base.os.treewalking.search.ItemType.FILE;
import static diarsid.beam.core.base.os.treewalking.search.ItemType.FOLDER;
import static diarsid.beam.core.base.os.treewalking.search.ItemType.fileItemTypeOf;

/**
 *
 * @author Diarsid
 */
public enum FileSearchMode {
    
    ALL (FILE, FOLDER),
    FILES_ONLY (FILE),
    FOLDERS_ONLY (FOLDER);

    private FileSearchMode(ItemType... permittedTypes) {
        this.permittedTypes = new HashSet<>(asList(permittedTypes));
    }
    
    private Set<ItemType> permittedTypes;
    
    boolean correspondsTo(ItemType type) {
        return this.permittedTypes.contains(type);
    }
    
    public boolean correspondsTo(Path path) {
        return this.permittedTypes.contains(fileItemTypeOf(path));
    }
    
    public boolean correspondsTo(File file) {
        return this.permittedTypes.contains(fileItemTypeOf(file));
    }
}
