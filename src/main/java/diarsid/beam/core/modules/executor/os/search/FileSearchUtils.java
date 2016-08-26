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
public class FileSearchUtils {
    
    private FileSearchUtils() {
    }
    
    static boolean givenPathIsDirectory(Path dir) {
        return Files.exists(dir) && Files.isDirectory(dir);
    }
    
    public static int indexOfFirstFileSeparator(String target) {
        int indexOfSlash = target.indexOf("/");
        int indexOfBackSlash = target.indexOf("\\");
        if ( indexOfBackSlash < 0 ) {
            return indexOfSlash;
        } else if ( indexOfSlash < 0 ) {
            return indexOfBackSlash;
        } else {
            return ( indexOfSlash < indexOfBackSlash) ? indexOfSlash : indexOfBackSlash; 
        }
    }
    
    public static boolean containsFileSeparator(String target) {
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
