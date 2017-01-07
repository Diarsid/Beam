/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package diarsid.beam.core.modules.data.daos.sql.h2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import diarsid.beam.core.control.io.base.Initiator;
import diarsid.beam.core.control.io.base.InnerIoEngine;
import diarsid.beam.core.control.io.commands.ArgumentedCommand;
import diarsid.beam.core.domain.entities.Batch;
import diarsid.beam.core.domain.entities.BatchedCommand;
import diarsid.beam.core.modules.data.DaoBatches;
import diarsid.beam.core.modules.data.DataBase;
import diarsid.beam.core.modules.data.daos.BeamDao;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.PerRowConversion;
import diarsid.jdbc.transactions.core.Params;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import static diarsid.beam.core.control.io.commands.Commands.restoreFrom;
import static diarsid.beam.core.util.CollectionsUtils.mergeInMapWithArrayLists;
import static diarsid.beam.core.util.CollectionsUtils.nonEmpty;
import static diarsid.beam.core.util.SqlUtil.SqlOperator.AND;
import static diarsid.beam.core.util.SqlUtil.lowerWildcard;
import static diarsid.beam.core.util.SqlUtil.lowerWildcardList;
import static diarsid.beam.core.util.SqlUtil.multipleLowerLike;
import static diarsid.beam.core.util.StringUtils.nonNullNonEmpty;
import static diarsid.jdbc.transactions.core.Params.params;


class H2DaoBatches 
        extends BeamDao 
        implements DaoBatches {
    
    private final PerRowConversion<String> rowToBatchNameConversion;
    private final PerRowConversion<ArgumentedCommand> rowToCommandConversion;
    private final Function<BatchedCommand, Params> batchedCommandToParams;
    
    H2DaoBatches(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
        this.rowToBatchNameConversion = (row) -> {
            return (String) row.get("bat_name");
        };
        this.rowToCommandConversion = (row) -> {
            return restoreFrom(
                    (String) row.get("bat_command_type"), 
                    (String) row.get("bat_command_original"), 
                    (String) row.get("bat_command_extended")
            );
        };
        this.batchedCommandToParams = (batchedCommand) -> {
            return params(
                batchedCommand.batch().getName(),
                batchedCommand.command().type().name(),
                batchedCommand.orderInBatch(),
                batchedCommand.command().stringifyOriginal(),
                batchedCommand.command().stringifyExtended()
            );
        };
    }

    @Override
    public List<String> getBatchNamesByName(
            Initiator initiator, String batchName) {
        try {
            return super.getDisposableTransaction()
                    .ifTrue( nonNullNonEmpty(batchName) )
                    .doQueryAndStreamVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE LOWER(bat_name) LIKE ? ",
                            this.rowToBatchNameConversion,
                            String.class,
                            lowerWildcard(batchName))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }

    @Override
    public List<String> getBatchNamesByNameParts(Initiator initiator, List<String> batchNameParts) {
        try {
            return super.getDisposableTransaction()
                    .ifTrue( nonEmpty(batchNameParts) )
                    .doQueryAndStream(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE " + multipleLowerLike("bat_name", batchNameParts.size(), AND),
                            this.rowToBatchNameConversion,
                            String.class,
                            lowerWildcardList(batchNameParts))
                    .collect(toList());
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }
    
    @Override
    public Optional<Batch> getBatchByName(Initiator initiator, String name) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean batchExists = transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " + 
                            "WHERE bat_name IS ? ",
                            name);
            
            List<ArgumentedCommand> commands = transact
                    .ifTrue( batchExists )
                    .doQueryAndStreamVarargParams(
                            "SELECT bat_command_type, " +
                            "       bat_command_original, " +
                            "       bat_command_extended " +
                            "FROM batch_commands " +
                            "WHERE bat_name IS ? " +
                            "ORDER BY bat_command_order" ,
                            this.rowToCommandConversion,
                            ArgumentedCommand.class,
                            name)
                    .collect(toList());
            
            if ( nonEmpty(commands) ) {
                return Optional.of(new Batch(name, commands));
            } else {
                return Optional.empty();
            }
        } catch (TransactionHandledSQLException ex) {
            
            return Optional.empty();
        }
    }

    @Override
    public boolean saveBatch(Initiator initiator, Batch batch) {        
        try (JdbcTransaction transact = super.getTransaction()) {
            
            boolean nameIsFree = ! transact
                    .doesQueryHaveResultsVarargParams(
                            "SELECT bat_name " +
                            "FROM batches " +
                            "WHERE bat_name IS ? ", 
                            batch.getName());
            
            int nameSavedCount = transact
                    .ifTrue( nameIsFree )
                    .doUpdateVarargParams(
                            "INSERT INTO batches (bat_name) " +
                            "VALUES ( ? )", 
                            batch.getName());
            
            int commandSavedCount = stream(transact
                    .ifTrue( nameIsFree )
                    .ifTrue( nonEmpty(batch.getCommands()) )
                    .doBatchUpdate(
                            "INSERT INTO batch_commands (" +
                            "       bat_name, " +
                            "       bat_command_type, " +
                            "       bat_command_order, " +
                            "       bat_command_original, " +
                            "       bat_command_extended ) " +
                            "VALUES ( ?, ?, ?, ?, ? ) ",
                            batch.getCommands()
                                    .stream()
                                    .map(this.batchedCommandToParams)
                                    .collect(toSet()))
            ).sum();
            
            return ( 
                    nameIsFree && 
                    nameSavedCount == 1 && 
                    commandSavedCount == batch.getCommands().size() );
            
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean removeBatch(Initiator initiator, String batchName) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editBatchName(Initiator initiator, String batchName, String newName) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
            
            
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
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editBatchCommands(
            Initiator initiator, String batchName, List<ArgumentedCommand> newCommands) {
        try (JdbcTransaction transact = super.getTransaction()) {
            
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
                            "       bat_command_original, " +
                            "       bat_command_extended) " +
                            "VALUES ( ?, ?, ?, ?, ? )",
                            newCommands
                                    .stream()
                                    .map(command -> params(
                                            batchName,
                                            command.type().name(),
                                            newCommands.indexOf(command),
                                            command.stringifyOriginal(),
                                            command.stringifyExtended()))
                                    .collect(toSet()))
            ).sum();
            
            transact
                    .ifTrue( modified != newCommands.size() )
                    .rollbackAndProceed();
            
            return ( 
                    oldCommandsRemoved && 
                    modified == newCommands.size() );
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public boolean editBatchOneCommand(
            Initiator initiator, String batchName, int commandOrder, ArgumentedCommand newCommand) {
        try (JdbcTransaction transact = super.getTransaction()) {
            int modified = transact
                    .doUpdateVarargParams(
                            "UPDATE batch_commands " +
                            "SET    bat_command_type = ?, " +
                            "       bat_command_original = ?, " +
                            "       bat_command_extended = ? " +
                            "WHERE ( bat_name IS ? ) AND ( bat_command_order IS ? ) ",
                            newCommand.type().name(),
                            newCommand.stringifyOriginal(),
                            newCommand.stringifyExtended(),
                            batchName,
                            commandOrder);
            
            transact
                    .ifTrue( modified != 1 )
                    .rollbackAndProceed();
            
            return ( modified == 1 );
        } catch (TransactionHandledSQLException ex) {
            
            return false;
        }
    }

    @Override
    public List<Batch> getAllBatches(Initiator initiator) {
        try {
            Map<String, List<ArgumentedCommand>> collectedBatches = new HashMap<>();
            
            super.getDisposableTransaction()
                    .doQuery(
                            "SELECT bat_name, " + 
                            "        bat_command_type, " +
                            "        bat_command_original, " +
                            "        bat_command_extended " +
                            "FROM batch_commands " +
                            "ORDER BY bat_name, bat_command_order",
                            (row) -> { 
                                mergeInMapWithArrayLists(
                                        collectedBatches, 
                                        (String) row.get("bat_name"), 
                                        this.rowToCommandConversion.convert(row));
                            });
            
            return collectedBatches
                    .entrySet()
                    .stream()
                    .map(entry -> new Batch(entry.getKey(), entry.getValue()))
                    .collect(toList());
            
        } catch (TransactionHandledSQLException ex) {
            
            return emptyList();
        }
    }
}
