/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import diarsid.support.strings.replace.Replace;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import static diarsid.support.strings.replace.Replace.replace;

/**
 *
 * @author Diarsid
 */
public class StringUtils {
    
    private final static RandomHexadecimalStringGenerator GENERATOR;
    private final static Predicate<String> NON_EMPTY;
    private static final Replace ALL_SEPARATORS_REPLACE;
    
    static {
        GENERATOR = new RandomHexadecimalStringGenerator();
        NON_EMPTY = s -> nonEmpty(s);
        ALL_SEPARATORS_REPLACE = replace()
                .regexToString("[/\\\\]+", "")
                .regexToString("-+", "")
                .regexToString("\\s+", "")
                .regexToString("_+", "");
    }
    
    private StringUtils() {
    }
    
    public static Predicate<String> nonEmpty() {
        return NON_EMPTY;
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
    
    public static boolean nonEmpty(CharSequence s) {
        return nonNull(s) && s.length() > 0;
    }
    
    public static boolean isEmpty(CharSequence s) {
        return isNull(s) || s.length() == 0;
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
    
    public static String removeAllSeparators(String target) {
        return ALL_SEPARATORS_REPLACE.doFor(target);
    }

    public static int countSpaces(String target) {
        // bad code, should use more efficient function here
        // possible dead code
        return target.split("\\s+").length - 1;
    }

    public static List<String> splitBySpacesToList(String target) {
        return new ArrayList<>(asList(target.split("\\s+")));
    }
    
    public static List<String> splitToLines(String multilines) {
        return new ArrayList<>(asList(multilines.split("\\r?\\n")));
    }
    
    public static String joinFromIndex(int start, List<String> list) {
        return join(" ", list.subList(start, list.size()));
    }
    
    public static int indexOfAny(String whereToSearch, String... any) {
        return stream(any)
                .mapToInt(oneOfAny -> whereToSearch.indexOf(oneOfAny))
                .filter(index -> index > -1)
                .findFirst()
                .orElse(-1);
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
    
    public static String joining(String... strings) {
        return join("", strings);
    }
    
    public static boolean haveEqualLength(String one, String two) {
        return one.length() == two.length();
    }
    
    public static void purge(StringBuilder stringBuilder) {
        stringBuilder.delete(0, stringBuilder.length());
    }
    
    public static void replaceAll(StringBuilder sb, String whatToReplace, String replacement) {
        int index = sb.indexOf(whatToReplace, 0);
        int whatToReplaceLength = whatToReplace.length();
        int replacementLength = replacement.length();
        while ( index >= 0 ) {
            sb.replace(index, index + whatToReplaceLength, replacement);
            index = sb.indexOf(whatToReplace, index + replacementLength);
        }
    }
    
    public static int countOccurences(String where, String what) {
        int count = 0;
        int lastOccurence = where.indexOf(what);
        while ( lastOccurence > -1 ) {
            count++;
            lastOccurence = where.indexOf(what, lastOccurence + 1);
        }
        return count;
    }
    
}
