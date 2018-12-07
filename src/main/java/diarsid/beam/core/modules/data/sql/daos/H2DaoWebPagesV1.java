/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;

/**
 *
 * @author Diarsid
 */
class H2DaoWebPagesV1 extends H2DaoWebPagesV0 {
    
    H2DaoWebPagesV1(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public List<WebPage> findByPattern(String pattern) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            transact.logHistoryAfterCommit();
            
            String lowerWildcardPattern = lowerWildcard(pattern);
            List<WebPage> pages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) LIKE ? ) OR ( LOWER(shortcuts) LIKE ? ) ", 
                            lowerWildcardPattern, lowerWildcardPattern)
                    .sorted()
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            if ( nonEmpty(pages) ) {
                return pages;
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            pages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE " +
                                    multipleLowerLikeAnd("name", criterias.size()) + 
                                    " OR " + 
                                    multipleLowerLikeAnd("shortcuts", criterias.size()), 
                            criterias, criterias)                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            if ( nonEmpty(pages) ) {
                return pages;
            }
            
            String multipleGroupedLikeOrNameCondition = 
                    multipleLowerGroupedLikesAndOr("name", criterias.size());
            String multipleGroupedLikeOrShortcutsCondition = 
                    multipleLowerGroupedLikesAndOr("shortcuts", criterias.size());
            
            pages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE " + 
                                    multipleGroupedLikeOrNameCondition + 
                                    " OR " + 
                                    multipleGroupedLikeOrShortcutsCondition, 
                            criterias, criterias)                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            shift(criterias);
            
            List<WebPage> shiftedPages = transact
                    .doQueryAndStreamVarargParams(
                            ROW_TO_WEBPAGE, 
                            "SELECT name, shortcuts, url, ordering, dir_id " +
                            "FROM web_pages " +
                            "WHERE " + 
                                    multipleGroupedLikeOrNameCondition + 
                                    " OR " + 
                                    multipleGroupedLikeOrShortcutsCondition, 
                            criterias, criterias)                    
                    .peek(page -> super.setLoadableDirectoryFor(page))
                    .collect(toList());
            
            shiftedPages.retainAll(pages);
            pages.retainAll(shiftedPages);
            
            return pages;            
            
        } catch (TransactionHandledException|TransactionHandledSQLException e) {
            throw super.logAndWrap(e);
        }
    }
}
