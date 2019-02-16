/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.base;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

import static diarsid.beam.core.base.os.treewalking.base.ItemType.FILE;
import static diarsid.beam.core.base.os.treewalking.base.ItemType.FOLDER;
import static diarsid.beam.core.base.os.treewalking.base.ItemType.itemTypeOf;

/**
 *
 * @author Diarsid
 */
public enum FileSearchMode {
    
    FILES_AND_FOLDERS (FILE, FOLDER),
    FILES_ONLY (FILE),
    FOLDERS_ONLY (FOLDER);
    
    private final Set<ItemType> permittedTypes;

    private FileSearchMode(ItemType... permittedTypes) {
        this.permittedTypes = new HashSet<>(asList(permittedTypes));
    }
    
    public boolean correspondsTo(ItemType type) {
        return this.permittedTypes.contains(type);
    }
    
    public boolean correspondsTo(Path path) {
        return this.permittedTypes.contains(itemTypeOf(path));
    }
    
    public boolean correspondsTo(File file) {
        return this.permittedTypes.contains(itemTypeOf(file));
    }
}
