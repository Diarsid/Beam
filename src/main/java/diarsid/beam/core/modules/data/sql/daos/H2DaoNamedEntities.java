/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.Program;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.CollectionsUtils.sortAndGetFirstFrom;
import static diarsid.beam.core.base.util.Logs.debug;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.SqlUtil.spacingWildcards;
import static diarsid.beam.core.base.util.SqlUtil.wildcardSpaceAfter;
import static diarsid.beam.core.base.util.SqlUtil.wildcardSpaceBefore;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.domain.entities.NamedEntityType.BATCH;
import static diarsid.beam.core.domain.entities.NamedEntityType.LOCATION;
import static diarsid.beam.core.domain.entities.NamedEntityType.PROGRAM;
import static diarsid.beam.core.domain.entities.NamedEntityType.WEBPAGE;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_EXECUTOR_COMMAND;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_LOCATION;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_WEBPAGE;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesAndOr;


class H2DaoNamedEntities 
        extends BeamCommonDao
        implements DaoNamedEntities {
    
    private final ProgramsCatalog programsCatalog;
    private final RowConversion<NamedEntity> rowToNamedEntityMask;
    private final NamedEntityComparator namedEntityComparator;
    
    H2DaoNamedEntities(
            DataBase dataBase, 
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog) {
        super(dataBase, ioEngine);
        this.programsCatalog = programsCatalog;
        this.rowToNamedEntityMask = (row) -> {
            return new NamedEntityMask(row);
        };
        this.namedEntityComparator = new NamedEntityComparator();
    }
    
    private Optional<NamedEntity> findRealEntityUsing(
            NamedEntity mockedEntity, JdbcTransaction transact)
            throws TransactionHandledSQLException, TransactionHandledException {
        String name = mockedEntity.name();
        NamedEntity entity;
        
        switch ( mockedEntity.type() ) {
            case LOCATION : {
                entity = transact
                        .doQueryAndConvertFirstRowVarargParams(
                                Location.class, 
                                "SELECT loc_name, loc_path " +
                                "FROM locations " +
                                "WHERE ( LOWER(loc_name) IS ? ) ",
                                ROW_TO_LOCATION, 
                                lower(name))
                        .orElse(null);
                break;
            }        
            case WEBPAGE : {
                entity = transact
                        .doQueryAndConvertFirstRowVarargParams(
                                WebPage.class,
                                "SELECT name, shortcuts, url, ordering, dir_id " +
                                "FROM web_pages " +
                                "WHERE ( LOWER(name) IS ? )", 
                                ROW_TO_WEBPAGE,
                                lower(name))
                        .orElse(null);
                break;
            }        
            case PROGRAM : {
                entity = this.programsCatalog
                        .findProgramByDirectName(name)
                        .orElse(null);
                break;
            }        
            case BATCH : {
                String lowerName = lower(name);
                boolean batchExists = transact
                        .doesQueryHaveResultsVarargParams(
                                "SELECT bat_name " +
                                "FROM batches " + 
                                "WHERE LOWER(bat_name) IS ? ",
                                lowerName);

                List<ExecutorCommand> commands = transact
                        .ifTrue( batchExists )
                        .doQueryAndStreamVarargParams(ExecutorCommand.class,
                                "SELECT bat_command_type, " +
                                "       bat_command_original " +
                                "FROM batch_commands " +
                                "WHERE LOWER(bat_name) IS ? " +
                                "ORDER BY bat_command_order" ,
                                ROW_TO_EXECUTOR_COMMAND,
                                lowerName)
                        .collect(toList());

                if ( batchExists && nonEmpty(commands) ) {
                    entity = new Batch(name, commands);
                } else {
                    entity = null;
                }
                break;
            }        
            default : {
                entity = null;
                break;
            }            
        }
        
        return Optional.of(entity);
    }
    
    private List<NamedEntity> collectRealEntitiesUsing(
            List<NamedEntity> maskedEntities, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
        debug("[ALL ENTITIES DAO] [collect real] " + maskedEntities);
        List<NamedEntity> realEntities = new ArrayList<>();
        for (NamedEntity maskedEntity : maskedEntities) {
            this.findRealEntityUsing(maskedEntity, transact)
                    .ifPresent(realEntity -> realEntities.add(realEntity));
        }
        return realEntities;
    }

    @Override
    public Optional<NamedEntity> getByExactName(
            Initiator initiator, String exactName) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            transact.logHistoryAfterCommit();
            
            String lowerExactName = lower(exactName);
            List<NamedEntity> maskedEntities = transact
                    .doQueryAndStreamVarargParams(
                            NamedEntity.class,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE ( LOWER(loc_name) IS ? ) " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE ( LOWER(bat_name) IS ? ) " +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages " +
                            "WHERE ( LOWER(name) IS ? ) " +
                            "       OR ( LOWER(shortcuts) IS ? ) " +
                            "       OR ( LOWER(shortcuts) LIKE ? ) " +
                            "       OR ( LOWER(shortcuts) LIKE ? ) " +
                            "       OR ( LOWER(shortcuts) LIKE ? ) ", 
                            (row) -> {
                                return new NamedEntityMask(row);
                            },
                            lowerExactName, 
                            lowerExactName, 
                            lowerExactName,
                            lowerExactName,
                            wildcardSpaceAfter(exactName),
                            wildcardSpaceBefore(exactName),
                            spacingWildcards(exactName))
                    .collect(toList());
            
            maskedEntities.addAll(this.programsCatalog.findProgramsByStrictName(exactName));
            
            if ( nonEmpty(maskedEntities) ) {
                NamedEntity entityMask = 
                        sortAndGetFirstFrom(maskedEntities, this.namedEntityComparator);
                if ( entityMask.type().isDefined() ) {
                    return this.findRealEntityUsing(entityMask, transact);                 
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by exact name failed.");
            return Optional.empty();
        }
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePattern(
            Initiator initiator, String namePattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            transact.logHistoryAfterCommit();
            
            String lowerNamePattern = lowerWildcard(namePattern);
            List<NamedEntity> entityMasks;
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            NamedEntity.class,
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
                            this.rowToNamedEntityMask,
                            lowerNamePattern, 
                            lowerNamePattern, 
                            lowerNamePattern,
                            lowerNamePattern)
                    .collect(toList());
            
            entityMasks.addAll(this.programsCatalog.findProgramsByWholePattern(namePattern));
            
            if ( nonEmpty(entityMasks) ) {
                return this.collectRealEntitiesUsing(entityMasks, transact);
            }
            
            List<String> criterias = patternToCharCriterias(namePattern);
            entityMasks = transact
                    .doQueryAndStreamVarargParams(
                            NamedEntity.class,
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
                            this.rowToNamedEntityMask,
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList()); 
                        
            List<Program> programsByPattern = 
                    this.programsCatalog.findProgramsByPatternSimilarity(namePattern);
            entityMasks.addAll(programsByPattern);
            
            if ( nonEmpty(entityMasks) ) {
                return this.collectRealEntitiesUsing(entityMasks, transact);
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
                            NamedEntity.class,
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
                            this.rowToNamedEntityMask,
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            
            shiftedMaskedEntities = transact
                    .doQueryAndStreamVarargParams(
                            NamedEntity.class,
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
                            this.rowToNamedEntityMask,
                            criterias, 
                            criterias, 
                            criterias, 
                            criterias)
                    .collect(toList()); 
                        
            shiftedMaskedEntities.retainAll(entityMasks);
            entityMasks.retainAll(shiftedMaskedEntities);
            entityMasks.addAll(programsByPattern);
            
            if ( nonEmpty(entityMasks) ) {
                return this.collectRealEntitiesUsing(entityMasks, transact);
            } else {
                return entityMasks;
            }            
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by name pattern failed.");
            return emptyList();
        }
    }

    @Override
    public List<NamedEntity> getAll(Initiator initiator) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<NamedEntity> entityMasks;
            
            entityMasks = transact
                    .doQueryAndStream(
                            NamedEntity.class,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages ", 
                            this.rowToNamedEntityMask)
                    .collect(toList());
            
            entityMasks.addAll(this.programsCatalog.getAll());
            
            return entityMasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "cannot get all named entities.");
            return emptyList();
        }
    }
}
