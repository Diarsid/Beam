/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import diarsid.support.objects.Possible;

import static java.util.Objects.isNull;

import static diarsid.beam.core.base.util.SqlUtil.SQL_SINGLE_QUOTE_ESCAPE;
import static diarsid.beam.core.base.util.SqlUtil.isSqlWildcard;
import static diarsid.support.strings.StringUtils.lower;
import static diarsid.support.strings.StringUtils.nonEmpty;
import static diarsid.support.strings.StringUtils.purge;
import static diarsid.support.strings.StringUtils.replaceAll;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
public class SqlPatternSelect extends SqlPatternQuery {
    
    private final Possible<String> pattern;
    private final Map<Character, Integer> charsOccurences;
    private final StringBuilder sql;
    private final StringBuilder caseCondition;
    private final Possible<String> columns;
    private final Possible<String> table;
    private final Possible<String> patternColumn;
    private final Possible<String> anotherWhereClauses;
    private final Possible<Integer> selectLimit;
    private boolean groupBySelectColumns;
    private boolean distinct;
    
    public SqlPatternSelect() {
        super();
        this.columns = possibleButEmpty();
        this.table = possibleButEmpty();
        this.pattern = possibleButEmpty();
        this.charsOccurences = new HashMap<>();
        this.sql = new StringBuilder();
        this.caseCondition = new StringBuilder();
        this.patternColumn = possibleButEmpty();
        this.anotherWhereClauses = possibleButEmpty();
        this.selectLimit = possibleButEmpty();
        this.groupBySelectColumns = false;
        this.distinct = false;
    }

    @Override
    protected void clearForReuse() {
        this.pattern.nullify();
        this.charsOccurences.clear();
        purge(this.sql);
        purge(this.caseCondition);
        this.patternColumn.nullify();
        this.columns.nullify();
        this.table.nullify();
        this.anotherWhereClauses.nullify();
        this.selectLimit.nullify();
        this.groupBySelectColumns = false;
        this.distinct = false;
        super.likeness().clear();
    }
    
    StringBuilder sql() {
        return this.sql;
    }
    
    Possible<String> pattern() {
        return this.pattern;
    }
    
    public SqlPatternSelect select(String columns) {
        this.distinct = false;
        this.columns.resetTo(columns);
        return this;
    }
    
    public SqlPatternSelect selectDistinct(String columns) {
        this.distinct = true;
        this.columns.resetTo(columns);
        return this;
    }
    
    public SqlPatternSelect from(String table) {
        this.table.resetTo(table);
        return this;
    }
    
    public SqlPatternSelect patternColumnForWhereCondition(String patternColumn) {
        if ( nonEmpty(this.sql) && nonEmpty(this.caseCondition) ) {
            String oldColumn = this.patternColumn.orThrow();
            String newColumn = lower(patternColumn);
            replaceAll(this.sql, oldColumn, newColumn);
            this.patternColumn.resetTo(newColumn);   
        } else {
            this.patternColumn.resetTo(lower(patternColumn));   
        }
        return this;
    }
    
    public SqlPatternSelect groupBySelectColumns() {
        this.groupBySelectColumns = true;
        return this;
    }
    
    public SqlPatternSelect patternForWhereCondition(String pattern) {
        this.charsOccurences.clear();
        purge(this.sql);
        purge(this.caseCondition);
        
        this.pattern.resetTo(lower(pattern));
        super.likeness().setPatternLength(pattern.length());
        this.countCharOccurences();
        return this;
    }
    
    @Override
    void onLikenessDecreased(int oldMatches, int newMatches) {
        replaceAll(this.sql, " )  >= " + oldMatches, " )  >= " + newMatches);
    }
    
    public SqlPatternSelect limit(int limit) {
        this.selectLimit.resetTo(limit);
        return this;
    }
    
    public SqlPatternSelect anotherWhereClauses(String whereClauses) {
        this.anotherWhereClauses.resetTo(whereClauses);
        return this;
    }

    private void countCharOccurences() {
        char character;
        Integer occurences;
        
        String patternLocal = this.pattern.orThrow();
        int patternLength = patternLocal.length();
        for (int i = 0; i < patternLength; i++) {
            character = patternLocal.charAt(i);
            occurences = this.charsOccurences.get(character);
            if ( isNull(occurences) ) {
                this.charsOccurences.put(character, 1);
            } else {
                this.charsOccurences.put(character, occurences + 1);
            }      
        }
    }
    
    void composeSelectInternally() {
        this.sql
                .append(this.distinct ? "SELECT DISTINCT " : "SELECT ")
                .append(this.columns.orThrow())
                .append(" FROM ")
                .append(this.table.orThrow())
                .append(" WHERE ( ( ");        
        this.composeCaseCondition();   
        this.sql
                .append(this.caseCondition)
                .append(" ) ")
                .append(" >= ")
                .append(super.likeness().requiredMatches())
                .append(" ) ")
                .append(this.anotherWhereClauses.or(""));
        if ( this.groupBySelectColumns ) {
            this.sql
                    .append(" GROUP BY ")
                    .append(this.columns.orThrow());
        }
        if ( ! this.distinct ) {
            this.sql
                    .append(" ORDER BY ")
                    .append(this.caseCondition)
                    .append(" DESC, LENGTH(")
                    .append(this.patternColumn.orThrow())
                    .append(") ASC ");
        }        
        if ( this.selectLimit.isPresent() ) {
            this.sql.append(" LIMIT ").append(this.selectLimit.orThrow());
        }
    }
    
    @Override
    public String composeSql() {     
        if ( nonEmpty(this.sql) && nonEmpty(this.caseCondition) ) {
            return this.sql.toString();
        }
        
        this.composeSelectInternally();
        this.sql.append(';');
        
        String query = this.sql.toString();
        return query;
    }

    private void composeCaseCondition() {
        if ( this.caseCondition.length() > 0 ) {
            return;
        }
        Integer occurences;
        Character character;
        int entriesVisited = 0;
        int entriesQuantity = this.charsOccurences.size();
        for (Entry<Character, Integer> entry : this.charsOccurences.entrySet()) {
            entriesVisited++;
            
            occurences = entry.getValue();
            character = entry.getKey();
            
            if ( occurences == 1 ) {
                this.appendPositionToCondition(character);
            } else {
                this.appendLikeToCondition(character, occurences);
            }
            
            if ( entriesVisited != entriesQuantity ) {
                this.caseCondition.append(" + ");
            }
        }
    }

    private void appendPositionToCondition(Character character) {
        this.caseCondition.append(" CASE WHEN POSITION('");
        this.appendCharToCaseCondition(character);
        this.caseCondition
                .append("', LOWER(")
                .append(this.patternColumn.orThrow())
                .append(")) > 0 THEN 1 ELSE 0 END ");
    }
    
    private void appendLikeToCondition(Character character, Integer occurences) {
        boolean isCharSqlWildcard = isSqlWildcard(character);
        
        String column = this.patternColumn.orThrow();
        
        this.caseCondition
                .append(" CASE WHEN POSITION('");
        this.appendCharToCaseCondition(character);
        this.caseCondition
                .append("', LOWER(")
                .append(column)
                .append(")) > 0 THEN 1 ELSE 0 END ");
        
        for (int i = 1; i < occurences; i++) {
            this.caseCondition
                .append(" + CASE WHEN LOWER(")
                .append(column)
                .append(") LIKE '");                
            for (int j = 0; j < i + 1; j++) {
                if ( character == '\'' ) {
                    this.caseCondition
                            .append("%")
                            .append(SQL_SINGLE_QUOTE_ESCAPE); 
                } else if ( isCharSqlWildcard ) {
                    this.caseCondition
                            .append("%\\")
                            .append(character);
                } else {
                    this.caseCondition
                            .append("%")
                            .append(character);
                }
            }
            this.caseCondition.append("%' THEN 1 ELSE 0 END ");
        }
    }
    
    private void appendCharToCaseCondition(Character character) {
        if ( character == '\'' ) {
            this.caseCondition.append(SQL_SINGLE_QUOTE_ESCAPE); 
        } else if ( isSqlWildcard(character) ) {
            this.caseCondition.append("\\").append(character);
        } else {
            this.caseCondition.append(character);
        }
    }
    
}
