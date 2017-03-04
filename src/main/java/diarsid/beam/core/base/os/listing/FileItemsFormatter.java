/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.os.listing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static java.nio.file.Files.isDirectory;
import static java.util.Arrays.fill;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.PathUtils.asName;

/**
 *
 * @author Diarsid
 */
class FileItemsFormatter  {
    
    private static final String FOLDER_ICON = "[_] ";
    private static final String FILE_ICON = " -  ";
    
    public static final String INLINE_SKIPPED = "inline";
    public static final String NEW_LINE_SKIPPED = "newline";
    
    private final int singleIndentLength;
    private final Map<Integer, Integer> indents;
    private Path root;
    private List<String> results;
    private char[] indent;
    
    FileItemsFormatter() {
        this.singleIndentLength = 3;
        this.indents = new HashMap<>();
    }
    
    void presetWith(Path root) {
        this.results = new ArrayList<>();
        this.root = root;
    }
    
    void clear() {
        this.results = null;
        this.root = null;
        this.indent = null;
        this.indents.clear();
    }
    
    List<String> getResults() {
        this.reduceResultsIfTooLarge();
        //debug(join(" ", this.results));
        return this.results;
    }
    
    private void reduceResultsIfTooLarge() {
        int currentMaxIndent = this.getMaxResultsIndent();
        //debug("[FILE ITEMS FORMATTER] reducing...");
        while ( this.results.size() > 30 && currentMaxIndent > 1) {
            //debug("[FILE ITEMS FORMATTER] reducing by indent of " + currentMaxIndent);
            String indentToFilter = this.getIndentOf(currentMaxIndent);
            this.results = this.results
                    .stream()
                    .filter(s -> ! s.contains(indentToFilter))
                    .collect(toList());
            currentMaxIndent--;
        }
    }
    
    private int getMaxResultsIndent() {
        if ( this.indents.isEmpty() ) {
            return 0;
        } else {
            return this.indents.keySet().stream().max(Integer::compare).get();
        }        
    }
    
    void skipFolderWithMessage(Path skippedFolder, String skippedMessage, String mode) {
        if ( skippedFolder.equals(this.root) ) {
            this.results.add(skippedMessage);
            return;
        }
        if ( mode.equals(INLINE_SKIPPED) ) {
            this.results.add(this.getFormattedNameOf(skippedFolder)
                    .concat("  ").concat(skippedMessage));
        } else if ( mode.equals(NEW_LINE_SKIPPED) ) {
            this.results.add(this.getFormattedNameOf(skippedFolder));
            this.results.add(
                    this.getIndentOf(skippedFolder)
                            .concat("   ")
                            .concat(skippedMessage));
        }         
    }
    
    void skipFailedItem(Path skippedItem, String skippedItemDescription) {
        if ( isDirectory(skippedItem) ) {
            this.skipFolderWithMessage(skippedItem, skippedItemDescription, INLINE_SKIPPED);
        } else {
            this.results.add(
                    this.getFormattedNameOf(skippedItem)
                            .concat(" ")
                            .concat(skippedItemDescription));
        }
    }
    
    void includeItem(Path item) {
        if ( item.equals(this.root) ) {
            //debug("[FILE ITEMS FORMATTER] is root, not included.");
            return;
        }
        if ( asName(item).contains("desktop.ini") ) {
            //debug("[FILE ITEMS FORMATTER] desktop.ini, not included.");
            return;
        }
        this.results.add(this.getFormattedNameOf(item));
        //debug("[FILE ITEMS FORMATTER] included: " + this.getFormattedNameOf(item));
    }
    
    private String getFormattedNameOf(Path item) {
        String name = asName(item);
        if ( name.length() > 40 ) {
            name = name.substring(0, 37).concat("...");
        }
        return this.getIndentOf(item)
                .concat(this.getIconOf(item))
                .concat(name);
    }
    
    private int getIndentLengthOf(Path item) {
        int itemIndent = this.root.relativize(item).getNameCount() - 1;
        if ( this.indents.containsKey(itemIndent) ) {
            this.indents.computeIfPresent(itemIndent, (k, v) -> v++);
        } else {
            this.indents.put(itemIndent, 1);
        }
        return itemIndent;
    }
    
    private String getIndentOf(int length) {
        this.indent = new char[(this.singleIndentLength * length)];
        fill(this.indent, ' ');
        return valueOf(this.indent);
    }
    
    private String getIndentOf(Path item) {
        this.indent = new char[(this.singleIndentLength * this.getIndentLengthOf(item))];
        fill(this.indent, ' ');
        return valueOf(this.indent);
    }
    
    private String getIconOf(Path item) {
        if ( isDirectory(item) ) {
            return FOLDER_ICON;
        } else {
            return FILE_ICON;
        }
    }
}
