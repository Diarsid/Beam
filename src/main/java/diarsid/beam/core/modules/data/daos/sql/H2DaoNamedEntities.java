/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import diarsid.beam.core.application.environment.ProgramsCatalog;
import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.Location;
import diarsid.beam.core.domain.entities.NamedEntity;
import diarsid.beam.core.domain.entities.WebPage;
import diarsid.beam.core.modules.data.DaoNamedEntities;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLIKE;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_COMMAND;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_LOCATION;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_NAMED_ENTITY;
import static diarsid.beam.core.modules.data.daos.sql.RowToEntityConversions.ROW_TO_PAGE;


class H2DaoNamedEntities 
        extends BeamCommonDao
        implements DaoNamedEntities {
    
    private final ProgramsCatalog programsCatalog;
    
    H2DaoNamedEntities(
            DataBase dataBase, 
            InnerIoEngine ioEngine, 
            ProgramsCatalog programsCatalog) {
        super(dataBase, ioEngine);
        this.programsCatalog = programsCatalog;
    }

    @Override
    public Optional<NamedEntity> getByExactName(
            Initiator initiator, String exactName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private Optional<? extends NamedEntity> findRealEntityInTransaction(
            NamedEntity mockedEntity, JdbcTransaction transact)
            throws TransactionHandledSQLException, TransactionHandledException {
        String name = mockedEntity.name();
        switch ( mockedEntity.type() ) {
            case LOCATION : {
                return transact.doQueryAndConvertFirstRowVarargParams(Location.class, 
                        "SELECT loc_name, loc_path " +
                        "FROM locations " +
                        "WHERE ( LOWER(loc_name) IS ? ) ",
                        (row) -> {
                            return Optional.of(ROW_TO_LOCATION.convert(row));
                        }, 
                        lower(name));
            }        
            case WEBPAGE : {
                return transact.doQueryAndConvertFirstRowVarargParams(
                        WebPage.class,
                        "SELECT name, shortcuts, url, ordering, dir_id " +
                        "FROM web_pages " +
                        "WHERE ( LOWER(name) IS ? ) ",
                        (row) -> {
                            return Optional.of(ROW_TO_PAGE.convert(row));
                        },
                        lower(name));
            }        
            case PROGRAM : {
                // return this.programsCatalog.
                throw new UnsupportedOperationException();
            }        
            case BATCH : {
                
                boolean batchExists = transact
                        .doesQueryHaveResultsVarargParams(
                                "SELECT bat_name " +
                                "FROM batches " + 
                                "WHERE LOWER(bat_name) IS ? ",
                                lower(name));

                List<ExecutorCommand> commands = transact
                        .ifTrue( batchExists )
                        .doQueryAndStreamVarargParams(
                                ExecutorCommand.class,
                                "SELECT bat_command_type, " +
                                "       bat_command_original " +
                                "FROM batch_commands " +
                                "WHERE LOWER(bat_name) IS ? " +
                                "ORDER BY bat_command_order" ,
                                ROW_TO_COMMAND,
                                lower(name))
                        .collect(toList());

                if ( nonEmpty(commands) ) {
                    return Optional.of(new Batch(name, commands));
                } else {
                    return Optional.empty();
                }
            }        
            default : {
                return Optional.empty();
            }            
        }
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePattern(
            Initiator initiator, String namePattern) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            String lowerName = lowerWildcard(namePattern);
            
            List<NamedEntity> mockedEntities = transact
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
                            "SELECT page_name, 'webpage' " +
                            "FROM webpages " +
                            "WHERE LOWER(page_name) LIKE ? ",
                            ROW_TO_NAMED_ENTITY,
                            lowerName, 
                            lowerName, 
                            lowerName)
                    .collect(toList());
            
            List<NamedEntity> realEntities = new ArrayList<>();
            for (NamedEntity mockedEntity : mockedEntities) {
                this.findRealEntityInTransaction(mockedEntity, transact)
                        .ifPresent(realEntity -> realEntities.add(realEntity));
            }
            
            return realEntities;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by name pattern failed.");
            return emptyList();
        }
    }

    @Override
    public List<NamedEntity> getEntitiesByNamePatternParts(
            Initiator initiator, List<String> namePatternParts) {
         try (JdbcTransaction transact = super.getTransaction()) {
            
            List<String> lowerNames = lowerWildcardList(namePatternParts);
            
            List<NamedEntity> mockedEntities = transact
                    .doQueryAndStreamVarargParams(NamedEntity.class,
                            "SELECT loc_name AS entity_name, 'location' AS entity_type " +
                            "FROM locations " +
                            "WHERE " + multipleLowerLIKE("loc_name", namePatternParts.size(), AND) + 
                            "       UNION ALL " +
                            "SELECT bat_name, 'batch' " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLIKE("bat_name", namePatternParts.size(), AND) + 
                            "       UNION ALL " +
                            "SELECT page_name, 'webpage' " +
                            "FROM webpages " +
                            "WHERE " + multipleLowerLIKE("page_name", namePatternParts.size(), AND),
                            ROW_TO_NAMED_ENTITY,
                            lowerNames, 
                            lowerNames, 
                            lowerNames)
                    .collect(toList());
            
            List<NamedEntity> realEntities = new ArrayList<>();
            for (NamedEntity mockedEntity : mockedEntities) {
                this.findRealEntityInTransaction(mockedEntity, transact)
                        .ifPresent(realEntity -> realEntities.add(realEntity));
            }
            
            return realEntities;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoNamedEntities.class, ex);
            super.ioEngine().report(
                    initiator, "named entities obtaining by name pattern parts failed.");
            return emptyList();
        }
    }
}
