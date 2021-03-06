/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.os.treewalking.search;

import diarsid.beam.core.base.os.treewalking.base.FileSearchMode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Diarsid
 */
interface FilesCollector {
    
    List<String> collectAll(
            Path root, FileSearchMode mode) throws IOException;

    List<String> collectByNameOrSubpathPatternSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByPathPartsSimilarity(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByStrictName(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;

    List<String> collectByWholeNamePattern(
            Path root, String nameToFind, FileSearchMode mode) throws IOException;    
}
