/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.Program;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;

/**
 *
 * @author Diarsid
 */
class H2DaoNamedEntitiesV1 extends H2DaoNamedEntitiesV0 {
    
    H2DaoNamedEntitiesV1(
            DataBase dataBase, 
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog) {
        super(dataBase, ioEngine, programsCatalog);
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePattern(
            Initiator initiator, String pattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            transact.logHistoryAfterCommit();
            
            String lowerNamePattern = lowerWildcard(pattern);
            List<NamedEntity> entityMasks;
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToNamedEntityMask(),
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) LIKE ? " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) LIKE ? " +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) LIKE ? ) OR ( LOWER(shortcuts) LIKE ? )", 
                            lowerNamePattern, 
                            lowerNamePattern, 
                            lowerNamePattern,
                            lowerNamePattern)
                    .collect(toList());
            
            entityMasks.addAll(super.programsCatalog().findProgramsByWholePattern(pattern));
            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToNamedEntityMask(),
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLikeAnd("loc_name", criterias.size()) +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLikeAnd("bat_name", criterias.size()) +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE " + 
                                    multipleLowerLikeAnd("name", criterias.size()) + 
                                    " OR " + 
                                    multipleLowerLikeAnd("shortcuts", criterias.size()),
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList()); 
                        
            List<Program> programsByPattern = 
                    super.programsCatalog().findProgramsByPatternSimilarity(pattern);
            entityMasks.addAll(programsByPattern);
            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            String andOrConditionLocations = 
                    multipleLowerGroupedLikesAndOr("loc_name", criterias.size());
            String andOrConditionBatces = 
                    multipleLowerGroupedLikesAndOr("bat_name", criterias.size());
            String andOrConditionPagesName = 
                    multipleLowerGroupedLikesAndOr("name", criterias.size());
            String andOrConditionPagesShortcuts = 
                    multipleLowerGroupedLikesAndOr("shortcuts", criterias.size());
            List<NamedEntity> shiftedMaskedEntities;
            
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToNamedEntityMask(),
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE " + andOrConditionLocations +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE " + andOrConditionBatces +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE " + 
                                    andOrConditionPagesName + 
                                    " OR " + 
                                    andOrConditionPagesShortcuts,
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            
            shiftedMaskedEntities = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToNamedEntityMask(),
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE " + andOrConditionLocations +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE " + andOrConditionBatces +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE " + 
                                    andOrConditionPagesName + 
                                    " OR " + 
                                    andOrConditionPagesShortcuts,
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList()); 
                        
            shiftedMaskedEntities.retainAll(entityMasks);
            entityMasks.retainAll(shiftedMaskedEntities);
            entityMasks.addAll(programsByPattern);
            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            } else {
                return entityMasks;
            }            
            
        } catch (TransactionHandledSQLException | TransactionHandledException ex) {
            logFor(this).error(format("find all named entities by %s", pattern), ex);
            super.ioEngine().report(
                    initiator, "named entities searching failed");
            return emptyList();
        }
    }
}
