/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.control.io.interpreter.ControlKeys.wordIsAcceptable;
import static diarsid.beam.core.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;

/**
 *
 * @author Diarsid
 */
public class PathUtils {
        
    public static final List<String> UNACCEPTABLE_FILEPATH_CHARS = unmodifiableList(asList(
            "~", "?", "$", "@", "!", 
            "#", "%", "^", "{", "}", 
            "*", ";", "`", "+", "=",
            "\""
    ));
    
    private PathUtils() {
    }
    
    public static boolean pathIsDirectory(Path dir) {
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
    
    public static Path combinePathFrom(String... fragments) {
        return Paths.get(normalizeSeparators(join("/", fragments)));
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
    
    public static boolean containsPathSeparator(String target) {
        return target.contains("/") || target.contains("\\");
    }
    
    public static boolean isAcceptableWebPath(String target) {        
        try {            
            // validate possible url.
            return ! new URL(target).toURI().toString().isEmpty();
        } catch (MalformedURLException|URISyntaxException e) {
            return false;
        }
    }
    
    public static boolean isAcceptableFilePath(String target) {
        return 
                ! containsIgnoreCaseAnyFragment(target, UNACCEPTABLE_FILEPATH_CHARS) &&
                containsPathSeparator(target);
    }
    
    public static boolean isAcceptableRelativePath(String target) {
        return ( 
                wordIsAcceptable(target) &&
                indexOfFirstFileSeparator(target) > 1 && 
                indexOfLastFileSeparator(target) < target.length() - 2);
    }
    
    public static String extractLocationFromPath(String path) {
        return path.substring(0, indexOfFirstFileSeparator(path));
    }
    
    public static String extractTargetFromPath(String path) {
        return path.substring(indexOfFirstFileSeparator(path) + 1);
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
    
    public static String normalizeArgument(String target) {
        return normalizeDashes(normalizeSeparators(target));
    }
    
    private static String normalizeDashes(String target) {
        return target.replaceAll("[-]+", "-");
    }

    public static String normalizeSeparators(String target) {
        return target.replaceAll("[/\\\\]+", "/");
    }
    
    public static String[] normalizePathFragmentsFrom(String target) {
        target = normalizeArgument(target);
        target = trimSeparatorsInBothEnds(target);
        return target.split("/");
    }    
    
    
    public static String relativizeFileName(Path root, Path file) {
        return root
                .relativize(file)
                .normalize()
                .toString()
                .replace("\\", "/");
    }
}
