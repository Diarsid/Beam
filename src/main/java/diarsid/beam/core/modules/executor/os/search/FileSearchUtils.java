/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.executor.os.search;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    
    static boolean isValidPath(String file) {
        return Files.exists(Paths.get(file));
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
    
    public static int indexOfLastFileSeparator(String target) {
        int indexOfSlash = target.lastIndexOf("/");
        int indexOfBackSlash = target.lastIndexOf("\\");
        if ( indexOfBackSlash < 0 ) {
            return indexOfSlash;
        } else if ( indexOfSlash < 0 ) {
            return indexOfBackSlash;
        } else {
            return ( indexOfSlash > indexOfBackSlash) ? indexOfSlash : indexOfBackSlash; 
        }
    }
    
    public static boolean containsFileSeparator(String target) {
        return target.contains("/") || target.contains("\\");
    }
    
    public static String trimSeparatorsInBothEnds(String target) {
        if (target.endsWith("/")) {
            target = target.substring(0, target.length()-1);
        }
        if (target.startsWith("/")) {
            target = target.substring(1);
        }
        return target;
    }
    
    public static String normalizeSingleCommandParam(String target) {
        return target.replaceAll("[/\\\\]+", "/").replaceAll("[-]+", "-");
    }
    
    static String[] normalizePathFragmentsFrom(String target) {
        target = normalizeSingleCommandParam(target);
        target = trimSeparatorsInBothEnds(target);
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
