package diarsid.beam.core.modules.data.sql.daos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.List;
import java.util.Optional;

import diarsid.beam.core.base.control.io.base.actors.Initiator;
import diarsid.beam.core.base.control.io.base.actors.InnerIoEngine;
import diarsid.beam.core.base.control.io.commands.CommandType;
import diarsid.beam.core.base.control.io.commands.executor.InvocationCommand;
import diarsid.beam.core.base.data.DataBase;
import diarsid.beam.core.modules.data.BeamCommonDao;
import diarsid.beam.core.modules.data.DaoCommands;
import diarsid.jdbc.transactions.JdbcTransaction;
import diarsid.jdbc.transactions.exceptions.TransactionHandledException;
import diarsid.jdbc.transactions.exceptions.TransactionHandledSQLException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static diarsid.beam.core.base.util.Logging.logFor;
import static diarsid.beam.core.base.util.SqlUtil.lowerWildcardAfter;
import static diarsid.beam.core.base.util.StringUtils.lower;
import static diarsid.beam.core.modules.data.sql.daos.RowToEntityConversions.ROW_TO_INVOCATION_COMMAND;


abstract class H2DaoCommandsV0 
        extends BeamCommonDao
        implements DaoCommands {
        
    H2DaoCommandsV0(DataBase dataBase, InnerIoEngine ioEngine) {
        super(dataBase, ioEngine);
    }
    
    @Override
    public Optional<InvocationCommand> getByExactOriginalAndType(
            Initiator initiator, String original, CommandType type) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndConvertFirstRowVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE ( com_type IS ? ) AND ( LOWER(com_original) IS ? ) ",
                            type.name(), lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("get %s %s command", type, original), ex);
            this.ioEngine().report(initiator, format("error on seasrch %s in commands", original));
            return Optional.empty();
        }
    }

    @Override
    public List<InvocationCommand> getByExactOriginalOfAnyType(
            Initiator initiator, String original) {
        try {
            return super.openDisposableTransaction()
                    .doQueryAndStreamVarargParams(
                            ROW_TO_INVOCATION_COMMAND,
                            "SELECT com_type, com_original, com_extended " +
                            "FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original))
                    .collect(toList());
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("get %s command", original), ex);
            this.ioEngine().report(initiator, format("error on search %s in commands", original));
            return emptyList();
        }
    }

    @Override
    public boolean save(
            Initiator initiator, InvocationCommand command) {        
        try (JdbcTransaction transact = super.openTransaction()) {
            return this.saveUsingTransaction(command, transact);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("save %s", command.originalArgument()), ex);
            this.ioEngine().report(
                    initiator, 
                    format("error on save %s in commands", command.originalArgument()));
            return false;
        }
    }
    
    @Override
    public boolean save(
            Initiator initiator, List<? extends InvocationCommand> commands) {       
        commands.forEach(command -> logFor(this).info(
                "saving: " + command.originalArgument() + ":" + command.stringify()));
        try (JdbcTransaction transact = super.openTransaction()) {
            boolean done = false;
            for (InvocationCommand command : commands) {
                done = this.saveUsingTransaction(command, transact);
                if ( ! done ) {
                    transact.rollbackAndProceed();
                    return false;
                }
            }
            return done;
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {     
            commands.forEach(command -> logFor(this).error(command.stringify()));
            logFor(this).error("save on %s commands", ex);
            this.ioEngine().report(initiator, "error on commands saving");
            return false;
        }
    }
    
    private boolean saveUsingTransaction(InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
        int modified;
        
        if ( command.argument().isOriginalEqualToExtended() ) {
            modified = this.saveBasicallyUsingTransaction(command, transact);
        } else {
            modified = this.saveAdvancinglyUsingTransaction(command, transact);
        }
        
        return modified > 0;
    }
    
    private int saveBasicallyUsingTransaction(
            InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {
        
        boolean commandExists = transact
                .doesQueryHaveResultsVarargParams(
                        "SELECT * " +
                        "FROM commands " +
                        "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                        lower(command.originalArgument()), command.type());

        int modified = 0;
        if ( commandExists ) {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "UPDATE commands " +
                            "SET com_extended = ? " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            command.extendedArgument(), 
                            lower(command.originalArgument()), 
                            command.type());
        } else {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "INSERT INTO commands ( com_type, com_original, com_extended )" +
                            "VALUES ( ?, ?, ? ) " , 
                            command.type(),
                            command.originalArgument(),
                            command.extendedArgument());
        }
        
        return modified;
    }
    
    private int saveAdvancinglyUsingTransaction(
            InvocationCommand command, JdbcTransaction transact) 
            throws TransactionHandledSQLException, TransactionHandledException {        
        
        int modified = this.saveBasicallyUsingTransaction(command, transact);

        boolean extendedExistsInOriginal = transact
                .doesQueryHaveResultsVarargParams(
                    "SELECT * " +
                    "FROM commands " +
                    "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                    lower(command.extendedArgument()), command.type());

        if ( extendedExistsInOriginal ) {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "UPDATE commands " +
                            "SET com_extended = ? " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ", 
                            command.extendedArgument(), 
                            lower(command.extendedArgument()), 
                            command.type());
        } else {
            modified = modified + transact
                    .doUpdateVarargParams(
                            "INSERT INTO commands ( com_type, com_original, com_extended )" +
                            "VALUES ( ?, ?, ? ) " , 
                            command.type(),
                            command.extendedArgument(),
                            command.extendedArgument());
        }

        if ( modified > 0 ) {
            logFor(this).info("saved: " + modified);
        }

        return modified;
    }
    
    @Override
    public boolean delete(
            Initiator initiator, InvocationCommand command) {
        logFor(this).info(
                "delete all: " + command.originalArgument() + ":" + command.extendedArgument());
        try (JdbcTransaction transact = super.openTransaction()) {
            
            int modified = transact
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(command.originalArgument()), command.type());
            
            if ( command.argument().isExtended() ) {
                modified = modified + transact
                        .doUpdateVarargParams(
                                "DELETE FROM commands " +
                                "WHERE ( LOWER(com_extended) IS ? ) AND ( com_type IS ? ) ",
                                lower(command.extendedArgument()), command.type());
            }
            
            if ( modified > 0 ) {
                logFor(this).info("deleted: " + modified);
            }
            
            return ( modified > 0 );
            
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("delete %s", command.originalArgument()), ex);
            this.ioEngine().report(
                    initiator, 
                    format("error on remove %s in commands", command.originalArgument()));
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfAllTypes(
            Initiator initiator, String original) {
        logFor(this).info("delete by original: " + original);
        try {
            return 0 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE LOWER(com_original) IS ? ",
                            lower(original));
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("delete on %s", original), ex);
            this.ioEngine().report(initiator, format("error on remove %s in commands", original));
            return false;
        }
    }

    @Override
    public boolean deleteByExactOriginalOfType(
            Initiator initiator, String original, CommandType type) {
        logFor(this).info("delete by original and type: " + original + ", " + type.name());
        try {
            return 1 == super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_original) IS ? ) AND ( com_type IS ? ) ",
                            lower(original), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("delete on %s", original), ex);
            this.ioEngine().report(initiator, format("error on remove %s in commands", original));
            return false;
        }
    }

    @Override
    public boolean deleteByExactExtendedOfType(
            Initiator initiator, String extended, CommandType type) {
        logFor(this).info("delete by extended and type: " + extended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) IS ? ) AND ( com_type IS ? ) ",
                            lower(extended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("delete on %s", extended), ex);
            this.ioEngine().report(initiator, format("error on remove %s in commands", extended));
            return false;
        }
    }

    @Override
    public boolean deleteByPrefixInExtended(
            Initiator initiator, String prefixInExtended, CommandType type) {
        logFor(this).info(
                "delete by extended-prefix and type: " + prefixInExtended + ", " + type.name());
        try {
            return 1 < super.openDisposableTransaction()
                    .doUpdateVarargParams(
                            "DELETE FROM commands " +
                            "WHERE ( LOWER(com_extended) LIKE ? ) AND ( com_type IS ? ) ",
                            lowerWildcardAfter(prefixInExtended), type);
        } catch (TransactionHandledSQLException|TransactionHandledException ex) {
            logFor(this).error(format("search on %s and type %s", prefixInExtended, type), ex);
            this.ioEngine().report(
                    initiator, format("error on remove commands by prefix %s", prefixInExtended));
            return false;
        }
    }
    
}
