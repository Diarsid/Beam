/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

import static diarsid.beam.core.base.util.MathUtil.isEven;
import static diarsid.beam.core.base.util.MathUtil.isOdd;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class SqlUtil {
    
    public static final String SQL_SINGLE_QUOTE_ESCAPE = "''";
    
    public static enum SqlOperator {
        
        AND (" AND "),
        OR (" OR ");
        
        private final String sql;
        
        private SqlOperator(String sql) {
            this.sql = sql;
        }
        
        public String getSql() {
            return this.sql;
        }
    }
    
    
    private SqlUtil() {
    }
    
    public static String lowerWildcardBefore(String part) {
        return "%" + lower(part);
    }
    
    public static String wildcardSpaceBefore(String lowerPart) {
        return "% " + lowerPart;
    }
    
    public static String lowerWildcardAfter(String part) {
        return lower(part) + "%";
    }
    
    public static String wildcardSpaceAfter(String lowerPart) {
        return lowerPart + " %";
    }
    
    public static String lowerWildcard(String part) {
        return "%" + lower(part) + "%";
    }
    
    public static String spacingWildcards(String lowerPart) {
        return "% " + lowerPart + " %";
    }
    
    public static List<String> lowerWildcardLists(List<String>... parts) {
        return stream(parts)
                .flatMap(partsList -> partsList.stream())
                .map(part -> lowerWildcard(part))
                .collect(toList());
    }
    
    public static List<String> lowerWildcardList(List<String> parts) {
        return parts
                .stream()
                .map(part -> lowerWildcard(part))
                .collect(toList());
    }
    
    public static List<String> lowerWildcardListAnd(List<String> parts, String... additionals) {
        return Stream.concat(parts.stream(), stream(additionals))
                .map(part -> lowerWildcard(part))
                .collect(toList());
    }
    
    public static String multipleValues(int size) {
        return generate(() -> " ? ")
                .limit(size)
                .collect(joining(", ", " ( ", " ) "));
    }
    
    public static String patternCriteria(String pattern, String column) {
        String part1 = " CASE WHEN POSITION('"; 
        String part2 = "', "; 
        String part3 = ") > 0 THEN 1 ELSE 0 END ";
        StringBuilder statement = new StringBuilder();
        int cycle = pattern.length() - 1;
        for (int i = 0; i < cycle; i++) {
            statement
                    .append(part1)
                    .append(pattern.charAt(i))
                    .append(part2)
                    .append(column)
                    .append(part3)
                    .append(" + ");
        }
        statement
                .append(part1)
                .append(pattern.charAt(pattern.length() - 1))
                .append(part2)
                .append(column)
                .append(part3);
        
        return statement.toString();
    }
    
    public static String multipleLowerGroupedLikesAndOr(String column, int quantity) {
        String condition = "( LOWER(" + column + ") LIKE ? )";
        switch ( quantity ) {
            case 1 : {
                return "( " + condition + " )";
            }
            case 2 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 3 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + 
                        ") OR " + 
                        condition + 
                        " )";
            }
            case 4 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 5 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 6 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 7 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 8 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 9 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            case 10 : {
                return 
                        "( ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) )";
            }
            default : {                
                return 
                        "( ( " + 
                        multipleLowerLikeAnd(column, quantity - (quantity / 2)) + 
                        " ) OR ( " + 
                        multipleLowerLikeAnd(column, (quantity / 2)) + 
                        " ) )";
            }
        }    
    }
    
    public static String multipleLowerGroupedLikesOrAnd(String column, int quantity) {
        String condition = "( LOWER(" + column + ") LIKE ? )";
        switch ( quantity ) {
            case 1 : {
                return condition;
            }
            case 2 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 3 : {
                return 
                        "( ( " + 
                        condition + 
                        ") AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 4 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 5 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 6 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 7 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 8 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 9 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            case 10 : {
                return 
                        "( ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition  + 
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition +  
                        " ) AND ( " + 
                        condition + " OR " + 
                        condition + 
                        " ) )";
            }
            default : {
                String conditionsOr = " ( " + condition + " OR " + condition + " ) ";
                
                String fullConditon;
                if ( isOdd(quantity) ) {
                    fullConditon = generate(() -> conditionsOr)
                            .limit((quantity / 2) - 1)
                            .collect(joining(" AND "))
                            .concat(
                                    " AND ( " + 
                                    condition + " OR " + 
                                    condition + " OR " + 
                                    condition + 
                                    " ) ");
                } else {
                    fullConditon = generate(() -> conditionsOr)
                            .limit(quantity / 2)
                            .collect(joining(" AND "));
                }
                
                return " ( " + fullConditon + " ) ";
            }
        }
    }
    
    public static String multipleLowerLike(
            String column, int partsSize, SqlOperator sqlOperator) {
        return generate(() -> "( LOWER(column) LIKE ? )")
                .limit(partsSize)
                .map(condition -> condition.replace("column", column))
                .collect(joining(sqlOperator.getSql(), " ", " "));
    }
    
    public static String multipleLowerLikeAnd(String column, int length) {
        String condition = "( LOWER(" + column + ") LIKE ? )";
        switch (length) {
            case 1 : {
                return condition;
            }
            case 2 : {
                return 
                        condition + " AND " + 
                        condition;
            }
            case 3 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 4 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 5 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 6 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 7 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 8 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 9 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            case 10 : {
                return 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition;
            }
            default : {
                return multipleLowerLike(column, length, AND);
            }
        }
    }
    
    public static void shift(List<String> criterias) {
        int length = criterias.size();
        if ( length == 0 ) {
            return;
        }
        
        int shifting = ( length / 2 ) + ( length % 2 );
        shifting = ( shifting / 2 ) + ( shifting % 2 );
        String shifted;
        int reverseShifting;
        for (int i = 0; i < shifting; i++) {
            reverseShifting = length - 1 - i;
            shifted = criterias.get(i);
            criterias.set(i, criterias.get(reverseShifting));
            criterias.set(reverseShifting, shifted);
        }
        return;
    }
    
    public static boolean isResultsQuantiyEnoughForMulticharCriterias(
            Collection results, int mutlicharCriteriasSize) {
        if ( mutlicharCriteriasSize < 5) {
            return results.size() > 4;
        } else if ( mutlicharCriteriasSize >= 5 && mutlicharCriteriasSize < 9 ) {
            return results.size() > 3;
        } else if ( mutlicharCriteriasSize >= 9 && mutlicharCriteriasSize < 13 ) {
            return results.size() > 2;
        } else {
            return results.size() > 2;
        }
    }
    
    public static List<String> patternToMulticharCriterias(String pattern) {
        return patternToMulticharCriteriasWithLength(pattern, criteriaLengthForPattern(pattern));
    }
    
    private static int criteriaLengthForPattern(String pattern) {
//        if ( pattern.length() < 8 ) {
//            return 2;
//        }
//        return pattern.length() / 4;
        return 2;
    }
    
    private static List<String> patternToMulticharCriteriasWithLength(
            String pattern, int criteriaLength) {
        pattern = lower(pattern);
        List<String> criterias = new ArrayList<>();
        
        collectCriterias(criterias, pattern, criteriaLength);
        
        return criterias;
    }
    
    private static void collectCriterias(
            List<String> criterias, String pattern, int criteriaLength) {        
        
        int criteriasQty = pattern.length() / criteriaLength;
        
        int criteriaStart = 0;
        int criteriaEnd = 0;
        String criteria;
        for (int i = 0; i < criteriasQty - 1; i++) {
            criteriaStart = i * criteriaLength;
            criteriaEnd = criteriaStart + criteriaLength;
            
            criteria = pattern.substring(criteriaStart, criteriaEnd);
            criterias.add("%" + criteria + "%");
            
            criteria = pattern.substring(criteriaStart + 1, criteriaEnd + 1);
            criterias.add("%" + criteria + "%");
        }
        
        criteriaStart = criteriaEnd;
        if ( isEven(pattern.length()) ) {
            criteria = pattern.substring(criteriaStart);
            criterias.add("%" + criteria + "%");
            
            criterias.add(criterias.get(criterias.size() / 2));
        } else {
            criteriaEnd = criteriaStart + criteriaLength;
            
            criteria = pattern.substring(criteriaStart, criteriaEnd);
            criterias.add("%" + criteria + "%");
            
            criteria = pattern.substring(criteriaStart + 1);
            criterias.add("%" + criteria + "%");
        }
    }
    
    public static void main(String[] args) {
        System.out.println(patternCriteria("abc", "col"));
    }
    
    public static List<String> patternToCharCriterias(String pattern) {
        char[] chars = lower(pattern).toCharArray();
        Map<Character, String> criterias = new HashMap<>();
        String criteria;        
        char character;
        for (int i = 0; i < chars.length; i++) {
            character = chars[i];
            criteria = criterias.get(character);
            if ( isSqlWildcard(character) ) {
                // add escape character before any underscore in order 
                // to treat them as usual chars and not as wildcard.
                if ( isNull(criteria) ) {
                    criterias.put(character, "%\\" + character + "%");
                } else {
                    criterias.put(character, criteria + "\\" + character + "%");
                }
            } else {
                if ( isNull(criteria) ) {
                    criterias.put(character, "%" + character + "%");
                } else {
                    criterias.put(character, criteria + character + "%");
                } 
            }        
        }
        return new ArrayList(criterias.values());
    }
    
    public static boolean isSqlWildcard(char character) {
        return character == '_' || character == '%';
    }
}
