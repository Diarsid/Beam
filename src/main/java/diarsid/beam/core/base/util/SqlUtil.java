/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.base.util;


import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.generate;

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
    
    public static String multipleLowerLIKE(
            String column, int partsSize, SqlOperator sqlOperator) {
        return generate(() -> "( LOWER(column) LIKE ? )")
                .limit(partsSize)
                .map(condition -> condition.replace("column", column))
                .collect(joining(sqlOperator.getSql(), " ", " "));
    }
}
