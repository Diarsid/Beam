/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.executor.os.search;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

import static diarsid.beam.core.modules.executor.os.search.ItemType.FILE;
import static diarsid.beam.core.modules.executor.os.search.ItemType.FOLDER;

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
}
