/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.beam.core.base.data.util.SqlPatternSelectUnion;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.objects.Pools.takeFromPool;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;

/**
 *
 * @author Diarsid
 */
public class H2DaoWebPagesV2 extends H2DaoWebPagesV0 {
    
    H2DaoWebPagesV2(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }

    @Override
    public List<WebPage> findByPattern(Initiator initiator, String pattern) {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class);
                SqlPatternSelectUnion patternUnion = takeFromPool(SqlPatternSelectUnion.class)) 
        {
            String lowerWildcardPattern = lowerWildcard(pattern);
            
            List<WebPage> tasks = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) LIKE ? ) OR ( LOWER(shortcuts) LIKE ? ) ",
                            lowerWildcardPattern, lowerWildcardPattern)                    
                    .peek(page -> super.setLoadableDirectoryFor(initiator, page))
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
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
                        
            tasks = transact
                    .doQueryAndStream( 
                            ROW_TO_WEBPAGE,
                            patternUnion.composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(initiator, page))
                    .collect(toList());
            
            if ( nonEmpty(tasks) ) {
                return tasks;
            }
            
            tasks = transact
                    .doQueryAndStream(  
                            ROW_TO_WEBPAGE,
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(initiator, page))
                    .collect(toList());
            
            if ( nonEmpty(tasks) && patternUnion.isNextRequiredLikenessDecreaseMeaningfull() ) {
                return tasks;
            }
            
            tasks = transact
                    .doQueryAndStream(
                            ROW_TO_WEBPAGE,
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())                    
                    .peek(page -> super.setLoadableDirectoryFor(initiator, page))
                    .collect(toList());
            
            return tasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(ex.getMessage(), ex);
            
            return emptyList();
        }        
    }
}
