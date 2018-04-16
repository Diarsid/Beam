/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.application.environment;

import java.nio.file.Path;
import java.nio.file.Paths;

import diarsid.beam.core.base.os.treewalking.search.FileSearcher;
import diarsid.beam.core.base.os.treewalking.search.result.FileSearchResult;

import static java.lang.String.format;
import static java.nio.file.Files.isDirectory;

import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.DIRECT_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.PATTERN_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.SIMILAR_MATCH;
import static diarsid.beam.core.base.os.treewalking.search.FileSearchMatching.STRICT_MATCH;
import static diarsid.beam.core.base.os.treewalking.base.FileSearchMode.FILES_ONLY;

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
    
    protected FileSearchResult findFileInCatalogByDirectName(String name) {
        return this.fileSearcher.find(name, this.catalogPath, DIRECT_MATCH, FILES_ONLY);
    }
    
    protected FileSearchResult findFileInCatalogByPattern(String pattern) {
        return this.fileSearcher.find(pattern, this.catalogPath, PATTERN_MATCH, FILES_ONLY);
    }
    
    protected FileSearchResult findFileInCatalogByStrictName(String strictName) {
        return this.fileSearcher.find(strictName, this.catalogPath, STRICT_MATCH, FILES_ONLY);
    }
    
    protected FileSearchResult findFileInCatalogByPatternSimilarity(String pattern) {
        return this.fileSearcher.find(pattern, this.catalogPath, SIMILAR_MATCH, FILES_ONLY);
    }
    
    protected FileSearchResult findAllFilesInCatalog() {
        return this.fileSearcher.findAll(this.catalogPath, FILES_ONLY);
    }
    
    protected Path getPath() {
        return this.catalogPath;
    }
}
