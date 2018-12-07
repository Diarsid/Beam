/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diarsid.beam.core.modules.data.sql.daos;

import java.util.List;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.base.data.util.SqlPatternSelect;
import diarsid.beam.core.base.data.util.SqlPatternSelectUnion;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.Program;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.support.objects.Pools.takeFromPool;

/**
 *
 * @author Diarsid
 */
public class H2DaoNamedEntitiesV2 extends H2DaoNamedEntitiesV0 {
    
    H2DaoNamedEntitiesV2(
            DataBase dataBase, 
            ProgramsCatalog programsCatalog) {
        super(dataBase, programsCatalog);
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePattern(String pattern) 
            throws DataExtractionException {
        try (
                JdbcTransaction transact = super.openTransaction();
                SqlPatternSelect patternSelect = takeFromPool(SqlPatternSelect.class);
                SqlPatternSelectUnion patternUnion = takeFromPool(SqlPatternSelectUnion.class)) 
        {            
            List<NamedEntity> entityMasks;
            
            String lowerPattern = lowerWildcard(pattern);
            
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            super.rowToNamedEntityMask(),
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE LOWER(loc_name) IS ? " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) IS ? " +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) IS ? ) OR ( LOWER(shortcuts) IS ? )", 
                            lowerPattern, 
                            lowerPattern, 
                            lowerPattern,
                            lowerPattern)
                    .collect(toList());
            
            entityMasks.addAll(super.programsCatalog().findProgramsByWholePattern(pattern));
            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            String lowerPatternLike = lowerWildcard(pattern);
            
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
                            lowerPatternLike, 
                            lowerPatternLike, 
                            lowerPatternLike,
                            lowerPatternLike)
                    .collect(toList());
            
            entityMasks.addAll(super.programsCatalog().findProgramsByWholePattern(pattern));
            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
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
            
            entityMasks = transact
                    .doQueryAndStream( 
                            super.rowToNamedEntityMask(),
                            patternUnion.composeSql())
                    .collect(toList());
            
            List<Program> programs = super
                    .programsCatalog()
                    .findProgramsByPatternSimilarity(pattern);
            
            entityMasks.addAll(programs);            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            entityMasks = transact
                    .doQueryAndStream( 
                            super.rowToNamedEntityMask(),
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            entityMasks.addAll(programs);            
            if ( nonEmpty(entityMasks) ) {
                return super.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            entityMasks = transact
                    .doQueryAndStream( 
                            super.rowToNamedEntityMask(),
                            patternUnion
                                    .decreaseRequiredLikeness()
                                    .composeSql())
                    .collect(toList());
            
            entityMasks.addAll(programs); 
            return super.collectRealEntitiesUsing(entityMasks, transact);
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
}
