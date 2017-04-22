/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;


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

import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.StringUtils.lower;

/**
 *
 * @author Diarsid
 */
public class SqlUtil {
    
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
    
    public static String lowerWildcardAfter(String part) {
        return lower(part) + "%";
    }
    
    public static String lowerWildcard(String part) {
        return "%" + lower(part) + "%";
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
    
    public static String multipleLowerGroupedLikesOr(String column, int length) {
        String condition = "( LOWER(" + column + ") LIKE ? )";
        switch (length) {
            case 1 : {
                return condition;
            }
            case 2 : {
                return 
                        condition + " OR " + 
                        condition;
            }
            case 3 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + 
                        ") OR " + 
                        condition;
            }
            case 4 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 5 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 6 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 7 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 8 : {
                return 
                        "( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " ) OR ( " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + " AND " + 
                        condition + 
                        " )";
            }
            case 9 : {
                return 
                        "( " + 
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
                        " )";
            }
            case 10 : {
                return 
                        "( " + 
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
                        " )";
            }
            default : {                
                return 
                        "( " + 
                        multipleLowerLikeAnd(column, length - (length / 2)) + 
                        " ) OR ( " + 
                        multipleLowerLikeAnd(column, (length / 2)) + 
                        " )";
            }
        }    
    }
    
    public static String multipleLowerLIKE(
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
                return multipleLowerLIKE(column, length, AND);
            }
        }
    }
    
    public static void main(String[] args) {
        patternToCharCriterias("nebeansprojs").forEach(System.out::println);
    }
    
    public static Collection<String> patternToCharCriterias(String pattern) {
        char[] chars = lower(pattern).toCharArray();
        Map<Character, String> criterias = new HashMap<>();
        String criteria;        
        char character;
        for (int i = 0; i < chars.length; i++) {
            character = chars[i];
            criteria = criterias.get(character);
            if ( isNull(criteria) ) {
                criterias.put(character, "%" + character + "%");
            } else {
                criterias.put(character, criteria + character + "%");
            }            
        }
        return criterias.values();
    }
}
