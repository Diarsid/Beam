/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.applicationhome;

import java.nio.file.Path;
import java.nio.file.Paths;

import diarsid.beam.core.os.search.FileSearcher;
import diarsid.beam.core.os.search.result.FileSearchResult;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;

import static diarsid.beam.core.os.search.FileSearchMode.FILES_ONLY;

/**
 *
 * @author Diarsid
 */
abstract class SearcheableCatalog {
    
    private final FileSearcher fileSearcher;
    private final Path catalogPath;
    
    SearcheableCatalog(String catalogPath, FileSearcher fileSearcher) {
        this.catalogPath = Paths.get(catalogPath);
        if ( ! isDirectory(this.catalogPath) ) {
            throw new IllegalArgumentException(
                    format("Catalog path '%s' is not directory.", catalogPath));
        }
        this.fileSearcher = fileSearcher;
    }
    
    protected FileSearchResult findFileInCatalogByPattern(String pattern) {
        return this.fileSearcher.find(pattern, this.catalogPath, FILES_ONLY);
    }
    
    protected FileSearchResult findFileInCatalogByStrictName(String strictName) {
        return this.fileSearcher.findStrictly(strictName, this.catalogPath, FILES_ONLY);
    }
    
    protected Path getPath() {
        return this.catalogPath;
    }
}
