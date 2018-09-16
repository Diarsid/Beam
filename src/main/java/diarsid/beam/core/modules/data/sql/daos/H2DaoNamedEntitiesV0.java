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
import diarsid.beam.core.domain.entities.NamedEntity;
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
import static diarsid.support.log.Logging.logFor;
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


abstract class H2DaoNamedEntitiesV0 
        extends BeamCommonDao
        implements DaoNamedEntities {
    
    private final ProgramsCatalog programsCatalog;
    private final RowConversion<NamedEntity> rowToNamedEntityMask;
    private final NamedEntityComparator namedEntityComparator;
    
    H2DaoNamedEntitiesV0(
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
    
    protected final ProgramsCatalog programsCatalog() {
        return this.programsCatalog;
    }
    
    protected final RowConversion<NamedEntity> rowToNamedEntityMask() {
        return this.rowToNamedEntityMask;
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
                                ROW_TO_LOCATION, 
                                "SELECT loc_name, loc_path " +
                                "FROM locations " +
                                "WHERE ( LOWER(loc_name) IS ? ) ",
                                lower(name))
                        .orElse(null);
                break;
            }        
            case WEBPAGE : {
                entity = transact
                        .doQueryAndConvertFirstRowVarargParams(
                                ROW_TO_WEBPAGE,
                                "SELECT name, shortcuts, url, ordering, dir_id " +
                                "FROM web_pages " +
                                "WHERE ( LOWER(name) IS ? )", 
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
                        .doQueryAndStreamVarargParams(
                                ROW_TO_EXECUTOR_COMMAND,
                                "SELECT bat_command_type, " +
                                "       bat_command_original " +
                                "FROM batch_commands " +
                                "WHERE LOWER(bat_name) IS ? " +
                                "ORDER BY bat_command_order" ,
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
    
    protected final List<NamedEntity> collectRealEntitiesUsing(
            List<NamedEntity> maskedEntities, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
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
                            (row) -> {
                                return new NamedEntityMask(row);
                            },
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
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(
                    initiator, "named entities obtaining by exact name failed.");
            return Optional.empty();
        }
    }

    @Override
    public List<NamedEntity> getAll(Initiator initiator) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<NamedEntity> entityMasks;
            
            entityMasks = transact
                    .doQueryAndStream(
                            this.rowToNamedEntityMask,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "       UNION ALL " +
                            "SELECT name, 'webpage' " +
                            "FROM web_pages ")
                    .collect(toList());
            
            entityMasks.addAll(this.programsCatalog.getAll());
            
            return entityMasks;
            
        } catch (TransactionHandledSQLException|TransactionHandledException e) {
            logFor(this).error(e.getMessage(), e);
            super.ioEngine().report(
                    initiator, "cannot get all named entities.");
            return emptyList();
        }
    }
}
