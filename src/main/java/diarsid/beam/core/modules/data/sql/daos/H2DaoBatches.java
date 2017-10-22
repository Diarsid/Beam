/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.sql.daos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchedCommand;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.CollectionsUtils.mergeInMapWithArrayLists;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.Logs.logError;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerGroupedLikesOr;
import static diarsid.beam.core.base.util.SqlUtil.multipleLowerLikeAnd;
import static diarsid.beam.core.base.util.SqlUtil.patternToCharCriterias;
import static diarsid.beam.core.base.util.SqlUtil.shift;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.jdbc.transactions.core.Params.params;

import diarsid.jdbc.transactions.RowConversion;

import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_EXECUTOR_COMMAND;


class H2DaoBatches 
        extends BeamCommonDao 
        implements DaoBatches {
    
    private final RowConversion<String> rowToBatchNameConversion;
    private final Function<BatchedCommand, Params> batchedCommandToParams;
    private final Function<Map.Entry<String, List<ExecutorCommand>>, Batch> entryToBatch;
    
    H2DaoBatches(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToBatchNameConversion = (row) -> {
            return (String) row.get("bat_name");
        };
        this.batchedCommandToParams = (batchedCommand) -> {
            return params(
                batchedCommand.batch().name(),
                batchedCommand.unwrap().type().name(),
                batchedCommand.orderInBatch(),
                batchedCommand.unwrap().originalArgument()
            );
        };
        this.entryToBatch = (entry) -> {
            return new Batch(entry.getKey(), entry.getValue());
        };
    }

    @Override
    public boolean isNameFree(Initiator initiator, String exactName) {
        try {
            return ! super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) IS ? ",
                            lower(exactName));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "is name free request failed.");
            return false;
        }
    }

    @Override
    public List<String> getBatchNamesByNamePattern(
            Initiator initiator, String pattern) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            List<String> found;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            String.class,
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) LIKE ? ",
                            this.rowToBatchNameConversion,
                            lowerWildcard(pattern))
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            List<String> criterias = patternToCharCriterias(pattern);
            found = transact
                    .doQueryAndStreamVarargParams(
                            String.class,
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLikeAnd("bat_name", criterias.size()),
                            this.rowToBatchNameConversion,
                            criterias)
                    .collect(toList());
            
            if ( nonEmpty(found) ) {
                return found;
            }
            
            String andOrCondition = multipleLowerGroupedLikesOr("bat_name", criterias.size());
            List<String> shuffleFound;
            
            found = transact
                    .doQueryAndStreamVarargParams(
                            String.class,
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + andOrCondition,
                            this.rowToBatchNameConversion,
                            criterias)
                    .collect(toList());
            
            shift(criterias);
            shuffleFound = transact
                    .doQueryAndStreamVarargParams(
                            String.class,
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + andOrCondition,
                            this.rowToBatchNameConversion,
                            criterias)
                    .collect(toList());
            
            shuffleFound.retainAll(found);
            found.retainAll(shuffleFound);
            
            return found;
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(
                    initiator, "batch names search by name pattern '" + pattern + "' failed.");
            return emptyList();
        }
    }
    
    @Override
    public Optional<Batch> getBatchByExactName(Initiator initiator, String name) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean batchExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " + 
                            "WHERE LOWER(bat_name) IS ? ",
                            lower(name));
            
            List<ExecutorCommand> commands = transact
                    .ifTrue( batchExists )
                    .doQueryAndStreamVarargParams(ExecutorCommand.class,
                            "SELECT bat_command_type, " +
                            "       bat_command_original " +
                            "FROM batch_commands " +
                            "WHERE LOWER(bat_name) IS ? " +
                            "ORDER BY bat_command_order" ,
                            ROW_TO_EXECUTOR_COMMAND,
                            lower(name))
                    .collect(toList());
            
            if ( nonEmpty(commands) ) {
                return Optional.of(new Batch(name, commands));
            } else {
                return Optional.empty();
            }
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(
                    initiator, "batch obtaining by name '" + name + "' failed.");
            return Optional.empty();
        }
    }

    @Override
    public boolean saveBatch(Initiator initiator, Batch batch) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean nameIsFree = ! transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE bat_name IS ? ", 
                            batch.name());
            
            int nameSavedCount = transact
                    .ifTrue( nameIsFree )
                    .doUpdateVarargParams(
                            "INSERT INTO batches (bat_name) " +
                            "VALUES ( ? )", 
                            batch.name());
            
            int commandSavedCount = stream(transact
                    .ifTrue( nameIsFree )
                    .ifTrue( nonEmpty(batch.batchedCommands()) )
                    .doBatchUpdate(
                            "INSERT INTO batch_commands (" +
                            "       bat_name, " +
                            "       bat_command_type, " +
                            "       bat_command_order, " +
                            "       bat_command_original ) " +
                            "VALUES ( ?, ?, ?, ? ) ",
                            batch.batchedCommands()
                                    .stream()
                                    .map(this.batchedCommandToParams)
                                    .collect(toSet()))
            ).sum();
            
            return ( 
                    nameIsFree && 
                    nameSavedCount == 1 && 
                    commandSavedCount == batch.batchedCommands().size() );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "batch saving failed.");
            return false;
        }
    }

    @Override
    public boolean removeBatch(Initiator initiator, String batchName) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean nameRemoved = transact
                    .doUpdateVarargParams(
                            "DELETE FROM batches " +
                            "WHERE bat_name IS ? ",
                            batchName) 
                    == 1;
            
            int commandsRemoved = transact
                    .ifTrue( nameRemoved )
                    .doUpdateVarargParams(
                            "DELETE FROM batch_commands " +
                            "WHERE bat_name IS ? ",
                            batchName); 
            
            transact
                    .ifTrue( commandsRemoved < 1 )
                    .rollbackAndProceed();
            
            return ( nameRemoved && commandsRemoved > 0 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "batch removing failed.");
            return false;
        }
    }

    @Override
    public boolean editBatchName(Initiator initiator, String batchName, String newName) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int updatedNameQty = transact
                    .doUpdateVarargParams(
                            "UPDATE batches " +
                            "SET bat_name = ? " +
                            "WHERE bat_name IS ? ",
                            newName, batchName);
            
            int updatedCommands = transact
                    .ifTrue( updatedNameQty == 1 )
                    .doUpdateVarargParams(
                            "UPDATE batch_commands " +
                            "SET bat_name = ? " +
                            "WHERE bat_name IS ? ",
                            newName, batchName);
            
            transact
                    .ifTrue( updatedNameQty != 1 || updatedCommands < 1 )
                    .rollbackAndProceed();
            
            return ( updatedNameQty == 1 && updatedCommands > 0 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(
                    initiator, 
                    "batch name changing: " + batchName + " -> " + newName + " failed.");
            return false;
        }
    }

    @Override
    public boolean editBatchCommands(
            Initiator initiator, String batchName, List<ExecutorCommand> newCommands) {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            if ( newCommands.isEmpty() ) {
                return false;
            }
            
            boolean oldCommandsRemoved = transact
                    .doUpdateVarargParams(
                            "DELETE FROM batch_commands " +
                            "WHERE bat_name IS ? ", 
                            batchName) 
                    > 0;
            
            int modified = stream(transact
                    .ifTrue( oldCommandsRemoved )
                    .doBatchUpdate(
                            "INSERT INTO batch_commands (" +
                            "       bat_name, " +
                            "       bat_command_type, " +
                            "       bat_command_order, " +
                            "       bat_command_original ) " +
                            "VALUES ( ?, ?, ?, ? )",
                            newCommands
                                    .stream()
                                    .map(command -> params(
                                            batchName,
                                            command.type().name(),
                                            newCommands.indexOf(command),
                                            command.originalArgument()))
                                    .collect(toSet()))
            ).sum();
            
            transact
                    .ifTrue( modified != newCommands.size() )
                    .rollbackAndProceed();
            
            return ( 
                    oldCommandsRemoved && 
                    modified == newCommands.size() );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "batch commands changing failed.");
            return false;
        }
    }

    @Override
    public boolean editBatchOneCommand(
            Initiator initiator, String batchName, int commandOrder, ExecutorCommand newCommand) {
        try (JdbcTransaction transact = super.openTransaction()) {
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE batch_commands " +
                            "SET " +
                            "   bat_command_type = ?, " +
                            "   bat_command_original = ? " +
                            "WHERE ( bat_name IS ? ) AND ( bat_command_order IS ? ) ",
                            newCommand.type().name(),
                            newCommand.originalArgument(),
                            batchName,
                            commandOrder);
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "batch one command changing failed.");
            return false;
        }
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        try {
            Map<String, List<ExecutorCommand>> collectedBatches = new HashMap<>();
            
            super.openDisposableTransaction()
                    .doQuery("SELECT bat_name, " + 
                            "       bat_command_type, " +
                            "       bat_command_original " +
                            "FROM batch_commands " +
                            "ORDER BY bat_name, bat_command_order",
                            (row) -> { 
                                mergeInMapWithArrayLists(collectedBatches, 
                                        (String) row.get("bat_name"), 
                                        ROW_TO_EXECUTOR_COMMAND.convert(row));
                            });
            
            return collectedBatches
                    .entrySet()
                    .stream()
                    .map(this.entryToBatch)
                    .collect(toList());
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logError(H2DaoBatches.class, ex);
            super.ioEngine().report(initiator, "all batches obtaining failed.");
            return emptyList();
        }
    }
}
