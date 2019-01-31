/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.beam.core.base.data.util.SqlPatternSelectUnion;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;
import diarsid.support.objects.Pool;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;

/**
 *
 * @author Diarsid
 */
public class H2DaoWebPagesV2 extends H2DaoWebPagesV0 {
        
    private final Pool<SqlPatternSelect> sqlPatternSelectPool;
    private final Pool<SqlPatternSelectUnion> sqlPatternSelectUnionPool;
    
    H2DaoWebPagesV2(
            DataBase dataBase, 
            Pool<SqlPatternSelect> sqlPatternSelectPool, 
            Pool<SqlPatternSelectUnion> sqlPatternSelectUnionPool) {
        super(dataBase);
        this.sqlPatternSelectPool = sqlPatternSelectPool;
        this.sqlPatternSelectUnionPool = sqlPatternSelectUnionPool;
    }

    @Override
    public List<WebPage> findByPattern(String pattern) throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = this.sqlPatternSelectPool.give();
                SqlPatternSelectUnion patternUnion = this.sqlPatternSelectUnionPool.give();) 
        {
            String lowerWildcardPattern = lowerWildcard(pattern);
            
            List<WebPage> pages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) LIKE ? ) OR ( LOWER(shortcuts) LIKE ? ) ",
                            lowerWildcardPattern, lowerWildcardPattern)                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            if ( nonEmpty(pages) ) {
                return pages;
            }
            
            patternSelect
                    .select("name, shortcuts, url, ordering, dir_id")
                    .from("web_pages")
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition("name");
            
            patternUnion.unionDistinct(patternSelect);
            
            patternSelect
                    .select("name, shortcuts, url, ordering, dir_id")
                    .from("web_pages")
                    .patternForWhereCondition(pattern)
                    .patternColumnForWhereCondition("shortcuts");
            
            patternUnion.unionDistinct(patternSelect);
                        
            pages = transact
                    .doQueryAndStream( 
                            ROW_TO_WEBPAGE,
                            patternUnion.composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            if ( nonEmpty(pages) ) {
                return pages;
            }
            
            pages = transact
                    .doQueryAndStream(  
                            ROW_TO_WEBPAGE,
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            if ( nonEmpty(pages) && patternUnion.isNextRequiredLikenessDecreaseMeaningfull() ) {
                return pages;
            }
            
            pages = transact
                    .doQueryAndStream(
                            ROW_TO_WEBPAGE,
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            return pages;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            throw super.logAndWrap(e);
        }        
    }
}
