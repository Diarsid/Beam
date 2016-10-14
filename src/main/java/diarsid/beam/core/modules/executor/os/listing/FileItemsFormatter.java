/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.listing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.fill;

/**
 *
 * @author Diarsid
 */
class FileItemsFormatter  {
    
    private static final String FOLDER_ICON = "[_] ";
    private static final String FILE_ICON = " o  ";
    
    private final int singleIndentLength;
    private Path root;
    private List<String> results;
    private char[] indent;
    
    FileItemsFormatter() {
        this.singleIndentLength = 3;
    }
    
    void presetWith(Path root) {
        this.results = new ArrayList<>();
        this.root = root;
    }
    
    void clear() {
        this.results = null;
        this.root = null;
        this.indent = null;
    }
    
    List<String> getResults() {
        return this.results;
    }
    
    void skipFolderWithMessage(Path skippedFolder, String skippedFolderDescription) {
        this.results.add(this.getFormattedNameOf(skippedFolder));
        this.results.add(
                this.getIndentOf(skippedFolder)
                        .concat("   ")
                        .concat(skippedFolderDescription));
    }
    
    void skipFailedItem(Path skippedItem, String skippedItemDescription) {
        if ( isDirectory(skippedItem) ) {
            this.skipFolderWithMessage(skippedItem, skippedItemDescription);
        } else {
            this.results.add(
                    this.getFormattedNameOf(skippedItem)
                            .concat(" ")
                            .concat(skippedItemDescription));
        }
    }
    
    void includeItem(Path item) {
        this.results.add(this.getFormattedNameOf(item));
    }
    
    private String getFormattedNameOf(Path item) {
        return this.getIndentOf(item)
                .concat(this.getIconOf(item))
                .concat(item.getFileName().toString());
    }
    
    private int getIndentNumberOf(Path item) {
        return this.root.relativize(item).getNameCount() - 1;
    }
    
    private String getIndentOf(Path item) {
        this.indent = new char[(this.singleIndentLength * this.getIndentNumberOf(item))];
        fill(this.indent, ' ');
        return item.getFileName().toString().concat(valueOf(this.indent));
    }
    
    private String getIconOf(Path item) {
        if ( isDirectory(item) ) {
            return FOLDER_ICON;
        } else {
            return FILE_ICON;
        }
    }
}
