/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.isNull;
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
    
    public static boolean nonEmpty(String s) {
        return nonNull(s) && ! s.isEmpty();
    }
    
    public static boolean isEmpty(String s) {
        return isNull(s) || s.isEmpty();
    }
    
    public static String normalizeSpaces(String target) {
        return target.replaceAll("\\s+", " ").trim();
    }
    
    public static String removeWildcards(String target) {
        return target.replaceAll("-+", "");
    }

    public static String[] splitBySpaces(String target) {
        return target.split("\\s+");
    }

    public static int countSpaces(String target) {
        // bad code, should use more efficient function here
        // possible dead code
        return target.split("\\s+").length - 1;
    }

    public static List<String> splitBySpacesToList(String target) {
        return new ArrayList<>(asList(target.split("\\s+")));
    }
    
    public static String joinFromIndex(int start, List<String> list) {
        return join(" ", list.subList(start, list.size()));
    }
    
    public static boolean isWordsSeparator(char c) {
        return 
                c == '.' ||
                c == ',' ||
                c == ';' ||
                c == ':' ||
                c == ' ' || 
                c == '_' || 
                c == '-' || 
                c == '/' || 
                c == '\\';
    }
    
    public static boolean containsWordsSeparator(String target) {
        for (int i = 0; i < target.length(); i++) {
            if ( isWordsSeparator(target.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
