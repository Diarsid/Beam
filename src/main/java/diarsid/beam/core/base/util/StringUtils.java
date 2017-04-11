/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author Diarsid
 */
public class StringUtils {
    
    private final static RandomHexadecimalStringGenerator GENERATOR;
    
    static {
        GENERATOR = new RandomHexadecimalStringGenerator();
    }
    
    private StringUtils() {
    }
    
    public static String randomString(int length) {
        return GENERATOR.randomString(length);
    }
    
    public static String lower(String target) {
        return target.toLowerCase(ENGLISH);
    }
    
    public static List<String> lower(List<String> targets) {
        return targets
                .stream()
                .map(target -> lower(target))
                .collect(toList());
    }
    
    public static String upper(String target) {
        return target.toUpperCase(ENGLISH);
    }
    
    public static boolean nonNullNonEmpty(String s) {
        return nonNull(s) && ! s.isEmpty();
    }
    
    public static boolean nonEmpty(String s) {
        return ! s.isEmpty();
    }
    
    public static String normalizeSpaces(String target) {
        return target.replaceAll("\\s+", " ");
    }
    
    public static String normalize(String target) {
        return target
                // replaces multiple spaces with single space
                .replaceAll("\\s+", " ")
                // eliminates wildcards before pattern: -xxx -> xxx
                .replaceAll(" -+", " ")
                // replaces multiple wildcard with single: xxx---yyy -> xxx-yyy
                .replaceAll("-+", "-")
                // eliminates trailing wildcards: xxx-yyy- -> xxx-yyy
                .replaceAll("-+ ", " ")
                .replaceAll("_+", "_")
                .replaceAll(" _+", " ")
                .replaceAll("_+ ", "_")
                .replaceAll("-$", "")
                .replaceAll("_$", "")
                .trim();
    }

    public static List<String> splitByWildcard(String target) {        
        return stream(target.split("-+"))
                .filter(string -> !string.matches("\\s+"))
                .map(string -> string.trim())
                .collect(toList());
    }
    
    public static String removeWildcards(String target) {
        return target.replaceAll("-+", "");
    }

    public static String[] splitBySpaces(String target) {
        return target.split("\\s+");
    }

    public static int countSpaces(String target) {
        // TODO
        // bad code, should use more efficient function here
        return target.split("\\s+").length - 1;
    }

    public static List<String> splitBySpacesToList(String target) {
        return new ArrayList<>(asList(target.split("\\s+")));
    }
}
