/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Diarsid
 */
class FileSearchUtils {
    
    private FileSearchUtils() {
    }
    
    static boolean givenPathIsDirectory(Path dir) {
        return Files.exists(dir) && Files.isDirectory(dir);
    }
    
    
    static boolean containsFileSeparator(String target) {
        return target.contains("/") || target.contains("\\");
    }
    
    static String[] normalizePathFragmentsFrom(String target) {
        target = target.replaceAll("[/\\\\]+", "/");
        if (target.endsWith("/")) {
            target = target.substring(0, target.length()-1);
        }
        if (target.startsWith("/")) {
            target = target.substring(1);
        }
        return target.split("/");
    }    
    
    
    static String relativizeFileName(Path root, Path file) {
        return root
                .relativize(file)
                .normalize()
                .toString()
                .replace("\\", "/");
    }
}
