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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import diarsid.beam.core.base.control.io.commands.executor.ExecutorCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.base.data.DataExtractionException;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchedCommand;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.RowConversion;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.base.util.CollectionsUtils.mergeInMapWithArrayLists;
import static diarsid.beam.core.base.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_EXECUTOR_COMMAND;
import static diarsid.jdbc.transactions.core.Params.params;


abstract class H2DaoBatchesV0 
        extends BeamCommonDao 
        implements DaoBatches {
    
    private final RowConversion<String> rowToBatchNameConversion;
    private final Function<BatchedCommand, Params> batchedCommandToParams;
    private final Function<Map.Entry<String, List<ExecutorCommand>>, Batch> entryToBatch;
    
    H2DaoBatchesV0(DataBase dataBase) {
        super(dataBase);
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
    
    protected RowConversion<String> rowToBatchNameConversion() {
        return this.rowToBatchNameConversion;
    }

    @Override
    public boolean isNameFree(String exactName) throws DataExtractionException {
        try {
            return ! super.openDisposableTransaction()
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) IS ? ",
                            lower(exactName));
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
    
    @Override
    public Optional<Batch> getBatchByExactName(String name) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean batchExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " + 
                            "WHERE LOWER(bat_name) IS ? ",
                            lower(name));
            
            List<ExecutorCommand> commands = transact
                    .ifTrue( batchExists )
                    .doQueryAndStreamVarargParams(
                            ROW_TO_EXECUTOR_COMMAND,
                            "SELECT bat_command_type, " +
                            "       bat_command_original " +
                            "FROM batch_commands " +
                            "WHERE LOWER(bat_name) IS ? " +
                            "ORDER BY bat_command_order",
                            lower(name))
                    .collect(toList());
            
            if ( nonEmpty(commands) ) {
                return Optional.of(new Batch(name, commands));
            } else {
                return Optional.empty();
            }
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean saveBatch(Batch batch) throws DataExtractionException {        
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
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean removeBatch(String batchName) throws DataExtractionException {
        try (JdbcTransaction transact = super.openTransaction()) {
            
            boolean batchExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " + 
                            "WHERE LOWER(bat_name) IS ? ",
                            lower(batchName));
            
            if ( !batchExists ) {                
                return false;
            }
            
            boolean commandsRemoved = transact
                    .doUpdateVarargParams(
                            "DELETE FROM batch_commands " +
                            "WHERE bat_name IS ? ",
                            batchName)
                    > 0; 
            
            boolean nameRemoved = transact                    
                    .ifTrue( commandsRemoved )
                    .doUpdateVarargParams(
                            "DELETE FROM batches " +
                            "WHERE bat_name IS ? ",
                            batchName) 
                    == 1;
            
            if ( nameRemoved && commandsRemoved ) {
                return true;
            } else {
                transact.rollbackAndProceed();
                return false;
            }
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editBatchName(String batchName, String newName) throws DataExtractionException {
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
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editBatchCommands(
            String batchName, List<ExecutorCommand> newCommands) throws DataExtractionException {
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
            
            AtomicInteger newCommandIndex = new AtomicInteger();
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
                                            newCommandIndex.getAndIncrement(),
                                            command.originalArgument()))
                                    .collect(toSet()))
            ).sum();
            
            transact
                    .ifTrue( modified != newCommands.size() )
                    .rollbackAndProceed();
            
            return ( 
                    oldCommandsRemoved && 
                    modified == newCommands.size() );
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public boolean editBatchOneCommand(
            String batchName, int commandOrder, ExecutorCommand newCommand) 
            throws DataExtractionException {
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
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }

    @Override
    public List<Batch> getAllBatches() throws DataExtractionException {
        try {
            Map<String, List<ExecutorCommand>> collectedBatches = new HashMap<>();
            
            super.openDisposableTransaction()
                    .doQuery(
                            (row) -> { 
                                mergeInMapWithArrayLists(collectedBatches, 
                                        (String) row.get("bat_name"), 
                                        ROW_TO_EXECUTOR_COMMAND.convert(row));
                            },
                            "SELECT bat_name, " + 
                            "       bat_command_type, " +
                            "       bat_command_original " +
                            "FROM batch_commands " +
                            "ORDER BY bat_name, bat_command_order");
            
            return collectedBatches
                    .entrySet()
                    .stream()
                    .map(this.entryToBatch)
                    .collect(toList());
            
        } catch (TransactionHandledSQLException | TransactionHandledException e) {
            throw super.logAndWrap(e);
        }
    }
}
