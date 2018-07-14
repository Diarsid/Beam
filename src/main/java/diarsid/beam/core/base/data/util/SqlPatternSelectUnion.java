/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.base.data.util;


import diarsid.beam.core.base.util.Possible;

import static diarsid.beam.core.base.util.Possible.possibleButEmpty;
import static diarsid.beam.core.base.util.StringUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.purge;
import static diarsid.beam.core.base.util.StringUtils.replaceAll;

/**
 *
 * @author Diarsid
 */
public class SqlPatternSelectUnion extends SqlPatternQuery {
    
    static {
        createPoolFor(SqlPatternSelectUnion.class, () -> new SqlPatternSelectUnion());
    }
    
    private final Possible<String> pattern;
    private final ModifiablePatternLikeness likeness;
    private final StringBuilder sql;
    
    private SqlPatternSelectUnion() {
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
    public String compose() {
        return this.sql.toString();
    }
    
    @Override
    void onLikenessDecreased(int oldMatches, int newMatches) {
        replaceAll(this.sql, " )  >= " + oldMatches, " )  >= " + newMatches);
    }
    
}
