/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import static diarsid.beam.core.base.control.io.interpreter.ControlKeys.charsAreDomainAcceptable;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.StringIgnoreCaseUtil.containsIgnoreCaseAnyFragment;

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
    
    public static boolean pathIsDirectory(String path) {
        return Files.exists(Paths.get(path)) && Files.isDirectory(Paths.get(path));
    }
    
    public static boolean pathIsDirectory(Path dir) {
        return Files.exists(dir) && Files.isDirectory(dir);
    }
    
    public static int indexOfFirstPathSeparator(String target) {
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
    
    public static int indexOfNextPathSeparatorAfter(String target, String pattern) {
        int indexOfNextSlash = target.indexOf("/", target.indexOf(pattern));
        int indexOfNextBackSlash = target.indexOf("\\", target.indexOf(pattern));
        if ( indexOfNextBackSlash < 0 ) {
            return indexOfNextSlash;
        } else if ( indexOfNextSlash < 0 ) {
            return indexOfNextBackSlash;
        } else {
            return ( indexOfNextSlash < indexOfNextBackSlash ) ? indexOfNextSlash : indexOfNextBackSlash; 
        }
    } 
    
    public static String subpathToPattern(String target, String pattern) {
        if ( containsPathSeparator(pattern) ) {
            pattern = pattern.substring(indexOfLastPathSeparator(pattern) + 1, pattern.length());
        }
        return target.substring(0, indexOfNextPathSeparatorAfter(target, pattern));
    }
    
    public static int indexOfLastPathSeparator(String target) {
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
            return 
                    target.contains("/") && 
                    ! new URL(target).toURI().toString().isEmpty();
        } catch (MalformedURLException|URISyntaxException e) {
            return false;
        }
    }
    
    public static boolean isAcceptableFilePath(String target) {
        return 
                ! target.isEmpty() &&
                ! containsIgnoreCaseAnyFragment(target, UNACCEPTABLE_FILEPATH_CHARS) &&
                containsPathSeparator(target);
    }
    
    public static boolean isAcceptableRelativePath(String target) {
        return ( 
                charsAreDomainAcceptable(target) &&
                indexOfFirstPathSeparator(target) > 1 && 
                indexOfLastPathSeparator(target) < target.length() - 2);
    }
    
    public static String extractLocationFromPath(String path) {
        if ( path.isEmpty() ) {
            return "";
        } else if ( containsPathSeparator(path) ) {
            debug("[PATHS] extract location from : '" + path + "'");
            return path.substring(0, indexOfFirstPathSeparator(path));
        } else {
            return path;
        }       
    }
    
    public static String extractTargetFromPath(String path) {
        if ( path.isEmpty() ) {
            return "";
        } else if ( containsPathSeparator(path) ) {
            return path.substring(indexOfFirstPathSeparator(path) + 1);
        } else {
            return path;
        }
    }
    
    public static String trimSeparators(String target) {
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
    
    public static String cleanSeparators(String target) {
        return target.replaceAll("[/\\\\]+", "");
    }
    
    public static String[] splitPathFragmentsFrom(String target) {
        target = normalizeArgument(target);
        target = trimSeparators(target);
        return target.split("/");
    }    
    
    public static String[] splitToParts(Path path) {
        return normalizeSeparators(path.normalize().toString()).split("/");
    }
    
    public static String asRelativeString(Path root, Path file) {
        return root
                .relativize(file)
                .normalize()
                .toString()
                .replace("\\", "/");
    }
    
    public static String asName(Path file) {
        if ( file.getNameCount() > 0 ) {
            return file.getFileName().toString();
        } else {
            return file.toString();
        }
    }
    
    public static List<String> decomposePath(String path) {
        List<String> decomposedPaths = new ArrayList<>();
        path = normalizeArgument(path);
        decomposedPaths.add(path);
        while ( containsPathSeparator(path) ) {            
            path = path.substring(0, indexOfLastPathSeparator(path));
            decomposedPaths.add(path);
        }
        return decomposedPaths;
    }
}
