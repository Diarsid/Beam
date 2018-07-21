/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;


import org.junit.Before;
import org.junit.Test;

import static java.lang.String.format;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.StringUtils.countOccurences;

/**
 *
 * @author Diarsid
 */
public class SqlPatternSelectTest {
    
    private String pattern;
    private String columns;
    private String table;
    private String patternColumn;
    private int limit;
    
    public SqlPatternSelectTest() {
    }
    
    @Before
    public void setUp() {
        patternColumn = "col";
        limit = 10;
        columns = "*";
        table = "table";
    }

    @Test
    public void testCharCriteriasBehavior() {
        SqlPatternSelect select;        
        
        pattern = "abc";
        
        try (SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) {
            select = patternSelect;
            
            String sql = patternSelect
                    .select(columns)
                    .from(table)
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition(patternColumn)
                    .limit(limit)
                    .composeSql();
            
            assertThat(sql.contains(format("SELECT %s FROM %s ", columns, table)), equalTo(true));
            assertThat(sql.contains(format("CASE WHEN POSITION('b', LOWER(%s)) > 0 THEN 1 ELSE 0 END", patternColumn)), equalTo(true));
            assertThat(sql.contains(format("CASE WHEN POSITION('b', LOWER(%s)) > 0 THEN 1 ELSE 0 END", patternColumn)), equalTo(true));
            assertThat(sql.contains(format("CASE WHEN POSITION('c', LOWER(%s)) > 0 THEN 1 ELSE 0 END", patternColumn)), equalTo(true));
            assertThat(sql.contains(format(" LIMIT %s;", limit)), equalTo(true));
            assertThat(sql.contains(" >= 2 "), equalTo(true));
        } finally {
            // do nothing
        }
        
        SqlPatternSelect reusedSelect = takeFromPool(SqlPatternSelect.class);
        assertThat(select.isSame(reusedSelect), equalTo(true));
    }
    
    @Test
    public void testCharCriteriasBehavior_withSqlWildcard() {   
        try (SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) {            
            
            pattern = "a_c";
            
            String sql = patternSelect
                    .select(columns)
                    .from(table)
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition(patternColumn)
                    .composeSql();
            
            assertThat(sql.contains("CASE WHEN POSITION('a', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN POSITION('\\_', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN POSITION('c', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains(" >= 2 "), equalTo(true));
        } finally {
            // do nothing
        }
    }
    
    @Test
    public void testCharCriteriasBehavior_withCharDuplications() {   
        try (SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) {            
            
            pattern = "abbb";
            
            String sql = patternSelect
                    .select(columns)
                    .from(table)
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition(patternColumn)
                    .composeSql();
            
            assertThat(sql.contains("CASE WHEN POSITION('a', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN POSITION('b', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN LOWER(col) LIKE '%b%b%' THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN LOWER(col) LIKE '%b%b%b%' THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains(" >= 3 "), equalTo(true));
        } finally {
            // do nothing
        }
    }
    
    @Test
    public void testCharCriteriasBehavior_withSqlWildcardCharDuplications() {   
        try (SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) {            
            
            pattern = "a_b__";
            
            String sql = patternSelect
                    .select(columns)
                    .from(table)
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition(patternColumn)
                    .composeSql();
            
            assertThat(sql.contains("CASE WHEN POSITION('a', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN POSITION('b', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN POSITION('\\_', LOWER(col)) > 0 THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN LOWER(col) LIKE '%\\_%\\_%' THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains("CASE WHEN LOWER(col) LIKE '%\\_%\\_%\\_%' THEN 1 ELSE 0 END"), equalTo(true));
            assertThat(sql.contains(" >= 4 "), equalTo(true));
        } finally {
            // do nothing
        }
    }
    
    @Test
    public void testUnion() {
        try (
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class);
                SqlPatternSelectUnion patternUnion = takeFromPool(SqlPatternSelectUnion.class)) 
        {
            pattern = "abc";
            
            patternSelect
                    .select("loc_name AS entity_name, 'location' AS entity_type")
                    .from("locations")
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition("loc_name");
            
            patternUnion.unionAll(patternSelect);
            
            patternSelect
                    .select("bat_name, 'batch'")
                    .from("batches")                    
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition("bat_name");
            
            patternUnion.unionAll(patternSelect);
            
            patternSelect
                    .select("name, 'webpage'")
                    .from("web_pages")
                    .patternForWhereCondition(pattern)                    
                    .patternColumnForWhereCondition("name");
            
            patternUnion.unionAll(patternSelect);
            
            patternSelect
                    .select("name, 'webpage'")
                    .from("web_pages")
                    .patternForWhereCondition(pattern)                    
                    .patternColumnForWhereCondition("shortcuts");
            
            patternUnion.unionDistinct(patternSelect);
            
            String sql = patternUnion.composeSql();
            
            assertThat(countOccurences(sql, "UNION ALL"), equalTo(2));
            assertThat(countOccurences(sql, "UNION "), equalTo(3));
            assertThat(countOccurences(sql, "CASE WHEN POSITION('a', LOWER("), equalTo(4 * 2 /* x2 for CASE in SELECT and in ORDER BY */ ));
            assertThat(countOccurences(sql, " >= 2 "), equalTo(4));
        } finally {
        }
    }
    
    @Test
    public void testCharCriteriasBehavior_withDuplicatedCharacters() {   
        try (SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class)) {            
            
            pattern = "abcde";
            
            String sql = patternSelect
                    .select(columns)
                    .from(table)
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition(patternColumn)
                    .composeSql();
            
            assertThat(sql.contains(" >= 4 "), equalTo(true));
            
            sql = patternSelect.decreaseRequiredLikeness().composeSql();
            
            assertThat(sql.contains(" >= 3 "), equalTo(true));
            
            sql = patternSelect.decreaseRequiredLikeness().composeSql();
            
            assertThat(sql.contains(" >= 3 "), equalTo(true));
            
            sql = patternSelect.decreaseRequiredLikeness().composeSql();
            
            assertThat(sql.contains(" >= 3 "), equalTo(true));
            
        } finally {
            // do nothing
        }
    }
    
}
