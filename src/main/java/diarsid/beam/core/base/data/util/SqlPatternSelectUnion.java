/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;


import diarsid.support.objects.Possible;

import static diarsid.support.strings.StringUtils.nonEmpty;
import static diarsid.support.strings.StringUtils.purge;
import static diarsid.support.strings.StringUtils.replaceAll;
import static diarsid.support.objects.Possibles.possibleButEmpty;

/**
 *
 * @author Diarsid
 */
public class SqlPatternSelectUnion extends SqlPatternQuery {
    
    private final Possible<String> pattern;
    private final ModifiablePatternLikeness likeness;
    private final StringBuilder sql;
    
    public SqlPatternSelectUnion() {
        this.pattern = possibleButEmpty();
        this.likeness = new ModifiablePatternLikeness();
        this.sql = new StringBuilder();
    }

    @Override
    protected void clearForReuse() {
        this.pattern.nullify();
        this.likeness.clear();
        purge(this.sql);
    }

    private void makeWith(SqlPatternSelect select, String union) {
        select.composeSelectInternally();
        this.verifyIsAllowedToUnion(select);
        if ( nonEmpty(this.sql) ) {
            this.sql.append(union);
        } 
        this.sql.append("(").append(select.sql()).append(")");
        select.clearForReuse();
    }
    
    private void verifyIsAllowedToUnion(SqlPatternSelect select) {
        if ( this.pattern.isNotPresent() ) {
            this.pattern.resetTo(select.pattern());
            this.likeness.setPatternLength(select.pattern().orThrow().length());
        } else {
            if ( this.pattern.notEquals(select.pattern()) || 
                 this.likeness.notEquals(select.likeness()) ) {
                throw new SqlPatternException(
                        "It is not allowed to union selects with different state or data!");
            }
        }
    }
    
    public void unionAll(SqlPatternSelect select) {
        this.makeWith(select, " UNION ALL ");
    }
    
    public void unionDistinct(SqlPatternSelect select) {
        this.makeWith(select, " UNION ");
    }
    
    @Override
    public String composeSql() {
        return this.sql.toString();
    }
    
    @Override
    void onLikenessDecreased(int oldMatches, int newMatches) {
        replaceAll(this.sql, " )  >= " + oldMatches, " )  >= " + newMatches);
    }
    
}
